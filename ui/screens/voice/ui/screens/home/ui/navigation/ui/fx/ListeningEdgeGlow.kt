package com.Lia.assistant.ui.fx

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun ListeningEdgeGlow() {
    val state = VoiceSessionManager.state.collectAsState().value
    val isActive = VoiceSessionManager.isActive.collectAsState().value

    if (!isActive) return

    val glowColor = when (state) {
        NovaOrbState.SPEAKING -> Color(0xFFFF6FAE)
        NovaOrbState.ERROR -> Color(0xFFFF7A85)
        else -> Color(0xFF8B5CF6)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, glowColor)
    )
}
