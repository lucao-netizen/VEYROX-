package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.domain.model.ThemePreference

@Immutable
data class VeyroxColors(
    val isDark: Boolean,
    val background: Color,
    val secondaryBackground: Color,
    val cardBackground: Color,
    val elevatedSurface: Color,
    val borderSubtle: Color,
    val primaryBlue: Color,
    val brightBlue: Color,
    val blueTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val codeBackground: Color,
    val userBubble: Color,
    val assistantBubble: Color
)

private val DarkVeyroxColors = VeyroxColors(
    isDark = true,
    background = VeyroxBlack,
    secondaryBackground = VeyroxSecondaryDark,
    cardBackground = VeyroxCardDark,
    elevatedSurface = VeyroxElevatedDark,
    borderSubtle = VeyroxBorderDark,
    primaryBlue = VeyroxPrimaryBlue,
    brightBlue = VeyroxBrightBlue,
    blueTint = VeyroxBlueTintDark,
    textPrimary = VeyroxTextPrimaryDark,
    textSecondary = VeyroxTextSecondaryDark,
    codeBackground = Color(0xFF0B0E16),
    userBubble = Color(0xFF1A3A78),
    assistantBubble = VeyroxCardDark
)

private val LightVeyroxColors = VeyroxColors(
    isDark = false,
    background = VeyroxLightBg,
    secondaryBackground = VeyroxLightSecondary,
    cardBackground = VeyroxLightCard,
    elevatedSurface = VeyroxLightElevated,
    borderSubtle = VeyroxLightBorder,
    primaryBlue = VeyroxPrimaryBlue,
    brightBlue = VeyroxDeepBlue,
    blueTint = VeyroxBlueTintLight,
    textPrimary = VeyroxTextPrimaryLight,
    textSecondary = VeyroxTextSecondaryLight,
    codeBackground = Color(0xFF0E1320),
    userBubble = VeyroxPrimaryBlue,
    assistantBubble = VeyroxLightSecondary
)

val LocalVeyroxColors = staticCompositionLocalOf { DarkVeyroxColors }

private val VeyroxDarkColorScheme = darkColorScheme(
    primary = VeyroxPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = VeyroxBlueTintDark,
    onPrimaryContainer = VeyroxBrightBlue,
    secondary = VeyroxBrightBlue,
    onSecondary = VeyroxBlack,
    secondaryContainer = VeyroxElevatedDark,
    onSecondaryContainer = VeyroxTextPrimaryDark,
    background = VeyroxBlack,
    onBackground = VeyroxTextPrimaryDark,
    surface = VeyroxSecondaryDark,
    onSurface = VeyroxTextPrimaryDark,
    surfaceVariant = VeyroxCardDark,
    onSurfaceVariant = VeyroxTextSecondaryDark,
    outline = VeyroxBorderDark,
    outlineVariant = Color(0xFF1A2130),
    error = VeyroxError,
    onError = Color.White
)

private val VeyroxLightColorScheme = lightColorScheme(
    primary = VeyroxPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = VeyroxBlueTintLight,
    onPrimaryContainer = VeyroxDeepBlue,
    secondary = VeyroxDeepBlue,
    onSecondary = Color.White,
    secondaryContainer = VeyroxLightElevated,
    onSecondaryContainer = VeyroxTextPrimaryLight,
    background = VeyroxLightBg,
    onBackground = VeyroxTextPrimaryLight,
    surface = VeyroxLightSecondary,
    onSurface = VeyroxTextPrimaryLight,
    surfaceVariant = VeyroxLightCard,
    onSurfaceVariant = VeyroxTextSecondaryLight,
    outline = VeyroxLightBorder,
    outlineVariant = Color(0xFFE0E7F3),
    error = VeyroxError,
    onError = Color.White
)

val VeyroxShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object VeyroxTheme {
    val colors: VeyroxColors
        @Composable
        get() = LocalVeyroxColors.current
}

@Composable
fun VeyroxTheme(
    themePreference: ThemePreference = ThemePreference.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val veyroxColors = if (isDark) DarkVeyroxColors else LightVeyroxColors
    val m3Scheme = if (isDark) VeyroxDarkColorScheme else VeyroxLightColorScheme

    CompositionLocalProvider(LocalVeyroxColors provides veyroxColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = Typography,
            shapes = VeyroxShapes,
            content = content
        )
    }
}
