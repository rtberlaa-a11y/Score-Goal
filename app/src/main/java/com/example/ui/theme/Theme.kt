package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = Color.Black,
    primaryContainer = PitchGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = LiveRed,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00A854),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F8E8),
    onPrimaryContainer = Color(0xFF00381A),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    tertiary = LiveRed,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun ScoreGoalTheme(
    darkTheme: Boolean = true, // Default to sleek athletic dark mode for stadium feel
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
