package com.Lia.assistant.ui.screens.quickactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaGlassCard

data class QuickActionItem(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsScreen(
    appState: NovaAppState,
    onOpenForge: () -> Unit,
    onBack: () -> Unit
) {
    val items = listOf(
        QuickActionItem("Website Forge", "Build 3D interactive website", Icons.Default.Web, onOpenForge),
        QuickActionItem("Open YouTube", "Launch app via voice/action", Icons.Default.PlayArrow, {}),
        QuickActionItem("Call Mom", "Initiate contact call", Icons.Default.Phone, {}),
        QuickActionItem("WhatsApp Chat", "Prepare message composer", Icons.Default.Message, {})
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Actions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { action ->
                NovaGlassCard(onClick = action.onClick) {
                    Icon(imageVector = action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = action.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
