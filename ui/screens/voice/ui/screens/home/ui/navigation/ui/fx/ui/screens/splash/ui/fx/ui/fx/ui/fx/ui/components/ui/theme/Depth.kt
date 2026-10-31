package com.Lia.assistant.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.nightSky(): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF15113A),
            Color(0xFF06050E),
            Color(0xFF030208)
        )
    )
)

fun Modifier.depthSurface(
    shape: Shape,
    elevation: Dp = 4.dp,
    backgroundColor: Color = Color(0xFF17143A),
    borderColor: Color = Color(0x33FFB547)
): Modifier = this.drawBehind {
    // Ambient depth shadow & rim light
    drawRect(color = backgroundColor)
}.background(backgroundColor, shape)
