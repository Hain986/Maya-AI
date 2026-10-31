package com.Lia.assistant.voice

import android.content.Context
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import com.Lia.assistant.action.ActionExecutor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

enum class NovaOrbState {
    IDLE, LISTENING, THINKING, SPEAKING, CONNECTING, ERROR
}

object VoiceSessionManager : GeminiLiveClient.Listener {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()

    private val _state = MutableStateFlow(NovaOrbState.IDLE)
    val state: StateFlow<NovaOrbState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private val _statusText = MutableStateFlow("Maya is ready")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _detectedLanguage = MutableStateFlow<LanguagePreference?>(null)
    val detectedLanguage: StateFlow<LanguagePreference?> = _detectedLanguage.asStateFlow()

    private var client: GeminiLiveClient? = null
    private var audioIO: LiveAudioIO? = null
    private val memory = ConversationMemory()
    private val latencyTracker = VoiceLatencyTracker()

    private var sessionEpoch = 0
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private var silenceWatchdogJob: Job? = null

    fun start(context: Context) {
        if (_isActive.value) return
        scope.launch {
            mutex.withLock {
                _isActive.value = true
                _state.value = NovaOrbState.CONNECTING
                _statusText.value = "Connecting to Maya..."
                _lastError.value = null
                reconnectAttempts = 0
                sessionEpoch++
                memory.clear()

                audioIO = LiveAudioIO(
                    onMicChunk = { bytes ->
                        if (!_muted.value && !EchoGuard.isMicMuted()) {
                            client?.sendAudio(bytes)
                        }
                    },
                    onMicAmplitude = { rms ->
                        if (_state.value == NovaOrbState.LISTENING) {
                            _amplitude.value = rms
                        }
                    }
                )

                connectInternal(context, isResuming = false, epoch = sessionEpoch)
            }
        }
    }

    private suspend fun connectInternal(context: Context, isResuming: Boolean, epoch: Int) {
        if (epoch != sessionEpoch) return
        val apiKey = ApiKeyStore.getKey(context)
        if (apiKey.isNullOrBlank()) {
            _state.value = NovaOrbState.ERROR
            _statusText.value = "API key missing in Settings"
            _lastError.value = "Missing API Key"
            _isActive.value = false
            return
        }

        val repo = PersonalityRepository.getInstance(context)
        // Read current preferences (or use defaults if collection hasn't emitted yet)
        val personality = Personality.NORMAL // Can be fetched from repo snapshot if needed
        val langPref = LanguagePreference.AUTO
        val assistantName = "Maya"
        val voiceName = "Aoede"

        val prompt = PersonalityPromptBuilder.build(
            personality = personality,
            languagePreference = langPref,
            detectedLanguage = _detectedLanguage.value,
            recap = if (isResuming) memory.recap() else null,
            assistantName = assistantName
        )

        client?.disconnect()
        client = GeminiLiveClient(this)
        client?.connect(apiKey, prompt, voiceName)
    }

    override fun onSetupComplete() {
        scope.launch {
            _state.value = NovaOrbState.LISTENING
            _statusText.value = "Maya is listening..."
            audioIO?.startRecording()
        }
    }

    override fun onAudioChunk(pcmBytes: ByteArray) {
        latencyTracker.markFirstAudioChunk()
        silenceWatchdogJob?.cancel()

        EchoGuard.notifyPlaybackStarted()
        val rms = audioIO?.playChunk(pcmBytes) ?: 0f
        _amplitude.value = rms
        _state.value = NovaOrbState.SPEAKING
        _statusText.value = "Maya is speaking..."

        // Rearm 300ms silence watchdog to detect end of turn
        silenceWatchdogJob = scope.launch {
            delay(300L)
            EchoGuard.notifyPlaybackStopped()
            if (_isActive.value && _state.value == NovaOrbState.SPEAKING) {
                _state.value = NovaOrbState.LISTENING
                _statusText.value = "Maya is listening..."
            }
        }
    }

    override fun onInputTranscript(text: String) {
        if (text.isNotBlank()) {
            val detected = LanguageDetector.detect(text)
            _detectedLanguage.value = detected
            memory.addTurn(isUser = true, text = text)
            _state.value = NovaOrbState.THINKING
            _statusText.value = "Maya is thinking..."
            latencyTracker.markSpeechStart()
        }
    }

    override fun onOutputTranscript(text: String) {
        if (text.isNotBlank()) {
            memory.addTurn(isUser = false, text = text)
        }
    }

    override fun onToolCall(id: String, name: String, args: JSONObject) {
        scope.launch {
            val currentEpoch = sessionEpoch
            if (name == "build_website") {
                // Trigger Website Forge immediately
                val result = JSONObject().put("result", "forge_started")
                client?.sendToolResponse(id, name, result)
            } else {
                val result = ActionExecutor.execute(name, args)
                if (currentEpoch == sessionEpoch) {
                    client?.sendToolResponse(id, name, result)
                }
            }
        }
    }

    override fun onTurnComplete() {
        audioIO?.flushPlayback()
        EchoGuard.notifyPlaybackStopped()
        latencyTracker.markTurnComplete()
        if (_isActive.value) {
            _state.value = NovaOrbState.LISTENING
            _statusText.value = "Maya is listening..."
        }
    }

    override fun onInterrupted() {
        audioIO?.flushPlayback()
        EchoGuard.notifyPlaybackStopped()
        if (_isActive.value) {
            _state.value = NovaOrbState.LISTENING
            _statusText.value = "Maya is listening..."
        }
    }

    override fun onError(message: String) {
        _lastError.value = message
        scheduleReconnect()
    }

    override fun onClosed() {
        if (_isActive.value) {
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        if (!(_isActive.value)) return
        if (reconnectJob?.isActive == true) return

        reconnectJob = scope.launch {
            mutex.withLock {
                if (!(_isActive.value)) return@withLock
                reconnectAttempts++
                if (reconnectAttempts > 5) {
                    _state.value = NovaOrbState.ERROR
                    _statusText.value = "Connection lost - tap Stop and reopen Voice Mode"
                    _isActive.value = false
                    return@withLock
                }

                _state.value = NovaOrbState.CONNECTING
                _statusText.value = "Reconnecting (attempt $reconnectAttempts/5)..."
                latencyTracker.incrementReconnect()
                delay(1200L)
                connectInternal(null ?: return@withLock, isResuming = true, epoch = sessionEpoch)
            }
        }
    }

    fun refreshInstructions(context: Context) {
        if (!_isActive.value) return
        scope.launch {
            mutex.withLock {
                sessionEpoch++
                _state.value = NovaOrbState.CONNECTING
                _statusText.value = "Updating settings..."
                connectInternal(context, isResuming = true, epoch = sessionEpoch)
            }
        }
    }

    fun toggleMute() {
        _muted.value = !_muted.value
    }

    fun stop() {
        scope.launch {
            mutex.withLock {
                _isActive.value = false
                sessionEpoch++
                reconnectJob?.cancel()
                silenceWatchdogJob?.cancel()
                client?.disconnect()
                client = null
                audioIO?.release()
                audioIO = null
                EchoGuard.reset()
                memory.clear()
                latencyTracker.reset()
                _state.value = NovaOrbState.IDLE
                _statusText.value = "Maya is ready"
            }
        }
    }
}
