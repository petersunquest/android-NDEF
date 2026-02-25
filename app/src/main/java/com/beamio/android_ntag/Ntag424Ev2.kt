package com.beamio.android_ntag

import android.nfc.tech.IsoDep
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class Ev2Session(
    val ti: ByteArray,
    val sesAuthEncKey: ByteArray,
    val sesAuthMacKey: ByteArray,
    var cmdCtr: Int = 0
)

class Ntag424Ev2(private val isoDep: IsoDep) {
    class NtagError(message: String) : Exception(message)

    fun authenticateEV2First(keyNo: Int, key: ByteArray): Ev2Session {
        require(key.size == 16) { "key length must be 16" }
        val apdu1 = byteArrayOf(0x90.toByte(), 0x71, 0x00, 0x00, 0x02, keyNo.toByte(), 0x00, 0x00)
        val (data1, sw1) = splitStatus(transceive(apdu1))
        if (sw1 != 0x91AF) throw NtagError("EV2First step1 bad status: 0x${sw1.toString(16)}")
        if (data1.size != 16) throw NtagError("EV2First step1 bad length")

        val rndB = Crypto.aesCbcDecrypt(data1, key, ByteArray(16))
        val rndA = Crypto.randomBytes(16)
        val rndBp = rotl1(rndB)
        val encAB = Crypto.aesCbcEncrypt(rndA + rndBp, key, data1)

        val apdu2 = byteArrayOf(0x90.toByte(), 0xAF.toByte(), 0x00, 0x00, 0x20) + encAB + byteArrayOf(0x00)
        val (data2, sw2) = splitStatus(transceive(apdu2))
        if (sw2 != 0x9100) throw NtagError("EV2First step2 bad status: 0x${sw2.toString(16)}")
        if (data2.size < 32) throw NtagError("EV2First step2 bad length")

        val dec = Crypto.aesCbcDecrypt(data2.copyOfRange(0, 32), key, encAB.copyOfRange(16, 32))
        val ti = dec.copyOfRange(0, 4)
        val rndAp = dec.copyOfRange(4, 20)
        if (!rndAp.contentEquals(rotl1(rndA))) {
            throw NtagError("EV2First rndA mismatch")
        }

        val (sv1, sv2) = buildSessionVectorsEV2(rndA, rndB)
        val sesEnc = Crypto.aesCmac(key, sv1)
        val sesMac = Crypto.aesCmac(key, sv2)
        return Ev2Session(ti = ti, sesAuthEncKey = sesEnc, sesAuthMacKey = sesMac, cmdCtr = 0)
    }

    fun changeKey(
        session: Ev2Session,
        changingKeyNo: Int,
        newKey: ByteArray,
        newKeyVersion: Int = 0x01
    ) {
        require(newKey.size == 16) { "new key length must be 16" }
        val cmdCtr = session.cmdCtr
        session.cmdCtr += 1

        var cmdDataPlain = newKey + byteArrayOf(newKeyVersion.toByte(), 0x80.toByte())
        while (cmdDataPlain.size < 32) cmdDataPlain += byteArrayOf(0x00)

        val ivInput = ivInput(byteArrayOf(0xA5.toByte(), 0x5A), session.ti, cmdCtr)
        val ivc = Crypto.aesEcbEncrypt(ivInput, session.sesAuthEncKey)
        val encCmdData = Crypto.aesCbcEncrypt(cmdDataPlain, session.sesAuthEncKey, ivc)

        val cmd = 0xC4
        val cmdCtrLE = byteArrayOf((cmdCtr and 0xFF).toByte(), ((cmdCtr shr 8) and 0xFF).toByte())
        val macInput = byteArrayOf(cmd.toByte()) + cmdCtrLE + session.ti + byteArrayOf(changingKeyNo.toByte()) + encCmdData
        val macT = Crypto.aesCmac(session.sesAuthMacKey, macInput).copyOfRange(8, 16)

        val lc = (1 + encCmdData.size + macT.size).toByte()
        val apdu = byteArrayOf(0x90.toByte(), 0xC4.toByte(), 0x00, 0x00, lc, changingKeyNo.toByte()) + encCmdData + macT + byteArrayOf(0x00)
        val (_, sw) = splitStatus(transceive(apdu))
        if (sw != 0x9100) throw NtagError("ChangeKey bad status: 0x${sw.toString(16)}")
    }

    fun autoDetectNdefFileNo(): Int {
        val files = getFileIDs()
        if (files.isEmpty()) throw NtagError("no file IDs")
        for (f in files) {
            try {
                val d = readDataPlain(f, 0, 16)
                if (d.size < 4) continue
                val nlen = ((d[0].toInt() and 0xFF) shl 8) or (d[1].toInt() and 0xFF)
                val hdr = d[2].toInt() and 0xFF
                val looks = hdr == 0xD1 || hdr == 0xD2 || hdr == 0x91 || hdr == 0x51
                val reasonable = nlen in 0..8191
                if (looks && reasonable) return f
            } catch (_: Exception) {
                // probe next file
            }
        }
        throw NtagError("cannot auto-detect NDEF file")
    }

    fun patchSdmFileSettingsFromLiveNdef(
        fileNo: Int,
        expectedEncHexLen: Int = 64,
        expectedCtrHexLen: Int = 6,
        expectedMacHexLen: Int = 16,
        readBytes: Int = 300
    ): ByteArray {
        val fileHead = readDataPlain(fileNo, 0, readBytes)
        val offsets = computeOffsetsFromNdefFileBytes(fileHead, expectedEncHexLen, expectedCtrHexLen, expectedMacHexLen)
        val raw = getFileSettingsPlain(fileNo)
        return patchFileSettingsRaw(raw, offsets)
    }

    fun changeFileSettings(session: Ev2Session, fileNo: Int, fileSettings: ByteArray) {
        var plain = fileSettings + byteArrayOf(0x80.toByte())
        while (plain.size % 16 != 0) plain += byteArrayOf(0x00)
        sendSecureCommand(session, 0x5F, byteArrayOf(fileNo.toByte()), plain)
    }

    fun deriveSdmKey(masterKey: ByteArray, uid: ByteArray): ByteArray {
        val domain = "BEAMIO_SDM_V1".toByteArray(Charsets.UTF_8)
        val msg = domain + byteArrayOf(0x00) + uid
        return Crypto.aesCmac(masterKey, msg).copyOfRange(0, 16)
    }

    private fun sendSecureCommand(session: Ev2Session, cmd: Int, header: ByteArray, cmdDataPlain: ByteArray): ByteArray {
        val cmdCtr = session.cmdCtr
        session.cmdCtr += 1
        val ivInput = ivInput(byteArrayOf(0xA5.toByte(), 0x5A), session.ti, cmdCtr)
        val ivc = Crypto.aesEcbEncrypt(ivInput, session.sesAuthEncKey)
        val encCmdData = Crypto.aesCbcEncrypt(cmdDataPlain, session.sesAuthEncKey, ivc)
        val cmdCtrLE = byteArrayOf((cmdCtr and 0xFF).toByte(), ((cmdCtr shr 8) and 0xFF).toByte())
        val macInput = byteArrayOf(cmd.toByte()) + cmdCtrLE + session.ti + header + encCmdData
        val macT = Crypto.aesCmac(session.sesAuthMacKey, macInput).copyOfRange(8, 16)
        val lc = (header.size + encCmdData.size + macT.size).toByte()
        val apdu = byteArrayOf(0x90.toByte(), cmd.toByte(), 0x00, 0x00, lc) + header + encCmdData + macT + byteArrayOf(0x00)
        val (data, sw) = splitStatus(transceive(apdu))
        if (sw != 0x9100) throw NtagError("secure cmd 0x${cmd.toString(16)} bad status: 0x${sw.toString(16)}")
        return data
    }

    private fun getFileIDs(): List<Int> = sendPlainCommand(0x6F).map { it.toInt() and 0xFF }

    private fun getFileSettingsPlain(fileNo: Int): ByteArray = sendPlainCommand(0xF5, byteArrayOf(fileNo.toByte()))

    private fun readDataPlain(fileNo: Int, offset: Int, length: Int): ByteArray {
        val data = byteArrayOf(fileNo.toByte()) + le3(offset) + le3(length)
        return sendPlainCommand(0xBD, data)
    }

    private fun sendPlainCommand(cmd: Int, data: ByteArray = byteArrayOf()): ByteArray {
        val apdu = byteArrayOf(0x90.toByte(), cmd.toByte(), 0x00, 0x00, data.size.toByte()) + data + byteArrayOf(0x00)
        val (d, sw) = splitStatus(transceive(apdu))
        if (sw != 0x9100) throw NtagError("plain cmd 0x${cmd.toString(16)} bad status: 0x${sw.toString(16)}")
        return d
    }

    private fun transceive(apdu: ByteArray): ByteArray = isoDep.transceive(apdu)

    private fun splitStatus(resp: ByteArray): Pair<ByteArray, Int> {
        if (resp.size < 2) throw NtagError("bad response length")
        val sw1 = resp[resp.size - 2].toInt() and 0xFF
        val sw2 = resp[resp.size - 1].toInt() and 0xFF
        val sw = (sw1 shl 8) or sw2
        return Pair(resp.copyOfRange(0, resp.size - 2), sw)
    }

    private data class SdmOffsets(
        val encOffset: Int,
        val encLen: Int,
        val ctrOffset: Int,
        val macOffset: Int,
        val macLen: Int
    )

    private fun computeOffsetsFromNdefFileBytes(fileBytes: ByteArray, expectedEncLen: Int, expectedCtrLen: Int, expectedMacLen: Int): SdmOffsets {
        if (fileBytes.size < 6) throw NtagError("ndef parse: too short")
        val nlen = ((fileBytes[0].toInt() and 0xFF) shl 8) or (fileBytes[1].toInt() and 0xFF)
        if (nlen <= 0) throw NtagError("ndef parse: NLEN=0")
        val (payloadStartInFile, payloadBytes) = parseFirstUriRecord(fileBytes, 2)
        if (payloadBytes.size < 2) throw NtagError("ndef parse: payload short")
        val uriBytes = payloadBytes.copyOfRange(1, payloadBytes.size)
        val eValueRel = findValueStart(uriBytes, "e") ?: throw NtagError("cannot locate e=")
        val cValueRel = findValueStart(uriBytes, "c") ?: throw NtagError("cannot locate c=")
        val mValueRel = findValueStart(uriBytes, "m") ?: throw NtagError("cannot locate m=")
        if (eValueRel + expectedEncLen > uriBytes.size) throw NtagError("e len out of range")
        if (cValueRel + expectedCtrLen > uriBytes.size) throw NtagError("c len out of range")
        if (mValueRel + expectedMacLen > uriBytes.size) throw NtagError("m len out of range")
        return SdmOffsets(
            encOffset = payloadStartInFile + 1 + eValueRel,
            encLen = expectedEncLen,
            ctrOffset = payloadStartInFile + 1 + cValueRel,
            macOffset = payloadStartInFile + 1 + mValueRel,
            macLen = expectedMacLen
        )
    }

    private fun parseFirstUriRecord(fileBytes: ByteArray, recordStart: Int): Pair<Int, ByteArray> {
        var i = recordStart
        if (i >= fileBytes.size) throw NtagError("recordStart out of range")
        val hdr = fileBytes[i].toInt() and 0xFF
        i += 1
        val sr = (hdr and 0x10) != 0
        val il = (hdr and 0x08) != 0
        val tnf = hdr and 0x07
        if (tnf != 0x01) throw NtagError("TNF not well-known")
        if (i >= fileBytes.size) throw NtagError("missing typeLen")
        val typeLen = fileBytes[i].toInt() and 0xFF
        i += 1
        if (!sr) throw NtagError("non-SR record unsupported")
        if (i >= fileBytes.size) throw NtagError("missing payloadLen")
        val payloadLen = fileBytes[i].toInt() and 0xFF
        i += 1
        val idLen = if (il) {
            if (i >= fileBytes.size) throw NtagError("missing idLen")
            val v = fileBytes[i].toInt() and 0xFF
            i += 1
            v
        } else 0
        if (i + typeLen > fileBytes.size) throw NtagError("type out of range")
        val type = String(fileBytes.copyOfRange(i, i + typeLen), Charsets.UTF_8)
        i += typeLen
        if (type != "U") throw NtagError("not URI record")
        if (il) {
            if (i + idLen > fileBytes.size) throw NtagError("id out of range")
            i += idLen
        }
        val payloadStart = i
        if (i + payloadLen > fileBytes.size) {
            if (i >= fileBytes.size) throw NtagError("payload not in buffer")
            return Pair(payloadStart, fileBytes.copyOfRange(i, fileBytes.size))
        }
        return Pair(payloadStart, fileBytes.copyOfRange(i, i + payloadLen))
    }

    private fun findValueStart(uriBytes: ByteArray, key: String): Int? {
        val needle = "$key=".toByteArray(Charsets.UTF_8)
        if (needle.isEmpty()) return null
        for (idx in 0..(uriBytes.size - needle.size)) {
            var ok = true
            for (j in needle.indices) {
                if (uriBytes[idx + j] != needle[j]) {
                    ok = false
                    break
                }
            }
            if (ok) return idx + needle.size
        }
        return null
    }

    private fun patchFileSettingsRaw(raw: ByteArray, offsets: SdmOffsets): ByteArray {
        val baseLen = 7
        val minSdmLen = 2 + 2 + 3 * 8
        val minTotal = baseLen + minSdmLen
        if (raw.size < minTotal) throw NtagError("unexpected file settings layout")
        val out = raw.copyOf()
        var p = baseLen
        p += 2
        p += 2
        val uidOffsetIdx = p
        p += 3
        val ctrOffsetIdx = p
        p += 3
        p += 3
        p += 3
        val encOffsetIdx = p
        p += 3
        val encLenIdx = p
        p += 3
        val macOffsetIdx = p
        p += 3
        val macLenIdx = p
        p += 3
        out.fill(0xFF.toByte(), uidOffsetIdx, uidOffsetIdx + 3)
        le3(offsets.ctrOffset).copyInto(out, ctrOffsetIdx)
        le3(offsets.encOffset).copyInto(out, encOffsetIdx)
        le3(offsets.encLen).copyInto(out, encLenIdx)
        le3(offsets.macOffset).copyInto(out, macOffsetIdx)
        le3(offsets.macLen).copyInto(out, macLenIdx)
        return out
    }

    private fun le3(v: Int): ByteArray = byteArrayOf((v and 0xFF).toByte(), ((v shr 8) and 0xFF).toByte(), ((v shr 16) and 0xFF).toByte())

    private fun buildSessionVectorsEV2(rndA: ByteArray, rndB: ByteArray): Pair<ByteArray, ByteArray> {
        val a1514 = rndA.copyOfRange(14, 16)
        val a138 = rndA.copyOfRange(8, 14)
        val b1510 = rndB.copyOfRange(10, 16)
        val b90 = rndB.copyOfRange(0, 10)
        val a70 = rndA.copyOfRange(0, 8)
        val x = ByteArray(6) { i -> (a138[i].toInt() xor b1510[i].toInt()).toByte() }
        val context = a1514 + x + b90 + a70
        val prefix1 = byteArrayOf(0xA5.toByte(), 0x5A, 0x00, 0x01, 0x00, 0x80.toByte())
        val prefix2 = byteArrayOf(0x5A, 0xA5.toByte(), 0x00, 0x01, 0x00, 0x80.toByte())
        return Pair(prefix1 + context, prefix2 + context)
    }

    private fun ivInput(label: ByteArray, ti: ByteArray, cmdCtr: Int): ByteArray {
        return label + ti + byteArrayOf((cmdCtr and 0xFF).toByte(), ((cmdCtr shr 8) and 0xFF).toByte()) + ByteArray(8)
    }

    private fun rotl1(d: ByteArray): ByteArray {
        if (d.isEmpty()) return d
        return d.copyOfRange(1, d.size) + byteArrayOf(d[0])
    }
}

object Crypto {
    private val random = SecureRandom()

    fun randomBytes(n: Int): ByteArray = ByteArray(n).also { random.nextBytes(it) }

    fun aesCbcEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(plaintext)
    }

    fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(ciphertext)
    }

    fun aesEcbEncrypt(block16: ByteArray, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(block16)
    }

    fun aesCmac(key: ByteArray, msg: ByteArray): ByteArray {
        require(key.size == 16) { "bad cmac key length" }
        val l = aesEcbEncrypt(ByteArray(16), key)
        val (k1, k2) = cmacSubkeys(l)
        val n = maxOf(1, kotlin.math.ceil(msg.size / 16.0).toInt())
        val lastComplete = (msg.size % 16 == 0) && msg.isNotEmpty()
        val mLast = if (lastComplete) {
            xor16(msg.copyOfRange((n - 1) * 16, n * 16), k1)
        } else {
            val start = (n - 1) * 16
            val last = if (start < msg.size) msg.copyOfRange(start, msg.size) else byteArrayOf()
            val pad = ByteArray(16)
            last.copyInto(pad)
            pad[last.size] = 0x80.toByte()
            xor16(pad, k2)
        }
        var x = ByteArray(16)
        for (i in 0 until (n - 1)) {
            val block = msg.copyOfRange(i * 16, (i + 1) * 16)
            x = aesEcbEncrypt(xor16(x, block), key)
        }
        return aesEcbEncrypt(xor16(x, mLast), key)
    }

    private fun cmacSubkeys(l: ByteArray): Pair<ByteArray, ByteArray> {
        val rb: Byte = 0x87.toByte()
        fun leftShift(input: ByteArray): ByteArray {
            val out = ByteArray(16)
            var carry = 0
            for (i in 15 downTo 0) {
                val b = input[i].toInt() and 0xFF
                out[i] = ((b shl 1) or carry).toByte()
                carry = if ((b and 0x80) != 0) 1 else 0
            }
            return out
        }
        var k1 = leftShift(l)
        if ((l[0].toInt() and 0x80) != 0) k1[15] = (k1[15].toInt() xor rb.toInt()).toByte()
        var k2 = leftShift(k1)
        if ((k1[0].toInt() and 0x80) != 0) k2[15] = (k2[15].toInt() xor rb.toInt()).toByte()
        return Pair(k1, k2)
    }

    private fun xor16(a: ByteArray, b: ByteArray): ByteArray {
        val out = ByteArray(16)
        for (i in 0 until 16) out[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        return out
    }
}
