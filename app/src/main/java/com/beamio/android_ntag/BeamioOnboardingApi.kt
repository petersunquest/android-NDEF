package com.beamio.android_ntag

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Keys
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URLEncoder

/**
 * POS terminal onboarding HTTP + CONET registry (align iOS `BeamioAPIClient` / `BeamioConstants`).
 */
object BeamioOnboardingApi {
    private const val TAG = "BeamioOnboarding"
    private const val BEAMIO_API = "https://beamio.app"
    /** iOS `BeamioConstants.beamioAccountRegistryAddress` */
    private const val ACCOUNT_REGISTRY = "0x4afaca09cf8307070a83836223Ae129073eC92e5"
    private const val CONET_RPC = "https://rpc1.conet.network"

    /**
     * `GET /api/search-users-by-card-owner-or-admin` — program card + issuers; no device wallet yet.
     * Query param spelling is `keyward` (server contract).
     */
    fun searchUsersByCardOwnerOrAdminSync(keyward: String, extraCardAddresses: List<String>): List<TerminalProfile> {
        val kw = keyward.trim()
        if (kw.length < 2) return emptyList()
        return try {
            val enc = URLEncoder.encode(kw, "UTF-8")
            val extra = extraCardAddresses
                .map { it.trim() }
                .filter { it.startsWith("0x", ignoreCase = true) && it.length >= 42 }
                .joinToString(",")
            val extraQ = if (extra.isNotEmpty()) "&extraCardAddresses=${URLEncoder.encode(extra, "UTF-8")}" else ""
            val url = java.net.URL("$BEAMIO_API/api/search-users-by-card-owner-or-admin?keyward=$enc$extraQ")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() }
                .orEmpty()
            conn.disconnect()
            if (code !in 200..299) {
                Log.w(TAG, "search HTTP $code ${body.take(200)}")
                return emptyList()
            }
            val root = JSONObject(body)
            val results = root.optJSONArray("results") ?: return emptyList()
            val out = ArrayList<TerminalProfile>(results.length())
            for (i in 0 until results.length()) {
                val o = results.optJSONObject(i) ?: continue
                out.add(
                    TerminalProfile(
                        accountName = o.optString("username").takeIf { it.isNotEmpty() }
                            ?: o.optString("accountName").takeIf { it.isNotEmpty() },
                        first_name = o.optString("first_name").takeIf { it.isNotEmpty() },
                        last_name = o.optString("last_name").takeIf { it.isNotEmpty() },
                        image = o.optString("image").takeIf { it.isNotEmpty() },
                        address = o.optString("address").takeIf { it.isNotEmpty() },
                    ),
                )
            }
            out
        } catch (e: Exception) {
            Log.w(TAG, "search failed", e)
            emptyList()
        }
    }

    /** `isAccountNameAvailable(string)` on CONET AccountRegistry; returns null on RPC/parse failure.
     * Must use the same bytes as registration: registry hashes `keccak256(bytes(name))` case-sensitively.
     * Do not lowercase — e.g. `rrr7_POS_0001` taken does not imply `rrr7_pos_0001` taken.
     */
    fun isBeamioAccountNameAvailableSync(normalizedHandle: String): Boolean? {
        val name = BeamioTagRules.normalizeInput(normalizedHandle)
        if (name.isEmpty() || !BeamioTagRules.ALLOWED_REGEX.matches(name)) return false
        val data = encodeIsAccountNameAvailableCalldata(name) ?: return null
        return try {
            val reqBody = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$ACCOUNT_REGISTRY","data":"$data"},"latest"],"id":1}"""
            val conn = java.net.URL(CONET_RPC).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            conn.outputStream.use { it.write(reqBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val json = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() }
                .orEmpty()
            conn.disconnect()
            val root = JSONObject(json)
            if (root.optJSONObject("error") != null) return null
            val result = root.optString("result", "")
            if (result.isEmpty() || result == "0x") return null
            decodeBoolAbiWord(result) ?: return null
        } catch (e: Exception) {
            Log.w(TAG, "isAvailable eth_call failed", e)
            null
        }
    }

    private fun encodeIsAccountNameAvailableCalldata(name: String): String? {
        return try {
            val fn = Function(
                "isAccountNameAvailable",
                listOf<Type<*>>(Utf8String(name)),
                listOf(object : TypeReference<Bool>() {}),
            )
            FunctionEncoder.encode(fn)
        } catch (e: Exception) {
            Log.w(TAG, "encode isAccountNameAvailable failed", e)
            null
        }
    }

    private fun decodeBoolAbiWord(hex: String): Boolean? {
        val raw = hex.trim().removePrefix("0x")
        if (raw.length < 64) return null
        val w = raw.substring(raw.length - 64)
        return try {
            BigInteger(w, 16).signum() != 0
        } catch (_: Exception) {
            null
        }
    }

    /**
     * `POST /api/addUser` — POS path may use empty `recover` (iOS `registerBeamioAccount`).
     * @param wallet checksummed `0x` address
     */
    fun addUserSync(accountName: String, walletChecksummed: String, signMessage0x: String): String? {
        return try {
            val body = JSONObject().apply {
                put("accountName", BeamioTagRules.normalizeInput(accountName))
                put("wallet", walletChecksummed)
                put("signMessage", signMessage0x)
                put("recover", JSONArray())
            }.toString()
            val url = java.net.URL("$BEAMIO_API/api/addUser")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 120000
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() }
                .orEmpty()
            conn.disconnect()
            val root = try {
                JSONObject(resp)
            } catch (_: Exception) {
                return if (code in 200..299) null else "Invalid response (HTTP $code)"
            }
            val err = root.optString("error").takeIf { it.isNotEmpty() }
            if (err != null) return err
            if (code !in 200..299) {
                return root.optString("message").takeIf { it.isNotEmpty() } ?: "Request failed (HTTP $code)"
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "addUser failed", e)
            e.message ?: "Network error"
        }
    }

    fun toChecksumAddress0x(lowerOrMixed: String): String {
        val t = lowerOrMixed.trim()
        val with0x = if (t.startsWith("0x", ignoreCase = true)) t else "0x$t"
        return Keys.toChecksumAddress(with0x)
    }
}
