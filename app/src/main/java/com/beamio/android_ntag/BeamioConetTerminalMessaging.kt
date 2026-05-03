package com.beamio.android_ntag

import android.util.Base64
import android.util.Log
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.EncryptionPurpose
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.key.info.KeyRingInfo
import org.pgpainless.key.parsing.KeyRingReader
import org.pgpainless.util.Passphrase
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * CoNET Chat 注册 + gossip 发「终端申请 admin」消息，对齐 iOS
 * [BeamioConetChatRouteRegister]、[BeamioConetGossipSend] 与 bizSite `chat.ts` / `beamio.ts`。
 */
object BeamioConetTerminalMessaging {

    /** `adb logcat -s BeamioConetPOS` 排查终端权限 gossip / regiestChatRoute / searchKey */
    private const val LOG_TAG = "BeamioConetPOS"

    private const val CONET_MAINNET_RPC = "https://rpc1.conet.network"
    /** CoNET AddressPGP（与 iOS `BeamioConstants.conetAddressPgpManager`、`x402sdk/db.addressPGP`、`deployments/conet-addresses.json` 一致）。 */
    private const val CONET_PGP_MANAGER = "0xb2aABe52f476356AE638839A786EAE425A0c1b66"
    private const val BEAMIO_API = "https://beamio.app"
    private const val SEARCH_KEY_SELECTOR = "052f2778"

    private val routeDomainHexPool: List<String> = listOf(
        "9977E9A45187DD80", "B4CB0A41352E9BDF", "20AB90FE82D0E9E3", "AE85A2AEEC768225",
        "2CC183B62F2223FD", "221B4F18389D6AAD", "D9ADB0E1E4F342D9", "94FD3DBABD9819C2",
    )

    private val gossipPostDomainHexIds: List<String> = listOf(
        "9977E9A45187DD80", "B4CB0A41352E9BDF", "20AB90FE82D0E9E3", "AE85A2AEEC768225",
        "2CC183B62F2223FD", "221B4F18389D6AAD", "D9ADB0E1E4F342D9", "94FD3DBABD9819C2",
        "810DFC165FC60B63", "274E663C521F4889", "DED9FAA490248805", "F8117E1568EEAED7",
        "EFF609F7062B78D3", "D98C66B8211048D4", "9C0E4F8A7542CD02", "BB79725DF3CDC2BF",
        "AC27967AA3D69FF6", "DCD8C3D278AB48CB", "BB64E2DB230F4EA3", "1FDE43C9C8225B30",
        "896E1EEA0B7A5B6F", "B2F2F581BB2548E0", "2D662019CBD8EFFD", "81B39FE096AFD227",
    )

    private fun conetEthCallSync(toAddress: String, dataHex: String): String? {
        val to = if (toAddress.startsWith("0x", true)) toAddress.lowercase(Locale.US) else "0x${toAddress.lowercase(Locale.US)}"
        val data = if (dataHex.startsWith("0x", true)) dataHex.lowercase(Locale.US) else "0x$dataHex".lowercase(Locale.US)
        return try {
            val reqBody = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$to","data":"$data"},"latest"],"id":1}"""
            val conn = URL(CONET_MAINNET_RPC).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 25_000
            conn.readTimeout = 25_000
            conn.outputStream.use { it.write(reqBody.toByteArray(StandardCharsets.UTF_8)) }
            val code = conn.responseCode
            val json = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code !in 200..299) {
                Log.w(LOG_TAG, "conetEthCall http=$code to=$to data=${data.take(12)}… body=${json.take(280)}")
                return null
            }
            val root = JSONObject(json)
            if (root.has("error")) {
                Log.w(LOG_TAG, "conetEthCall jsonrpc error to=$to err=${root.opt("error")}")
                return null
            }
            val result = root.optString("result", "")
            if (result.isEmpty() || result == "0x") {
                Log.w(LOG_TAG, "conetEthCall empty result to=$to")
                return null
            }
            result
        } catch (e: Exception) {
            Log.w(LOG_TAG, "conetEthCall exception to=$toAddress: ${e.message}")
            null
        }
    }

    private fun encodeSearchKeyCall(recipientEoaLower40: String): String? {
        var h = recipientEoaLower40.trim().lowercase(Locale.US)
        if (h.startsWith("0x")) h = h.removePrefix("0x")
        if (h.length != 40 || !h.all { it in '0'..'9' || it in 'a'..'f' }) return null
        return "0x$SEARCH_KEY_SELECTOR" + "0".repeat(24) + h
    }

    private fun hexToBytes(hex: String): ByteArray? {
        val s = hex.trim().removePrefix("0x").removePrefix("0X")
        if (s.length % 2 != 0) return null
        val out = ByteArray(s.length / 2)
        var i = 0
        while (i < s.length) {
            out[i / 2] = ((s[i].digitToInt(16) shl 4) or s[i + 1].digitToInt(16)).toByte()
            i += 2
        }
        return out
    }

    private fun u256Tail8AsLong(data: ByteArray, byteOffset: Int): Long {
        if (byteOffset + 32 > data.size) return 0
        var v = 0L
        for (i in 24 until 32) {
            v = (v shl 8) or (data[byteOffset + i].toLong() and 0xff)
        }
        return v
    }

    private fun readAbiString(data: ByteArray, headWordByteOffset: Int): String? {
        val ptr = u256Tail8AsLong(data, headWordByteOffset).toInt()
        if (ptr < 0 || ptr + 32 > data.size) return null
        val len = u256Tail8AsLong(data, ptr).toInt()
        if (len < 0 || ptr + 32 + len > data.size) return null
        return String(data, ptr + 32, len, StandardCharsets.UTF_8)
    }

    /** 与 iOS `BeamioConetSearchKeyAbi.decodeSearchKeyUserPublicArmored` */
    fun decodeSearchKeyUserPublicArmored(hex: String): String? {
        val raw = hex.trim().removePrefix("0x").removePrefix("0X")
        val data = hexToBytes(raw) ?: run {
            Log.w(LOG_TAG, "decodeSearchKey: invalid hex (len=${raw.length})")
            return null
        }
        if (data.size < 160) {
            Log.w(LOG_TAG, "decodeSearchKey: abi too short bytes=${data.size}")
            return null
        }
        u256Tail8AsLong(data, 128)
        val userPubB64 = readAbiString(data, 32) ?: run {
            Log.w(LOG_TAG, "decodeSearchKey: readAbiString(offset=32) failed")
            return null
        }
        if (userPubB64.isEmpty()) {
            Log.w(LOG_TAG, "decodeSearchKey: userPublicKeyArmored b64 empty")
            return null
        }
        val rawArmored = try {
            Base64.decode(userPubB64, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "decodeSearchKey: base64 decode fail: ${e.message}")
            return null
        }
        val armored = String(rawArmored, StandardCharsets.UTF_8)
        return if (armored.contains("BEGIN PGP")) {
            Log.d(LOG_TAG, "decodeSearchKey: ok armoredLen=${armored.length}")
            armored
        } else {
            Log.w(LOG_TAG, "decodeSearchKey: decoded string has no BEGIN PGP (len=${armored.length})")
            null
        }
    }

    private fun fetchRecipientPublicArmoredSync(recipientEoa: String): String? {
        var h = recipientEoa.trim().lowercase(Locale.US)
        if (h.startsWith("0x")) h = h.removePrefix("0x")
        val dataHex = encodeSearchKeyCall(h) ?: run {
            Log.w(LOG_TAG, "fetchRecipientPgp: bad recipient eoa")
            return null
        }
        val resHex = conetEthCallSync(CONET_PGP_MANAGER, dataHex) ?: run {
            Log.w(LOG_TAG, "fetchRecipientPgp: eth_call failed parent=${h.take(6)}…")
            return null
        }
        Log.d(LOG_TAG, "fetchRecipientPgp: searchKey result hexLen=${resHex.length}")
        return decodeSearchKeyUserPublicArmored(resHex)
    }

    /** bizSite `aesGcmEncrypt` / iOS `aesGcmEncryptBeamioStyle`：SHA-256(password UTF-8)，AES-GCM，IV 12 字节前置，再 base64 */
    fun aesGcmEncryptBeamioStyle(plaintext: String, password: String): String {
        val pwHash = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(StandardCharsets.UTF_8))
        val key = SecretKeySpec(pwHash, "AES")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ctWithTag = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val combined = iv + ctWithTag
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun beamioToBase64(utf8: String): String =
        Base64.encodeToString(utf8.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

    private fun postRegiestChatRouteSync(
        walletChecksummed: String,
        keyIdUpperHex: String,
        publicKeyArmoredUtf8: String,
        secretKeyArmoredUtf8: String,
        walletPrivateKeyForAesPasswordWith0x: String,
        routeKeyId: String,
    ): Boolean {
        val enc = try {
            aesGcmEncryptBeamioStyle(secretKeyArmoredUtf8, walletPrivateKeyForAesPasswordWith0x)
        } catch (_: Exception) {
            return false
        }
        return try {
            val body = JSONObject().apply {
                put("wallet", walletChecksummed)
                put("keyID", keyIdUpperHex)
                put("publicKeyArmored", beamioToBase64(publicKeyArmoredUtf8))
                put("encrypKeyArmored", enc)
                put("routeKeyID", routeKeyId)
            }.toString()
            val conn = URL("$BEAMIO_API/api/regiestChatRoute").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 45_000
            conn.readTimeout = 45_000
            conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code !in 200..299) {
                Log.w(LOG_TAG, "regiestChatRoute http=$code body=${text.take(400)}")
                return false
            }
            val ok = JSONObject(text).optBoolean("ok", false)
            Log.d(LOG_TAG, "regiestChatRoute ok=$ok http=$code keyID=$keyIdUpperHex route=$routeKeyId")
            ok
        } catch (e: Exception) {
            Log.w(LOG_TAG, "regiestChatRoute exception: ${e.message}")
            false
        }
    }

    private fun hasOnChainUserPgpPublicSync(walletLower0x: String): Boolean {
        var h = walletLower0x.trim().lowercase(Locale.US)
        if (h.startsWith("0x")) h = h.removePrefix("0x")
        if (h.length != 40) return false
        val dataHex = encodeSearchKeyCall(h) ?: return false
        val hex = conetEthCallSync(CONET_PGP_MANAGER, dataHex) ?: return false
        return decodeSearchKeyUserPublicArmored(hex) != null
    }

    private fun pgpEncryptionSubkeyIdUpperHex16(secretRing: org.bouncycastle.openpgp.PGPSecretKeyRing): String? {
        val info = KeyRingInfo(secretRing)
        val enc = info.getEncryptionSubkeys(EncryptionPurpose.COMMUNICATIONS).firstOrNull()
            ?: info.getEncryptionSubkeys(EncryptionPurpose.STORAGE).firstOrNull()
            ?: return null
        val id = enc.keyID
        val hex = java.lang.Long.toUnsignedString(id, 16).uppercase(Locale.US)
        return hex.padStart(16, '0').takeLast(16)
    }

    private fun generateModernPgpKeyRing(userIdChecksummed: String): Triple<String, String, String>? {
        return try {
            val secret = PGPainless.generateKeyRing().modernKeyRing(userIdChecksummed, Passphrase.emptyPassphrase())
            val pubArmor = PGPainless.asciiArmor(PGPainless.extractCertificate(secret))
            val secArmor = PGPainless.asciiArmor(secret)
            val kid = pgpEncryptionSubkeyIdUpperHex16(secret) ?: return null
            Triple(pubArmor, secArmor, kid)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "generateModernPgpKeyRing failed: ${e.message}")
            null
        }
    }

    /**
     * 链上尚无发件人 PGP 时生成 modern 密钥环、`POST /api/regiestChatRoute`，并轮询 `searchKey` 至可读。
     * 与 iOS [BeamioConetChatRouteRegister.ensureRegisteredForSenderGossip] 一致。
     */
    fun ensureRegisteredForSenderGossip(privateKeyHexNo0x: String, walletChecksummed0x: String): Boolean {
        var pk = privateKeyHexNo0x.trim()
        if (pk.startsWith("0x", true) || pk.startsWith("0X")) pk = pk.removeRange(0, 2)
        if (pk.length != 64 || !pk.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return false
        pk = pk.lowercase(Locale.US)
        val addrLower = BeamioWeb3Wallet.addressHexLowerFromPrivateKeyHex(pk)
        if (hasOnChainUserPgpPublicSync(addrLower)) {
            Log.d(LOG_TAG, "ensureRegistered: already on chain sender=${addrLower.take(10)}…")
            return true
        }
        Log.d(LOG_TAG, "ensureRegistered: registering PGP for $walletChecksummed0x")
        val keys = generateModernPgpKeyRing(walletChecksummed0x) ?: run {
            Log.w(LOG_TAG, "ensureRegistered: generateModernPgpKeyRing returned null")
            return false
        }
        val (pubArmor, secArmor, keyIdUpper) = keys
        val route = routeDomainHexPool[Random.nextInt(routeDomainHexPool.size)]
        val pkWith0x = "0x$pk"
        val ok = postRegiestChatRouteSync(
            walletChecksummed = walletChecksummed0x,
            keyIdUpperHex = keyIdUpper,
            publicKeyArmoredUtf8 = pubArmor,
            secretKeyArmoredUtf8 = secArmor,
            walletPrivateKeyForAesPasswordWith0x = pkWith0x,
            routeKeyId = route,
        )
        if (!ok) {
            Log.w(LOG_TAG, "ensureRegistered: regiestChatRoute failed")
            return false
        }
        try {
            Thread.sleep(2000)
        } catch (_: InterruptedException) {
            return false
        }
        val after = hasOnChainUserPgpPublicSync(addrLower)
        Log.d(LOG_TAG, "ensureRegistered: after sleep chainHasPgp=$after")
        return after
    }

    private fun jsonTerminalPermissionInner(
        sendId: String,
        createdAt: Long,
        childEoa: String,
        childBeamioTag: String,
        parentBeamioTag: String,
    ): String {
        val o = JSONObject().apply {
            put("type", "beamio_pos_terminal_permission_v1")
            put("sendId", sendId)
            put("createdAt", createdAt)
            put("childEoa", childEoa.lowercase(Locale.US))
            put("childBeamioTag", childBeamioTag)
            put("parentBeamioTag", parentBeamioTag)
        }
        return o.toString()
    }

    private fun jsonChatOuterLine(sendId: String, createdAt: Long, innerText: String): String {
        val o = JSONObject().apply {
            put("sendId", sendId)
            put("from", "me")
            put("text", innerText)
            put("createdAt", createdAt)
        }
        return o.toString()
    }

    private fun postGossipPayloadSync(domainHex: String, armored: String): Boolean {
        return try {
            val urlStr = "https://${domainHex.lowercase(Locale.US)}.conet.network/post"
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000
            val body = JSONObject().put("data", armored).toString()
            conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            val code = conn.responseCode
            val errBody = if (code !in 200..299) {
                try {
                    conn.errorStream?.bufferedReader()?.readText()?.take(200) ?: ""
                } catch (_: Exception) {
                    ""
                }
            } else ""
            conn.disconnect()
            if (code in 200..299) {
                Log.d(LOG_TAG, "gossip POST ok domain=$domainHex http=$code")
            } else {
                Log.w(LOG_TAG, "gossip POST fail domain=$domainHex http=$code errBody=${errBody.take(200)}")
            }
            code in 200..299
        } catch (e: Exception) {
            Log.w(LOG_TAG, "gossip POST exception domain=$domainHex: ${e.message}")
            false
        }
    }

    private fun encryptOpenPgpArmoredMessageForRecipient(plainUtf8: ByteArray, recipientArmored: String): String? {
        return try {
            val recipientRing = KeyRingReader().publicKeyRing(recipientArmored) ?: run {
                Log.w(LOG_TAG, "encryptPgp: parse publicKeyRing failed")
                return null
            }
            // 与 iOS 使用收件人环上「加密子钥」一致，避免默认选钥与 Web/OpenPGP.js 不一致
            val encOpt = EncryptionOptions.encryptCommunications()
                .addRecipient(recipientRing, EncryptionOptions.encryptToFirstSubkey())
            val prod = ProducerOptions.encrypt(encOpt).setAsciiArmor(true)
            val baos = ByteArrayOutputStream()
            val encStream = PGPainless.encryptAndOrSign().onOutputStream(baos).withOptions(prod)
            encStream.use { it.write(plainUtf8) }
            val out = baos.toString(StandardCharsets.UTF_8)
            Log.d(LOG_TAG, "encryptPgp: ok cipherArmorLen=${out.length} plainLen=${plainUtf8.size}")
            out
        } catch (e: Exception) {
            Log.w(LOG_TAG, "encryptPgp: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    /**
     * 与 iOS [BeamioConetGossipSend.sendTerminalPermissionRequest]：EIP-191 签外层 pending 行，信封 base64(JSON) 的 UTF-8 字节作 PGP literal 加密，POST gossip。
     *
     * @param walletPrivateKeyHexWith0x 用于 AES 注册时已用 `0x`+64hex；此处签名用无 0x 调 [BeamioWeb3Wallet.signEthereumPersonalMessageUtf8]
     */
    fun sendTerminalPermissionRequest(
        recipientEoa: String,
        childEoa: String,
        childBeamioTag: String,
        parentBeamioTag: String,
        walletPrivateKeyHexWith0x: String,
    ): Boolean {
        Log.d(
            LOG_TAG,
            "sendPermission: child=${childEoa.take(12)}… parentTag=$parentBeamioTag childTagLen=${childBeamioTag.length} recipient=${recipientEoa.take(12)}…",
        )
        val armoredPub = fetchRecipientPublicArmoredSync(recipientEoa) ?: run {
            Log.w(LOG_TAG, "sendPermission: ABORT no parent PGP on chain (parent must register chat route in SilentPassUI/Web first)")
            return false
        }
        val sendId = UUID.randomUUID().toString().lowercase(Locale.US)
        val createdAt = System.currentTimeMillis()
        val innerText = jsonTerminalPermissionInner(
            sendId = sendId,
            createdAt = createdAt,
            childEoa = childEoa,
            childBeamioTag = childBeamioTag,
            parentBeamioTag = parentBeamioTag,
        )
        val outerLine = jsonChatOuterLine(sendId = sendId, createdAt = createdAt, innerText = innerText)
        val pkNo0x = walletPrivateKeyHexWith0x.trim().removePrefix("0x").removePrefix("0X").lowercase(Locale.US)
        val signMessage = try {
            BeamioWeb3Wallet.signEthereumPersonalMessageUtf8(pkNo0x, outerLine)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "sendPermission: personal_sign failed: ${e.message}")
            return false
        }
        val tsMs = System.currentTimeMillis()
        val envelope = JSONObject().apply {
            put("timestamp", tsMs)
            put("text", outerLine)
            put("from", childEoa.lowercase(Locale.US))
            put("signMessage", signMessage)
        }
        val envJson = envelope.toString()
        val b64 = Base64.encodeToString(envJson.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        val literal = b64.toByteArray(StandardCharsets.UTF_8)
        val armoredCipher = encryptOpenPgpArmoredMessageForRecipient(literal, armoredPub) ?: run {
            Log.w(LOG_TAG, "sendPermission: ABORT OpenPGP encrypt failed")
            return false
        }
        val domains = gossipPostDomainHexIds.shuffled().take(6)
        if (domains.isEmpty()) return false
        var anyOk = false
        for (d in domains) {
            if (postGossipPayloadSync(d, armoredCipher)) anyOk = true
        }
        if (anyOk) {
            Log.d(LOG_TAG, "sendPermission: SUCCESS gossip accepted by at least one node")
        } else {
            Log.w(LOG_TAG, "sendPermission: FAIL all ${domains.size} gossip POST(s) non-2xx or network error (see gossip lines above)")
        }
        return anyOk
    }
}
