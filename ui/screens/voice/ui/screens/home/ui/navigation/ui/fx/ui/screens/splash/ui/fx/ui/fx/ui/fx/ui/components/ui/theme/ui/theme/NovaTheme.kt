package com.Lia.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun NovaTheme(
    mode: String = "DARK",
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (mode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val novaColors = if (isDark) DarkNovaColors else LightNovaColors
    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = novaColors.accent,
            background = novaColors.background,
            surface = novaColors.surface,
            error = novaColors.error
        )
    } else {
        lightColorScheme(
            primary = novaColors.accent,
            background = novaColors.background,
            surface = novaColors.surface,
            error = novaColors.error
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = novaColors.background.toArgb()
            window.navigationBarColor = novaColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalNovaColors provides novaColors,
        LocalNovaReducedMotion provides reducedMotion
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            content = content
        )
    }
}

object NovaThemeTokens {
    val colors: NovaColorScheme
        @Composable get() = LocalNovaColors.current

    val reducedMotion: Boolean
        @Composable get() = LocalNovaReducedMotion.current
}
