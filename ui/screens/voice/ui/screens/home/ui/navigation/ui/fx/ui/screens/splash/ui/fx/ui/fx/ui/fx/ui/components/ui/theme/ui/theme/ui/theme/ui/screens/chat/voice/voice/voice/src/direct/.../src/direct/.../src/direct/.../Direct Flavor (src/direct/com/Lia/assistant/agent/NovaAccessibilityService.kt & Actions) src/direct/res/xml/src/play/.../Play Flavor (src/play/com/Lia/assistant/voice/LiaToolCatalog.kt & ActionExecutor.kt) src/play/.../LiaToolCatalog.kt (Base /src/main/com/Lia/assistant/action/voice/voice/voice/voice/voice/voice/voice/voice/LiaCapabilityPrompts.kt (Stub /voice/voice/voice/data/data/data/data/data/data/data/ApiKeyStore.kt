package com.Lia.assistant.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ApiKeyStore {
    private const val PREFS_NAME = "nova_secure_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    private fun getPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard prefs if hardware-backed keystore fails on specific devices
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getKey(context: Context): String? {
        return getPrefs(context).getString(KEY_GEMINI_API_KEY, null)?.takeIf { it.isNotBlank() }
    }

    fun saveKey(context: Context, key: String) {
        val trimmed = key.trim()
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
    }

    fun hasKey(context: Context): Boolean {
        return !getKey(context).isNullOrBlank()
    }
}
