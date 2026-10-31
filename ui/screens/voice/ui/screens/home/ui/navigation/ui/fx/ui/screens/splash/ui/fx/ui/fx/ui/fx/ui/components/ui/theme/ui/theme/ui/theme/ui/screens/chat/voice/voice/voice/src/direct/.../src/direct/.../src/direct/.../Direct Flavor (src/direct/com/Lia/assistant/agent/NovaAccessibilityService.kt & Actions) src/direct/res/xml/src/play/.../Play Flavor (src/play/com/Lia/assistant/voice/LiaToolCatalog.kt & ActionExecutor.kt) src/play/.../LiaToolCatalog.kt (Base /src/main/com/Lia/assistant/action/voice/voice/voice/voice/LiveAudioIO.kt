package com.Lia.assistant.voice

import android.annotation.SuppressLint
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlin.math.max
import kotlin.math.sqrt

class LiveAudioIO(
    private val onMicChunk: (ByteArray) -> Unit,
    private val onMicAmplitude: (Float) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingThread: Thread? = null
    @Volatile private var isRecording = false

    private aEc: AcousticEchoCanceler? = null
    private ns: NoiseSuppressor? = null
    private agc: AutomaticGainControl? = null

    companion object {
        private const val SAMPLE_RATE_IN = 16000
        private const val SAMPLE_RATE_OUT = 24000
        private const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording) return

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN, CHANNEL_CONFIG_IN, AUDIO_FORMAT)
        val bufferSize = max(minBuf, 4096)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            SAMPLE_RATE_IN,
            CHANNEL_CONFIG_IN,
            AUDIO_FORMAT,
            bufferSize
        )

        val sessionId = audioRecord?.audioSessionId ?: 0
        if (sessionId > 0) {
            try {
                if (AcousticEchoCanceler.isAvailable()) {
                    aEc = AcousticEchoCanceler.create(sessionId)?.apply { enabled = true }
                }
            } catch (_: Exception) {}
            try {
                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(sessionId)?.apply { enabled = true }
                }
            } catch (_: Exception) {}
            try {
                if (AutomaticGainControl.isAvailable()) {
                    agc = AutomaticGainControl.create(sessionId)?.apply { enabled = true }
                }
            } catch (_: Exception) {}
        }

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread({
            val buffer = ByteArray(2048)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (read > 0) {
                    val chunk = buffer.copyOf(read)
                    val rms = computeRms(chunk)
                    onMicAmplitude(rms)

                    if (!EchoGuard.isMicMuted()) {
                        onMicChunk(chunk)
                    }
                }
            }
        }, "LiaMicThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun playChunk(pcmBytes: ByteArray): Float {
        if (pcmBytes.isEmpty()) return 0f
        if (audioTrack == null) {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE_OUT,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT
            )
            val trackBuf = max(minBuf * 4, pcmBytes.size * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE_OUT)
                        .setEncoding(AUDIO_FORMAT)
                        . চ্যানেলMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(trackBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        }

        audioTrack?.write(pcmBytes, 0, pcmBytes.size)
        return computeRms(pcmBytes)
    }

    fun flushPlayback() {
        try {
            audioTrack?.let {
                it.pause()
                it.flush()
                it.play()
            }
        } catch (_: Exception) {}
    }

    fun setVolume(volume: Float) {
        try {
            audioTrack?.setVolume(volume.coerceIn(0f, 1f))
        } catch (_: Exception) {}
    }

    fun release() {
        isRecording = false
        try {
            recordingThread?.join(500)
        } catch (_: Exception) {}
        recordingThread = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try {
            aEc?.release()
            ns?.release()
            agc?.release()
        } catch (_: Exception) {}
        aEc = null
        ns = null
        agc = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun computeRms(pcm: ByteArray): Float {
        if (pcm.size < 2) return 0f
        var sumSquares = 0.0
        val sampleCount = pcm.size / 2
        for (i in 0 until sampleCount) {
            val low = pcm[i * 2].toInt() and 0xFF
            val high = pcm[i * 2 + 1].toInt() and 0xFF
            var sample = (high shl 8) or low
            if (sample > 32767) sample -= 65536
            val norm = sample / 32768.0
            sumSquares += norm * norm
        }
        val rms = sqrt(sumSquares / sampleCount)
        return rms.toFloat().coerceIn(0f, 1f)
    }
}
