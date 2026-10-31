package com.Lia.assistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.Lia.assistant.voice.VoiceSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "assistant_settings")

class PersonalityRepository private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    object PreferencesKeys {
        val PERSONALITY = stringPreferencesKey("personality_key")
        val LANGUAGE = stringPreferencesKey("language_key")
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name_key")
        val VOICE_NAME = stringPreferencesKey("voice_name_key")
    }

    val personalityFlow: Flow<Personality> = context.dataStore.data.map { prefs ->
        val name = prefs[PreferencesKeys.PERSONALITY] ?: Personality.NORMAL.name
        try { Personality.valueOf(name) } catch (e: Exception) { Personality.NORMAL }
    }

    val languageFlow: Flow<LanguagePreference> = context.dataStore.data.map { prefs ->
        val name = prefs[PreferencesKeys.LANGUAGE] ?: LanguagePreference.AUTO.name
        try { LanguagePreference.valueOf(name) } catch (e: Exception) { LanguagePreference.AUTO }
    }

    val assistantNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ASSISTANT_NAME] ?: NovaPreferences.getString(context, NovaPreferences.Keys.ASSISTANT_NAME, AssistantBrand.NAME)
    }

    val voiceNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VOICE_NAME] ?: NovaPreferences.getString(context, NovaPreferences.Keys.VOICE_NAME, "Aoede")
    }

    fun setPersonality(personality: Personality) {
        NovaPreferences.putString(context, NovaPreferences.Keys.PERSONALITY, personality.name)
        scope.launch {
            context.dataStore.edit { it[PreferencesKeys.PERSONALITY] = personality.name }
            VoiceSessionManager.refreshInstructions(context)
        }
    }

    fun setLanguage(language: LanguagePreference) {
        NovaPreferences.putString(context, NovaPreferences.Keys.LANGUAGE, language.name)
        scope.launch {
            context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = language.name }
            VoiceSessionManager.refreshInstructions(context)
        }
    }

    fun setAssistantName(name: String) {
        val trimmed = name.ifBlank { AssistantBrand.NAME }
        NovaPreferences.putString(context, NovaPreferences.Keys.ASSISTANT_NAME, trimmed)
        scope.launch {
            context.dataStore.edit { it[PreferencesKeys.ASSISTANT_NAME] = trimmed }
            VoiceSessionManager.refreshInstructions(context)
        }
    }

    fun setVoiceName(voiceName: String) {
        NovaPreferences.putString(context, NovaPreferences.Keys.VOICE_NAME, voiceName)
        scope.launch {
            context.dataStore.edit { it[PreferencesKeys.VOICE_NAME] = voiceName }
            VoiceSessionManager.refreshInstructions(context)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PersonalityRepository? = null

        fun getInstance(context: Context): PersonalityRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PersonalityRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
