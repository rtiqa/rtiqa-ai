package com.rtiqa.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Interface contract for encrypted key-value storage.
 */
interface SecurityManager {
    fun putEncryptedString(key: String, value: String)
    fun getEncryptedString(key: String, defaultValue: String? = null): String?
    fun removeKey(key: String)
    fun clearAll()
}

/**
 * Encrypted Storage implementation leveraging Android KeyStore and AES-256 GCM.
 */
class EncryptedSecurityManager(context: Context) : SecurityManager {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun putEncryptedString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getEncryptedString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val SECURE_PREFS_NAME = "rtiqa_secure_prefs"
    }
}
