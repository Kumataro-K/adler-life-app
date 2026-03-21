package com.example.adlerlife.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6E8B84),
    secondary = Color(0xFFB5967E),
    tertiary = Color(0xFF8F7DB0),
    background = Color(0xFFF6F1EA),
    surface = Color(0xFFFFFBF7),
    onPrimary = Color.White,
    onBackground = Color(0xFF2E2A28),
    onSurface = Color(0xFF2E2A28)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5C0B7),
    secondary = Color(0xFFD7B89E),
    tertiary = Color(0xFFC2B2E2),
    background = Color(0xFF171514),
    surface = Color(0xFF24201F),
    onPrimary = Color(0xFF1B2421),
    onBackground = Color(0xFFF1EAE4),
    onSurface = Color(0xFFF1EAE4)
)

@Composable
fun AdlerLifeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
