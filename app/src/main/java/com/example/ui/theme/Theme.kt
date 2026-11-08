package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberBgSurfaceVariant,
    onPrimaryContainer = CyberCyan,
    secondary = CyberMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF350020),
    onSecondaryContainer = CyberPinkGlow,
    tertiary = CyberAmber,
    onTertiary = Color.Black,
    background = CyberBgDark,
    onBackground = TextPrimary,
    surface = CyberBgSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberBgSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = CyberRedGlitch,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced cyberpunk theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
