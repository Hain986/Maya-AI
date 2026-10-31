package com.Lia.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.fx.ListeningEdgeGlow
import com.Lia.assistant.ui.navigation.NovaNavHost
import com.Lia.assistant.ui.screens.splash.LiaSplash3D
import com.Lia.assistant.ui.screens.splash.SplashSession
import com.Lia.assistant.ui.theme.NovaTheme

class MainActivity : ComponentActivity() {
    private var pendingForgeOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            val appState = remember { NovaAppState(applicationContext) }
            var showSplash by remember { mutableStateOf(!SplashSession.played) }

            NovaTheme(mode = appState.themeMode, reducedMotion = appState.reducedMotion) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NovaNavHost(appState = appState, initialOpenForge = pendingForgeOpen)
                    if (showSplash) {
                        LiaSplash3D { showSplash = false }
                    }
                    ListeningEdgeGlow()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_OPEN_FORGE", false) == true) {
            pendingForgeOpen = true
        }
    }
}
