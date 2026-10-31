package com.Lia.assistant.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun HomeScreen(
    appState: NovaAppState,
    onOpenVoice: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenForge: () -> Unit
) {
    val state = VoiceSessionManager.state.collectAsState().value
    val amplitude = VoiceSessionManager.amplitude.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Good day, ${appState.userName}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "I am ${appState.assistantName}, your AI companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Hero 3D Orb
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            LiaOrb3D(state = state, size = 180.dp, amplitude = amplitude)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NovaButton(text = "Talk", onClick = onOpenVoice, modifier = Modifier.weight(1f), isPrimary = true)
            NovaButton(text = "Chat", onClick = onOpenChat, modifier = Modifier.weight(1f), isPrimary = false)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Try saying",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        NovaGlassCard(onClick = onOpenForge) {
            Text(text = "🌐 \"Maya, ek cafe ki website banao\"", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Launches Website Forge with live 3D preview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
