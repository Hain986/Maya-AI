package com.Lia.assistant.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.delay

object SplashSession {
    var played = false
}

@Composable
fun LiaSplash3D(onFinished: () -> Unit) {
    var startExit by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(600),
        finishedListener = { onFinished() },
        label = "SplashFade"
    )

    LaunchedEffect(Unit) {
        delay(2000L)
        startExit = true
        SplashSession.played = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LiaOrb3D(state = NovaOrbState.IDLE, size = 160.dp, amplitude = 0f)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = AssistantBrand.FULL_NAME,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
