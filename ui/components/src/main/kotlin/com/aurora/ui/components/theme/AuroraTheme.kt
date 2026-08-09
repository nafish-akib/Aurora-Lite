package com.aurora.ui.components.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuroraColorScheme = darkColorScheme(
    primary = AuroraColors.primary,
    onPrimary = Color.White,
    primaryContainer = AuroraColors.primaryDim,
    secondary = AuroraColors.textSecondary,
    background = AuroraColors.background,
    surface = AuroraColors.surface1,
    surfaceVariant = AuroraColors.surface2,
    onBackground = AuroraColors.textPrimary,
    onSurface = AuroraColors.textPrimary,
    error = AuroraColors.red,
    onError = Color.White
)

@Composable
fun AuroraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuroraColorScheme,
        typography = AuroraTypography,
        content = content
    )
}

