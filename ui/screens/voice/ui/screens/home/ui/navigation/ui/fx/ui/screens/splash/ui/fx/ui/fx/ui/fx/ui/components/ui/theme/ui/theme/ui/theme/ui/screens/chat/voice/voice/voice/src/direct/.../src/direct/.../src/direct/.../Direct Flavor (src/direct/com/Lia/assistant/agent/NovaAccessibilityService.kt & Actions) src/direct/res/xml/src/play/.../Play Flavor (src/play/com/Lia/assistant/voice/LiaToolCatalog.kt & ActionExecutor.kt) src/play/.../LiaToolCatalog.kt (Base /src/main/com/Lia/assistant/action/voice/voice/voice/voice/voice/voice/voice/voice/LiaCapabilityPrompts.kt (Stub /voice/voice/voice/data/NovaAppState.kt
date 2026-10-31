package com.Lia.assistant.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NovaAppState(private val context: Context) {
    private val repo = PersonalityRepository.getInstance(context)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    var themeMode by mutableStateOf(NovaPreferences.getString(context, NovaPreferences.Keys.THEME_MODE, "DARK"))
        private set

    var reducedMotion by mutableStateOf(NovaPreferences.getBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, false))
        private set

    var orbStyle by mutableStateOf(NovaPreferences.getString(context, NovaPreferences.Keys.ORB_STYLE, "NOVA"))
        private set

    var personality by mutableStateOf(Personality.NORMAL)
        private set

    var assistantName by mutableStateOf(AssistantBrand.NAME)
        private set

    var userName by mutableStateOf(NovaPreferences.getString(context, NovaPreferences.Keys.USER_NAME, "User"))
        private set

    var language by mutableStateOf(LanguagePreference.AUTO)
        private set

    var voiceName by mutableStateOf("Aoede")
        private set

    init {
        ioScope.launch {
            repo.personalityFlow.collect { personality = it }
        }
        ioScope.launch {
            repo.languageFlow.collect { language = it }
        }
        ioScope.launch {
            repo.assistantNameFlow.collect { assistantName = it }
        }
        ioScope.launch {
            repo.voiceNameFlow.collect { voiceName = it }
        }
    }

    fun applyThemeMode(mode: String) {
        themeMode = mode
        NovaPreferences.putString(context, NovaPreferences.Keys.THEME_MODE, mode)
    }

    fun applyReducedMotion(enabled: Boolean) {
        reducedMotion = enabled
        NovaPreferences.putBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, enabled)
    }

    fun applyOrbStyle(style: String) {
        orbStyle = style
        NovaPreferences.putString(context, NovaPreferences.Keys.ORB_STYLE, style)
    }

    fun applyPersonality(newPersonality: Personality) {
        personality = newPersonality
        repo.setPersonality(newPersonality)
    }

    fun applyAssistantName(name: String) {
        assistantName = name
        repo.setAssistantName(name)
    }

    fun applyUserName(name: String) {
        userName = name
        NovaPreferences.putString(context, NovaPreferences.Keys.USER_NAME, name)
    }

    fun applyLanguage(newLanguage: LanguagePreference) {
        language = newLanguage
        repo.setLanguage(newLanguage)
    }

    fun applyVoiceName(newVoice: String) {
        voiceName = newVoice
        repo.setVoiceName(newVoice)
    }
}
