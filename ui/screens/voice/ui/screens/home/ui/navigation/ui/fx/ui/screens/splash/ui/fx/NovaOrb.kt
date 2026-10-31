package com.Lia.assistant.ui.fx

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.Lia.assistant.ui.theme.NovaThemeTokens
import com.Lia.assistant.voice.NovaOrbState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NovaOrb(
    state: NovaOrbState,
    style: String = "NOVA",
    size: Dp,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val reducedMotion = NovaThemeTokens.reducedMotion
    val infiniteTransition = rememberInfiniteTransition(label = "NovaOrb")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val currentScale = if (reducedMotion) 1f else (pulse + amplitude * 0.4f)

    val (color1, color2) = when (state) {
        NovaOrbState.IDLE -> Pair(Color(0xFFFF6FAE), Color(0xFF8B5CF6))
        NovaOrbState.LISTENING -> Pair(Color(0xFF3FE0D0), Color(0xFF0284C7))
        NovaOrbState.THINKING -> Pair(Color(0xFF8B5CF6), Color(0xFFC084FC))
        NovaOrbState.SPEAKING -> Pair(Color(0xFFFFB547), Color(0xFFFF6FAE))
        NovaOrbState.CONNECTING -> Pair(Color(0xFFF59E0B), Color(0xFFD97706))
        NovaOrbState.ERROR -> Pair(Color(0xFFFF7A85), Color(0xFFB91C1C))
    }

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
        val radius = (size.toPx() / 2f) * 0.8f * currentScale

        // Outer Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = center
        )

        // Core Sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, color2),
                center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f),
                radius = radius * 1.4f
            ),
            radius = radius,
            center = center
        )

        // Accent Ring
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius * 0.9f,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
