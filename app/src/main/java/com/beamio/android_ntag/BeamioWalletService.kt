package com.beamio.android_ntag

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.WalletUtils
import java.io.File

/**
 * Beamio 钱包创建与登记服务。
 * 实现与 SilentPassUI createRecover + newUser 一致的逻辑，使用 Argon2id 抗暴力破解。
 */
object BeamioWalletService {
    private const val TAG = "BeamioWalletService"
    private const val BEAMIO_API = "https://beamio.app"
    private const val CONET_RPC = "https://mainnet-rpc1.conet.network"
    private const val ACCOUNT_REGISTRY = "0x3E15607BCf98B01e6C7dF834a2CEc7B8B6aFb1BC"

    // Argon2id 参数：与 web defaultBrowserParams 一致（32MB, 3 iter, 1 parallel）
    private const val ARGON2_MEMORY_KB = 32 * 1024
    private const val ARGON2_ITERATIONS = 3
    private const val ARGON2_PARALLELISM = 1
    private const val ARGON2_DKLEN = 32

    data class Argon2idHash(
        val algo: String = "argon2id",
        val v: Int = 19,
        val m: Int,
        val t: Int,
        val p: Int,
        val salt: String,
        val hash: String
    )

    data class RecoverCode(val code: String, val hash: String)

    data class CreateRecoverResult(
        val privateKeyHex: String,
        val recoverCode: String,
        val mnemonicPhrase: String
    )

    private fun secureRandomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }

    private fun b64Encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun b64Decode(s: String): ByteArray = Base64.decode(s, Base64.NO_WRAP)

    /** Argon2id 哈希密码，与 web hashPasswordBrowser 一致 */
    fun hashPassword(password: String): Argon2idHash {
        val salt = secureRandomBytes(16)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val output = ByteArray(ARGON2_DKLEN)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withIterations(ARGON2_ITERATIONS)
            .withMemoryAsKB(ARGON2_MEMORY_KB)
            .withParallelism(ARGON2_PARALLELISM)
            .build()
        Argon2BytesGenerator().apply {
            init(params)
            generateBytes(passwordBytes, output, 0, ARGON2_DKLEN)
        }
        return Argon2idHash(
            m = ARGON2_MEMORY_KB,
            t = ARGON2_ITERATIONS,
            p = ARGON2_PARALLELISM,
            salt = b64Encode(salt),
            hash = b64Encode(output)
        )
    }

    /** 从 password + stored 派生 AES-256 密钥 */
    private fun deriveAesKey(password: String, stored: Argon2idHash): ByteArray {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val salt = b64Decode(stored.salt)
        val output = ByteArray(32)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withIterations(stored.t)
            .withMemoryAsKB(stored.m)
            .withParallelism(stored.p)
            .build()
        Argon2BytesGenerator().apply {
            init(params)
            generateBytes(passwordBytes, output, 0, 32)
        }
        return output
    }

    /** AES-GCM 加密，输出 base64(iv || ciphertext) */
    private fun aesGcmEncrypt(plaintext: String, keyBytes: ByteArray): String {
        val iv = secureRandomBytes(12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), javax.crypto.spec.GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return b64Encode(iv + ciphertext)
    }

    /** 生成恢复码：code = 随机串，hash = keccak256(code, passcode) */
    private fun generateCode(passcode: String = ""): RecoverCode {
        val code = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").take(16)
        val input = (code + passcode).toByteArray(Charsets.UTF_8)
        val hashBytes = Hash.sha3(input)
        val hashHex = "0x" + Numeric.toHexStringNoPrefix(hashBytes)
        return RecoverCode(code = code, hash = hashHex)
    }

    /** Keccak256 字符串（与 ethers.solidityPackedKeccak256(['string'],[s]) 一致） */
    private fun keccak256String(s: String): String {
        val bytes = s.toByteArray(Charsets.UTF_8)
        return "0x" + Numeric.toHexStringNoPrefix(Hash.sha3(bytes))
    }

    /** 创建钱包并登记到 Beamio */
    fun createRecover(beamioName: String, pin: String): CreateRecoverResult? {
        return try {
            val trimmedName = beamioName.trim().replace(Regex("^@+"), "")
            if (trimmedName.length < 3) return null

            if (!checkBeamioAccountAvailable(trimmedName)) {
                Log.e(TAG, "BeamioTag $trimmedName is already taken")
                return null
            }

            val tempDir = File.createTempFile("bip39", "").parentFile ?: return null
            val bip39 = WalletUtils.generateBip39Wallet("", tempDir)
            val mnemonic = bip39.mnemonic
            val credentials = WalletUtils.loadBip39Credentials("", mnemonic)
            val privateKeyHex = Numeric.toHexStringNoPrefix(credentials.ecKeyPair.privateKey).padStart(64, '0')

            val recoverCode = generateCode("")
            val stored = hashPassword(pin)
            val phraseBase64 = b64Encode(mnemonic.toByteArray(Charsets.UTF_8))

            val key1 = deriveAesKey(recoverCode.code, stored)
            val img = aesGcmEncrypt(phraseBase64, key1)

            val key2 = deriveAesKey(pin, stored)
            val img1 = aesGcmEncrypt(phraseBase64, key2)

            val storedJson = JSONObject().apply {
                put("algo", stored.algo)
                put("v", stored.v)
                put("m", stored.m)
                put("t", stored.t)
                put("p", stored.p)
                put("salt", stored.salt)
                put("hash", stored.hash)
            }
            val inner1 = JSONObject().apply {
                put("stored", storedJson)
                put("img", img)
            }
            val storageEncryptedImg = b64Encode(inner1.toString().toByteArray(Charsets.UTF_8))

            val nameHash = keccak256String(trimmedName)
            val inner2 = JSONObject().apply {
                put("stored", storedJson)
                put("img", img1)
            }
            val storageEncryptedImg1 = b64Encode(inner2.toString().toByteArray(Charsets.UTF_8))

            val recoverData = JSONArray().apply {
                put(JSONObject().apply {
                    put("hash", recoverCode.hash)
                    put("encrypto", storageEncryptedImg)
                })
                put(JSONObject().apply {
                    put("hash", nameHash)
                    put("encrypto", storageEncryptedImg1)
                })
            }

            val success = newUser(trimmedName, recoverData, privateKeyHex)
            if (!success) {
                Log.e(TAG, "newUser API failed")
                return null
            }

            CreateRecoverResult(
                privateKeyHex = privateKeyHex,
                recoverCode = recoverCode.code,
                mnemonicPhrase = mnemonic
            )
        } catch (e: Exception) {
            Log.e(TAG, "createRecover failed", e)
            null
        }
    }

    fun checkBeamioAccountAvailable(accountName: String): Boolean {
        return try {
            val conn = URL(CONET_RPC).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val data = encodeIsAccountNameAvailable(accountName)
            val payload = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$ACCOUNT_REGISTRY","data":"0x$data"},"latest"],"id":1}"""
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val json = JSONObject(body)
            val result = json.optString("result", "")
            if (result.isEmpty() || result == "0x") return true
            val hex = result.removePrefix("0x").padStart(64, '0').takeLast(64)
            BigInteger(hex, 16) == BigInteger.ONE
        } catch (e: Exception) {
            Log.e(TAG, "checkBeamioAccountAvailable failed", e)
            true
        }
    }

    private fun encodeIsAccountNameAvailable(accountName: String): String {
        val selector = Numeric.toHexStringNoPrefix(Hash.sha3("isAccountNameAvailable(string)".toByteArray(Charsets.UTF_8))).take(8)
        val encodedParam = encodeAbiString(accountName)
        return selector + encodedParam
    }

    private fun encodeAbiString(s: String): String {
        val bytes = s.toByteArray(Charsets.UTF_8)
        val offset = "0000000000000000000000000000000000000000000000000000000000000020"
        val lenHex = bytes.size.toString(16).padStart(64, '0')
        val dataPadded = (bytes.size + 31) / 32 * 32
        val dataHex = bytes.joinToString("") { "%02x".format(it) }.padEnd(dataPadded * 2, '0')
        return offset + lenHex + dataHex
    }

    private fun newUser(accountName: String, recoverData: JSONArray, privateKeyHex: String): Boolean {
        return try {
            val keyPair = org.web3j.crypto.ECKeyPair.create(BigInteger(privateKeyHex.removePrefix("0x"), 16))
            val address = "0x" + Keys.getAddress(keyPair)
            val sig = Sign.signPrefixedMessage(address.toByteArray(Charsets.UTF_8), keyPair)
            val signMessage = "0x" +
                Numeric.toHexStringNoPrefix(sig.r).padStart(64, '0') +
                Numeric.toHexStringNoPrefix(sig.s).padStart(64, '0') +
                (sig.v[0].toInt() and 0xFF).toString(16).padStart(2, '0')

            val body = JSONObject().apply {
                put("accountName", accountName)
                put("recover", recoverData)
                put("wallet", address)
                put("signMessage", signMessage)
            }

            val conn = URL("$BEAMIO_API/api/addUser").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 60000
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val ok = conn.responseCode in 200..299
            conn.disconnect()
            ok
        } catch (e: Exception) {
            Log.e(TAG, "newUser failed", e)
            false
        }
    }
}
