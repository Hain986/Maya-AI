package com.Lia.assistant.voice

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

object EchoGuard {
    private const val ECHO_TAIL_MS = 450L
    private const val WATCHDOG_TIMEOUT_MS = 60_000L

    private val activePlaybackCount = AtomicInteger(0)
    private var tailJob: Job? = null
    private var watchdogJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Synchronized
    fun notifyPlaybackStarted() {
        activePlaybackCount.incrementAndGet()
        tailJob?.cancel()
        tailJob = null

        // Start or reset watchdog timer to prevent permanent mute locks
        if (watchdogJob?.isActive != true) {
            watchdogJob = scope.launch {
                delay(WATCHDOG_TIMEOUT_MS)
                forceReset()
            }
        }
    }

    @Synchronized
    fun notifyPlaybackStopped() {
        val current = activePlaybackCount.decrementAndGet()
        if (current <= 0) {
            activePlaybackCount.set(0)
            tailJob?.cancel()
            tailJob = scope.launch {
                delay(ECHO_TAIL_MS)
                if (activePlaybackCount.get() == 0) {
                    watchdogJob?.cancel()
                    watchdogJob = null
                }
            }
        }
    }

    fun isMicMuted(): Boolean {
        return activePlaybackCount.get() > 0 || tailJob?.isActive == true
    }

    @Synchronized
    fun forceReset() {
        activePlaybackCount.set(0)
        tailJob?.cancel()
        tailJob = null
        watchdogJob?.cancel()
        watchdogJob = null
    }

    @Synchronized
    fun reset() {
        forceReset()
    }
}
