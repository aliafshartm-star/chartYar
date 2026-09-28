package com.chartyar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2962FF),
    onPrimary = Color(0xFFEAECEF),
    secondary = Color(0xFF2EBD85),
    onSecondary = Color(0xFFEAECEF),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFEAECEF),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFEAECEF),
    surfaceVariant = Color(0xFF1C2128),
    onSurfaceVariant = Color(0xFF848E9C),
    error = Color(0xFFF6465D),
    outline = Color(0xFF2B3139)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2962FF),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF2EBD85),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1D21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1D21),
    surfaceVariant = Color(0xFFEEF1F5),
    onSurfaceVariant = Color(0xFF5A6470),
    error = Color(0xFFF6465D),
    outline = Color(0xFFD8DDE3)
)

@Composable
fun ChartYarTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDark) DarkColors else LightColors
    val chartyarColors = if (isDark) DarkThemeColors else LightThemeColors

    CompositionLocalProvider(LocalChartyarColors provides chartyarColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}