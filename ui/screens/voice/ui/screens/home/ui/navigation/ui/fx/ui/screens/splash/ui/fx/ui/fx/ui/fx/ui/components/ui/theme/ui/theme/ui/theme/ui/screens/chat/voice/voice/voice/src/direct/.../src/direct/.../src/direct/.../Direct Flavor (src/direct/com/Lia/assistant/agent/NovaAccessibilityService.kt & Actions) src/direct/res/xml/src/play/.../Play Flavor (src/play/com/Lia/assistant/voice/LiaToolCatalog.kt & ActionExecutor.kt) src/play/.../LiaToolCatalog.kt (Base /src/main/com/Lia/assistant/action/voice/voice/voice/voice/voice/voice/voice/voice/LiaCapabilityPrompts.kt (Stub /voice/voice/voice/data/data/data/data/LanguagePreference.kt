package com.Lia.assistant.data

enum class LanguagePreference(val displayName: String, val description: String) {
    AUTO("Auto-Detect", "Follows whatever language you speak seamlessly"),
    HINDI("Hindi", "Pure Devanagari Hindi responses"),
    HINGLISH("Hinglish", "Natural conversational Hindi-English mix"),
    ENGLISH("English", "Fluent English responses")
}
