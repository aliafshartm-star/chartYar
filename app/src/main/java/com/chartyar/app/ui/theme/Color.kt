package com.chartyar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==================== رنگ‌های خام (ثابت) ====================
private val RawAccentBlue = Color(0xFF2962FF)
private val RawAccentGreen = Color(0xFF2EBD85)
private val RawAccentRed = Color(0xFFF6465D)
private val RawAccentYellow = Color(0xFFF0B90B)

private val RawDarkBg = Color(0xFF0D1117)
private val RawDarkSurface = Color(0xFF161B22)
private val RawDarkCard = Color(0xFF1C2128)
private val RawDarkTextPrimary = Color(0xFFEAECEF)
private val RawDarkTextSecondary = Color(0xFF848E9C)
private val RawDarkDivider = Color(0xFF2B3139)

private val RawLightBg = Color(0xFFF5F7FA)
private val RawLightSurface = Color(0xFFFFFFFF)
private val RawLightCard = Color(0xFFEEF1F5)
private val RawLightTextPrimary = Color(0xFF1A1D21)
private val RawLightTextSecondary = Color(0xFF5A6470)
private val RawLightDivider = Color(0xFFD8DDE3)

// ==================== مدل رنگ‌ها ====================
data class ChartyarColors(
    val bg: Color,
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentBlue: Color,
    val accentGreen: Color,
    val accentRed: Color,
    val accentYellow: Color,
    val divider: Color
)

val DarkThemeColors = ChartyarColors(
    bg = RawDarkBg,
    surface = RawDarkSurface,
    card = RawDarkCard,
    textPrimary = RawDarkTextPrimary,
    textSecondary = RawDarkTextSecondary,
    accentBlue = RawAccentBlue,
    accentGreen = RawAccentGreen,
    accentRed = RawAccentRed,
    accentYellow = RawAccentYellow,
    divider = RawDarkDivider
)

val LightThemeColors = ChartyarColors(
    bg = RawLightBg,
    surface = RawLightSurface,
    card = RawLightCard,
    textPrimary = RawLightTextPrimary,
    textSecondary = RawLightTextSecondary,
    accentBlue = RawAccentBlue,
    accentGreen = RawAccentGreen,
    accentRed = RawAccentRed,
    accentYellow = RawAccentYellow,
    divider = RawLightDivider
)

val LocalChartyarColors = staticCompositionLocalOf { DarkThemeColors }

// ==================== دسترسی داینامیک ====================
val DarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.bg

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.surface

val DarkCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.card

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.textSecondary

val AccentBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.accentBlue

val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.accentGreen

val AccentRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.accentRed

val AccentYellow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.accentYellow

val Divider: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalChartyarColors.current.divider