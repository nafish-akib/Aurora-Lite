package com.aurora.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun AuroraTheme(
    accentColorHex: String = "#4DA3FF",
    themeName: String = "Aurora Dark",
    content: @Composable () -> Unit
) {
    val accentColors = accentColorsFromHex(accentColorHex)
    val bgColor = backgroundFromTheme(themeName)
    val scheme = darkColorScheme(
        primary = accentColors.primary,
        secondary = accentColors.secondary,
        tertiary = accentColors.tertiary,
        background = bgColor,
        surface = Color(0xFF17181F),
        onPrimary = bgColor,
        onSecondary = bgColor,
        onBackground = Color(0xFFF2F4F8),
        onSurface = Color(0xFFF2F4F8)
    )
    CompositionLocalProvider(
        LocalAccentColors provides accentColors.copy(background = bgColor)
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
