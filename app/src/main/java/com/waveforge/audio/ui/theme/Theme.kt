package com.waveforge.audio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WaveForgeDarkColorScheme = darkColorScheme(
    primary = WaveForgeAccent,
    secondary = WaveForgeAccent,
    tertiary = WaveForgeAccent,
    background = WaveForgeBackground,
    surface = WaveForgeElevatedBackground,
    surfaceVariant = WaveForgeCard,
    onPrimary = WaveForgeBackground,
    onSecondary = WaveForgeBackground,
    onTertiary = WaveForgeBackground,
    onBackground = WaveForgeTextPrimary,
    onSurface = WaveForgeTextPrimary,
    onSurfaceVariant = WaveForgeTextSecondary,
    error = WaveForgeError,
    onError = WaveForgeTextPrimary
)

@Composable
fun WaveForgeTheme(
    content: @Composable () -> Unit
) {
    // We only use the dark scheme for WaveForge
    val colorScheme = WaveForgeDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WaveForgeTypography,
        content = content
    )
}
