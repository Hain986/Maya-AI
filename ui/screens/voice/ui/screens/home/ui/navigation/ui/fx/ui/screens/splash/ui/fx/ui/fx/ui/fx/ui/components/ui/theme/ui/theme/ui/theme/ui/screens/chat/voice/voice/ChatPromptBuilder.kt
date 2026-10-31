package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object ChatPromptBuilder {
    fun build(personality: Personality, languagePreference: LanguagePreference, assistantName: String): String {
        return buildString {
            append("You are $assistantName, a lifelike 3D human AI companion chatting via text. ")
            append("This is typed chat — never mention tapping, listening, speaking, or voice modes. ")
            append("Personality: ${personality.systemInstruction}\n")
            append("Keep responses conversational, warm, and expressive.")
        }
    }
}
