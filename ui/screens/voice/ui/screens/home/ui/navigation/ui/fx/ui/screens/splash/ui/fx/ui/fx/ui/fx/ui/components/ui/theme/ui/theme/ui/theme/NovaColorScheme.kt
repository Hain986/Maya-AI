package com.Lia.assistant.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NovaColorScheme(
    val background: Color,
    val backgroundGradientTop: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val surfaceBorder: Color,
    val surfaceRaised: Color,
    val accent: Color,
    val accentSecondary: Color,
    val accentGlow: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val userBubble: Color,
    val assistantBubble: Color,
    val depthShadow: Color,
    val rimLight: Color,
    val lagoon: Color,
    val lotus: Color
)

val DarkNovaColors = NovaColorScheme(
    background = Color(0xFF06050E),
    backgroundGradientTop = Color(0xFF15113A),
    surface = Color(0xFF17143A),
    surfaceGlass = Color(0x26221E4D),
    surfaceBorder = Color(0x33FFB547),
    surfaceRaised = Color(0xFF221E4D),
    accent = Color(0xFFFFB547),
    accentSecondary = Color(0xFFFF8A3D),
    accentGlow = Color(0x66FFB547),
    onAccent = Color(0xFF15112E),
    textPrimary = Color(0xFFEEE9FF),
    textSecondary = Color(0xFFA7A0CC),
    textTertiary = Color(0xFF6E6799),
    success = Color(0xFF3FE0D0),
    warning = Color(0xFFFFB547),
    error = Color(0xFFFF7A85),
    userBubble = Color(0xFFFFB547),
    assistantBubble = Color(0xFF17143A),
    depthShadow = Color(0x66000000),
    rimLight = Color(0x4DFFFFFF),
    lagoon = Color(0xFF3FE0D0),
    lotus = Color(0xFFFF6FAE)
)

val LightNovaColors = NovaColorScheme(
    background = Color(0xFFF3F0FB),
    backgroundGradientTop = Color(0xFFE2DCF6),
    surface = Color(0xFFFFFFFF),
    surfaceGlass = Color(0xCCFFFFFF),
    surfaceBorder = Color(0x33B86A00),
    surfaceRaised = Color(0xFFECE6F8),
    accent = Color(0xFFB86A00),
    accentSecondary = Color(0xFFD67D00),
    accentGlow = Color(0x33B86A00),
    onAccent = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF15112E),
    textSecondary = Color(0xFF5A5280),
    textTertiary = Color(0xFF8A82B3),
    success = Color(0xFF00897B),
    warning = Color(0xFFB86A00),
    error = Color(0xFFD32F2F),
    userBubble = Color(0xFFB86A00),
    assistantBubble = Color(0xFFFFFFFF),
    depthShadow = Color(0x1A000000),
    rimLight = Color(0x80FFFFFF),
    lagoon = Color(0xFF00897B),
    lotus = Color(0xFFC2185B)
)

val LocalNovaColors = staticCompositionLocalOf { DarkNovaColors }
val LocalNovaReducedMotion = staticCompositionLocalOf { false }
val LocalBottomBarInset = staticCompositionLocalOf { 0.dp }
