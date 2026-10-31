package com.Lia.assistant.voice

object ForgeIntent {
    private val buildVerbs = setOf("bana", "banao", "build", "create", "make", "design", "develop")

    fun extract(text: String): String? {
        val lower = text.lowercase()
        val hasBuildVerb = buildVerbs.any { lower.contains(it) }
        val hasWebTerm = lower.contains("website") || lower.contains("web page") || lower.contains("landing page") || lower.contains("site") || lower.contains("cafe") || lower.contains("portfolio")

        if (hasBuildVerb && hasWebTerm) {
            return text.trim()
        }
        return null
    }
}
