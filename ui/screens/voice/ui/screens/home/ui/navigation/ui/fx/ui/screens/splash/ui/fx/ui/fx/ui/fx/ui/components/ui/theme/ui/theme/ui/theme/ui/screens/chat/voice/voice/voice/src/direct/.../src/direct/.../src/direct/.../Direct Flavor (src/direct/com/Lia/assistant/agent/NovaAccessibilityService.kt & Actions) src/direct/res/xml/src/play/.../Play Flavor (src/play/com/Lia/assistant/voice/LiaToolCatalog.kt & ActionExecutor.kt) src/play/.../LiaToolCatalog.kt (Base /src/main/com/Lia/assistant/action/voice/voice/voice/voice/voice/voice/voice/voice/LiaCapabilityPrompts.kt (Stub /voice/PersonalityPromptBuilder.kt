package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object PersonalityPromptBuilder {
    fun build(
        personality: Personality,
        languagePreference: LanguagePreference,
        detectedLanguage: LanguagePreference?,
        recap: String?,
        assistantName: String
    ): String {
        val parts = mutableListOf<String>()

        // (a) Base prompt
        parts.add(
            "You are $assistantName, a lifelike 3D human AI companion living on the user's phone. " +
                    "Keep replies concise, conversational, and natural for spoken voice interactions."
        )

        // (b) Capability instructions placeholder (populated by flavors)
        parts.add(LiaCapabilityPrompts.toolInstruction(assistantName))

        // (c) Personality instruction
        parts.add(personality.systemInstruction)

        // (d) Language policy
        val activeLang = if (languagePreference == LanguagePreference.AUTO) {
            detectedLanguage ?: LanguagePreference.HINGLISH
        } else {
            languagePreference
        }

        when (activeLang) {
            LanguagePreference.HINDI -> {
                parts.add("Language Policy: Respond strictly in pure Devanagari Hindi. Never translate user terms awkwardly.")
            }
            LanguagePreference.HINGLISH -> {
                parts.add("Language Policy: Respond naturally in conversational Hinglish (Hindi written in Latin script mixed smoothly with English). Match the user's casual, warm tone.")
            }
            LanguagePreference.ENGLISH -> {
                parts.add("Language Policy: Respond in fluent, articulate English.")
            }
            LanguagePreference.AUTO -> {
                parts.add("Language Policy: Match the user's language turn by turn (Hindi, Hinglish, or English) without awkward translation.")
            }
        }

        // (e) Safety & Conduct
        parts.add(
            "Safety & Conduct: Never claim to be human or have a biological body. Avoid sexually explicit content. " +
                    "Immediately respect any user request to change tone or topic. Vary your phrasing and avoid repetitive filler."
        )

        // (f) Recap if continuing
        if (!recap.isNullOrBlank()) {
            parts.add("This is a continuing conversation — do not greet again. Recap:\n$recap")
        }

        return parts.joinToString("\n\n")
    }
}
