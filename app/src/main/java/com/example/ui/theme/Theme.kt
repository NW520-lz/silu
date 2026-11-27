package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SiluDarkColorScheme = darkColorScheme(
    primary = SiluEmerald,
    onPrimary = SiluBlack,
    secondary = SiluTextSecondary,
    onSecondary = SiluBlack,
    background = SiluBlack,
    onBackground = SiluTextPrimary,
    surface = SiluSurface,
    onSurface = SiluTextPrimary,
    surfaceVariant = SiluSurfaceElevated,
    onSurfaceVariant = SiluTextSecondary,
    outline = SiluBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SiluDarkColorScheme,
        typography = SiluTypography,
        content = content
    )
}
