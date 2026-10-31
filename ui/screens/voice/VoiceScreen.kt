package com.Lia.assistant.ui.screens.voice

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun VoiceScreen(
    appState: NovaAppState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val state = VoiceSessionManager.state.collectAsState().value
    val amplitude = VoiceSessionManager.amplitude.collectAsState().value
    val statusText = VoiceSessionManager.statusText.collectAsState().value
    val muted = VoiceSessionManager.muted.collectAsState().value

    LaunchedEffect(Unit) {
        VoiceSessionManager.start(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    VoiceSessionManager.stop()
                    onClose()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LiaOrb3D(state = state, size = 240.dp, amplitude = amplitude)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { VoiceSessionManager.toggleMute() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                NovaButton(
                    text = "End Session",
                    onClick = {
                        VoiceSessionManager.stop()
                        onClose()
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = false
                )
            }
        }
    }
}
