package com.Lia.assistant.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaSettingsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appState: NovaAppState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NovaSettingsRow(
                icon = Icons.Default.DarkMode,
                title = "Theme Mode",
                subtitle = "Current: ${appState.themeMode}",
                onClick = {
                    appState.applyThemeMode(if (appState.themeMode == "DARK") "LIGHT" else "DARK")
                }
            )
            NovaSettingsRow(
                icon = Icons.Default.Animation,
                title = "Reduced Motion",
                subtitle = if (appState.reducedMotion) "Enabled" else "Disabled",
                onClick = {
                    appState.applyReducedMotion(!appState.reducedMotion)
                }
            )
            NovaSettingsRow(
                icon = Icons.Default.Person,
                title = "Assistant Name",
                subtitle = appState.assistantName,
                onClick = {
                    // Toggle or edit name
                    val newName = if (appState.assistantName == "Maya") "Maya AI" else "Maya"
                    appState.applyAssistantName(newName)
                }
            )
        }
    }
}
