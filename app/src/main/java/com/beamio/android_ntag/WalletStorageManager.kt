package com.beamio.android_ntag

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 安全存储 WALLET_PRIVATE_KEY，使用 EncryptedSharedPreferences（基于 Android Keystore）。
 */
object WalletStorageManager {
    private const val PREFS_NAME = "beamio_wallet_secure"
    private const val KEY_PRIVATE_KEY = "wallet_private_key"

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun savePrivateKey(context: Context, privateKeyHex: String) {
        getEncryptedPrefs(context).edit().putString(KEY_PRIVATE_KEY, privateKeyHex.trim().removePrefix("0x")).apply()
    }

    fun loadPrivateKey(context: Context): String? {
        val raw = getEncryptedPrefs(context).getString(KEY_PRIVATE_KEY, null) ?: return null
        val trimmed = raw.trim()
        if (trimmed.length != 64 || !trimmed.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
        return trimmed
    }

    fun hasStoredWallet(context: Context): Boolean = loadPrivateKey(context) != null

    fun clear(context: Context) {
        getEncryptedPrefs(context).edit().remove(KEY_PRIVATE_KEY).apply()
    }
}
