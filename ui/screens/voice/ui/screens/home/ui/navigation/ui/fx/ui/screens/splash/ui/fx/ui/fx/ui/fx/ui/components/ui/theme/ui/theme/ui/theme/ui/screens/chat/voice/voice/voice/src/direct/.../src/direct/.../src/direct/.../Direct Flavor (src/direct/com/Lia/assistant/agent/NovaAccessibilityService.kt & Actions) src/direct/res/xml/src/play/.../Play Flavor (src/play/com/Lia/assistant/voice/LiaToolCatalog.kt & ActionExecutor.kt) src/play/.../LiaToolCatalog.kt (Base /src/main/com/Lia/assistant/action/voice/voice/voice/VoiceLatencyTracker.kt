package com.Lia.assistant.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceLatencyTracker {
    private var speechStartNano = 0L
    private var firstAudioChunkNano = 0L
    private var turnCompleteNano = 0L

    private val _reconnectCount = MutableStateFlow(0)
    val reconnectCount: StateFlow<Int> = _reconnectCount.asStateFlow()

    fun markSpeechStart() {
        speechStartNano = System.nanoTime()
        firstAudioChunkNano = 0L
        turnCompleteNano = 0L
    }

    fun markFirstAudioChunk() {
        if (firstAudioChunkNano == 0L && speechStartNano > 0L) {
            firstAudioChunkNano = System.nanoTime()
        }
    }

    fun markTurnComplete() {
        turnCompleteNano = System.nanoTime()
    }

    fun getTtffMs(): Long {
        if (speechStartNano == 0L || firstAudioChunkNano == 0L) return 0L
        return (firstAudioChunkNano - speechStartNano) / 1_000_000L
    }

    fun incrementReconnect() {
        _reconnectCount.value++
    }

    fun reset() {
        speechStartNano = 0L
        firstAudioChunkNano = 0L
        turnCompleteNano = 0L
    }
}
