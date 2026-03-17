package com.beamio.android_ntag

import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * 全局设备 POS 钱包。Onboarding 完成后创建并持久化，后续 top-up、charge 等均使用此钱包签名。
 * 不维护多份钱包；top-up 等函数不自行维护设备钱包。
 */
object BeamioWeb3Wallet {
    private const val BASE_CARD_FACTORY = "0xfB5E3F2AbFe24DC17970d78245BeF56aAE8cb71a"
    private const val BASE_CHAIN_ID = 8453L

    private var keyPair: ECKeyPair? = null

    fun init(privateKeyHex: String) {
        val pk = privateKeyHex.trim().removePrefix("0x")
        if (pk.length != 64) throw IllegalArgumentException("Invalid private key length")
        keyPair = ECKeyPair.create(BigInteger(pk, 16))
    }

    fun isInitialized(): Boolean = keyPair != null

    /** 从私钥推导的 EOA 地址，用于收款（payee） */
    fun getAddress(): String {
        val kp = keyPair ?: throw IllegalStateException("Wallet not initialized")
        return "0x" + Keys.getAddress(kp)
    }

    /**
     * 对 ExecuteForAdmin 进行 EIP-712 离线签名。
     * @param cardAddr 卡地址
     * @param data executeForAdmin 的 data（hex）
     * @param deadline 过期时间戳
     * @param nonce 0x 开头的 hex
     * @return adminSignature 十六进制签名，格式与 ethers 一致
     */
    fun signExecuteForAdmin(cardAddr: String, data: String, deadline: Long, nonce: String): String {
        val kp = keyPair ?: throw IllegalStateException("Wallet not initialized")
        val dataBytes = Numeric.hexStringToByteArray(if (data.startsWith("0x")) data else "0x$data")
        val dataHash = org.web3j.crypto.Hash.sha3(dataBytes)
        val dataHashHex = Numeric.toHexString(dataHash)
        val nonceHex = if (nonce.startsWith("0x")) nonce else "0x$nonce"
        val json = """
            {
                "types": {
                    "EIP712Domain": [
                        {"name": "name", "type": "string"},
                        {"name": "version", "type": "string"},
                        {"name": "chainId", "type": "uint256"},
                        {"name": "verifyingContract", "type": "address"}
                    ],
                    "ExecuteForAdmin": [
                        {"name": "cardAddress", "type": "address"},
                        {"name": "dataHash", "type": "bytes32"},
                        {"name": "deadline", "type": "uint256"},
                        {"name": "nonce", "type": "bytes32"}
                    ]
                },
                "primaryType": "ExecuteForAdmin",
                "domain": {
                    "name": "BeamioUserCardFactory",
                    "version": "1",
                    "chainId": $BASE_CHAIN_ID,
                    "verifyingContract": "$BASE_CARD_FACTORY"
                },
                "message": {
                    "cardAddress": "$cardAddr",
                    "dataHash": "$dataHashHex",
                    "deadline": $deadline,
                    "nonce": "$nonceHex"
                }
            }
        """.trimIndent()
        val sig = Sign.signTypedData(json, kp)
        return "0x" +
            Numeric.toHexStringNoPrefix(sig.r).padStart(64, '0') +
            Numeric.toHexStringNoPrefix(sig.s).padStart(64, '0') +
            (sig.v[0].toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
