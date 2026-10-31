package com.Lia.assistant.license

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessKeyManager {
    private const val PREFS_NAME = "lia_access_key_prefs"
    private const val KEY_CODE = "access_key_code"
    private const val KEY_PLAN = "access_plan"
    private const val KEY_EXPIRES = "access_expires"
    private const val KEY_BLOCKED = "access_blocked"

    private val _licenseInfo = MutableStateFlow(LicenseInfo(false, "None", 0L, "Initializing", false, false))
    val licenseInfo: StateFlow<LicenseInfo> = _licenseInfo.asStateFlow()

    fun refreshInfo(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_CODE, null)
        val plan = prefs.getString(KEY_PLAN, "Standard") ?: "Standard"
        val expires = prefs.getLong(KEY_EXPIRES, 0L)
        val blocked = prefs.getBoolean(KEY_BLOCKED, false)

        val info = AccessKeyRules.evaluate(plan, expires, blocked, !code.isNullOrBlank())
        _licenseInfo.value = info
    }

    fun activate(context: Context, key: String): Boolean {
        val trimmed = key.trim()
        if (trimmed.isBlank()) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CODE, trimmed)
            .putString(KEY_PLAN, "Pro Direct")
            .putLong(KEY_EXPIRES, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)
            .putBoolean(KEY_BLOCKED, false)
            .apply()

        refreshInfo(context)
        return true
    }
}
