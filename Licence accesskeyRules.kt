package com.Lia.assistant.license

data class LicenseInfo(
    val active: Boolean,
    val plan: String,
    val expiresAt: Long,
    val statusText: String,
    val blocked: Boolean,
    val hasRememberedKey: Boolean
)

object AccessKeyRules {
    fun evaluate(plan: String, expiresAt: Long, blocked: Boolean, hasKey: Boolean): LicenseInfo {
        if (!hasKey) {
            return LicenseInfo(false, "None", 0L, "No access key entered", false, false)
        }
        if (blocked) {
            return LicenseInfo(false, plan, expiresAt, "Key has been blocked by admin", true, true)
        }
        val now = System.currentTimeMillis()
        if (expiresAt > 0 && now > expiresAt) {
            return LicenseInfo(false, plan, expiresAt, "License expired", false, true)
        }
        return LicenseInfo(true, plan, expiresAt, "Active ($plan plan)", false, true)
    }
}
