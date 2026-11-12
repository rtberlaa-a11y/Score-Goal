package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AppBackgroundTheme(
    val key: String,
    val name: String,
    val backgroundBrush: Brush,
    val surfaceColor: Color,
    val accentColor: Color,
    val previewColor: Color
)

object ThemePresets {
    val themes = listOf(
        AppBackgroundTheme(
            key = "midnight_stadium",
            name = "Midnight Stadium",
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0C1117), Color(0xFF090D12))
            ),
            surfaceColor = Color(0xFF161F28),
            accentColor = Color(0xFF00E676),
            previewColor = Color(0xFF0C1117)
        ),
        AppBackgroundTheme(
            key = "emerald_turf",
            name = "Emerald Turf",
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF071F15), Color(0xFF04140D))
            ),
            surfaceColor = Color(0xFF0C2C1F),
            accentColor = Color(0xFF00FF88),
            previewColor = Color(0xFF063321)
        ),
        AppBackgroundTheme(
            key = "cyber_neon",
            name = "Cyber Neon",
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0A0F1D), Color(0xFF060912))
            ),
            surfaceColor = Color(0xFF121B30),
            accentColor = Color(0xFF00E5FF),
            previewColor = Color(0xFF0059B2)
        ),
        AppBackgroundTheme(
            key = "sunset_derby",
            name = "Sunset Derby",
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1F0E18), Color(0xFF12080E))
            ),
            surfaceColor = Color(0xFF2C1523),
            accentColor = Color(0xFFFFB300),
            previewColor = Color(0xFFD81B60)
        ),
        AppBackgroundTheme(
            key = "classic_pitch",
            name = "Classic Pitch",
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF141414), Color(0xFF0B0B0B))
            ),
            surfaceColor = Color(0xFF1F1F1F),
            accentColor = Color(0xFF76FF03),
            previewColor = Color(0xFF2E7D32)
        )
    )

    fun getTheme(key: String): AppBackgroundTheme {
        return themes.find { it.key == key } ?: themes.first()
    }
}
