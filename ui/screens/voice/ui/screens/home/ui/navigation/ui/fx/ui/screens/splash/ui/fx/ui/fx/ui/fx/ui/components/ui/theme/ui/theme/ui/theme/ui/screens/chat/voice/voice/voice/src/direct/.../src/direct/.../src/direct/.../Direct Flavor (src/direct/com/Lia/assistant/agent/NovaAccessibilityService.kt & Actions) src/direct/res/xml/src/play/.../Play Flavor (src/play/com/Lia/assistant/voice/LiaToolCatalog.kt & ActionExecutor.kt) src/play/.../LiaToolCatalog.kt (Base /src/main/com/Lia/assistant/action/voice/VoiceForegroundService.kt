package com.Lia.assistant.voice

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.Lia.assistant.MainActivity
import com.Lia.assistant.data.AssistantBrand

class VoiceForegroundService : Service() {
    companion object {
        const val ACTION_STOP = "com.Lia.assistant.action.STOP_VOICE"
        private const val CHANNEL_ID = "maya_voice_channel"
        private const val NOTIF_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            VoiceSessionManager.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val stopIntent = Intent(this, VoiceForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${AssistantBrand.NAME} AI Voice Session")
            .setContentText("${AssistantBrand.NAME} is listening in real time...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()

        startForeground(NOTIF_ID, notification)
        VoiceSessionManager.start(this)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "${AssistantBrand.NAME} Voice Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active voice companion channel"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        VoiceSessionManager.stop()
        super.onDestroy()
    }
}
