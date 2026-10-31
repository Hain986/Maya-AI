package com.Lia.assistant.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.screens.chat.ChatScreen
import com.Lia.assistant.ui.screens.forge.ForgeScreen
import com.Lia.assistant.ui.screens.home.HomeScreen
import com.Lia.assistant.ui.screens.quickactions.QuickActionsScreen
import com.Lia.assistant.ui.screens.settings.SettingsScreen
import com.Lia.assistant.ui.screens.voice.VoiceScreen
import com.Lia.assistant.voice.VoiceSessionManager

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Voice : Screen("voice", "Talk", null)
    object QuickActions : Screen("quick_actions", "Actions", Icons.Default.Apps)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Forge : Screen("forge", "Forge", null)
}

@Composable
v, NovaNavHost(appState: NovaAppState, initialOpenForge: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Chat.route,
        Screen.QuickActions.route,
        Screen.Settings.route
    )

    LaunchedEffect(initialOpenForge) {
        if (initialOpenForge) {
            navController.navigate(Screen.Forge.route)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = if (currentRoute == Screen.Home.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { navController.navigate(Screen.Chat.route) }) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = if (currentRoute == Screen.Chat.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Centre Docked Talk Orb Button
                        FloatingActionButton(
                            onClick = { navController.navigate(Screen.Voice.route) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            val state = VoiceSessionManager.state.collectAsState().value
                            val amplitude = VoiceSessionManager.amplitude.collectAsState().value
                            LiaOrb3D(state = state, size = 40.dp, amplitude = amplitude)
                        }

                        IconButton(onClick = { navController.navigate(Screen.QuickActions.route) }) {
                            Icon(Icons.Default.Apps, contentDescription = "Actions", tint = if (currentRoute == Screen.QuickActions.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = if (currentRoute == Screen.Settings.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    appState = appState,
                    onOpenVoice = { navController.navigate(Screen.Voice.route) },
                    onOpenChat = { navController.navigate(Screen.Chat.route) },
                    onOpenForge = { navController.navigate(Screen.Forge.route) }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    appState = appState,
                    onBack = { navController.popBackStack() },
                    onOpenForge = { navController.navigate(Screen.Forge.route) }
                )
            }
            composable(Screen.Voice.route) {
                VoiceScreen(
                    appState = appState,
                    onClose = { navController.popBackStack() }
                )
            }
            composable(Screen.QuickActions.route) {
                QuickActionsScreen(
                    appState = appState,
                    onOpenForge = { navController.navigate(Screen.Forge.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    appState = appState,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Forge.route) {
                ForgeScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
