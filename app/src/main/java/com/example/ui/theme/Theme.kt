package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberNeonBlue,
    secondary = CyberNeonGreen,
    tertiary = CyberHotPink,
    background = CyberDarkBg,
    surface = CyberCardBg,
    onPrimary = Color(0xFF020617),
    onSecondary = Color(0xFF020617),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    outline = CyberBorder,
    error = CyberNeonRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Force dark theme with our custom sci-fi palette for consistency
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
