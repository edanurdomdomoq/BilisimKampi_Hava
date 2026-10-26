package com.example.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF275D87),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE1D8FF),
        onPrimaryContainer = Color(0xFF17344E),
        secondaryContainer = Color(0xFFE5EDF4),
        background = Color(0xFFF5F8FC),
        surface = Color(0xFFF5F8FC),
        surfaceVariant = Color(0xFFE7EEF5),
        onBackground = Color(0xFF172B40),
        onSurface = Color(0xFF172B40),
        onSurfaceVariant = Color(0xFF586B7E),
        outline = Color(0xFFBBCAD8),
    )
private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFC6B5FF),
        primaryContainer = Color(0xFF2C2948),
        onPrimaryContainer = Color(0xFFE1D8FF),
        secondaryContainer = Color(0xFF232C46),
        background = Color(0xFF080F1C),
        surface = Color(0xFF080F1C),
        surfaceVariant = Color(0xFF182338),
        onBackground = Color(0xFFF2EEFA),
        onSurface = Color(0xFFF2EEFA),
        onSurfaceVariant = Color(0xFFA7ADC9),
        outline = Color(0xFF617588),
    )

@Composable
fun HAVATheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
