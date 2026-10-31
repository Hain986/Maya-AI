package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference

object LanguageDetector {
    private val HINGLISH_MARKERS = setOf(
        "hai", "hain", "ho", "kya", "kyun", "kaise", "nahi", "haan",
        "mujhe", "tumhe", "aap", "mera", "meri", "mere", "yaar", "bhai",
        "accha", "theek", "matlab", "karna", "karo", "raha", "rahe",
        "chahiye", "batao", "suno", "dekho", "abhi", "phir", "lekin",
        "bahut", "thoda", "kuch", "sab", "wala", "wali", "wale", "aur",
        "toh", "agar", "nahi", "mera", "meri", "hum", "uska", "unki"
    )

    fun detect(text: String): LanguagePreference {
        if (text.isBlank()) return LanguagePreference.AUTO

        // Check for Devanagari script (Hindi Unicode block: U+0900..U+097F)
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) return LanguagePreference.HINDI

        val cleanText = text.lowercase().replace(Regex("[^a-z'\\s]"), " ")
        val words = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return LanguagePreference.AUTO

        var hitCount = 0
        for (word in words) {
            if (word in HINGLISH_MARKERS) {
                hitCount++
            }
        }

        val ratio = hitCount.toDouble() / words.size.toDouble()
        return if (words.size <= 4) {
            if (hitCount >= 1) LanguagePreference.HINGLISH else LanguagePreference.ENGLISH
        } else {
            if (hitCount >= 2 || ratio >= 0.15) LanguagePreference.HINGLISH else LanguagePreference.ENGLISH
        }
    }
}
