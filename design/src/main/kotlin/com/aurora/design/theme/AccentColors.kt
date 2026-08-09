package com.aurora.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

@Immutable
data class AuroraAccentColors(
    val primary: Color = Color(0xFF4DA3FF),
    val secondary: Color = Color(0xFFA073FF),
    val tertiary: Color = Color(0xFF00D28A),
    val amber: Color = Color(0xFFFFB800),
    val background: Color = Color(0xFF0E0F14)
)

val LocalAccentColors = staticCompositionLocalOf { AuroraAccentColors() }

fun accentColorsFromHex(hex: String): AuroraAccentColors {
    val base = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFF4DA3FF) }
    val (h, s, l) = rgbToHsl(base)
    return AuroraAccentColors(
        primary = base,
        secondary = hslToRgb(h = (h + 40f) % 360f, s = (s * 1.2f).coerceAtMost(1f), l = l),
        tertiary = hslToRgb(h = (h + 120f) % 360f, s = (s * 0.8f), l = l),
        amber = hslToRgb(h = (h + 30f) % 360f, s = 1f, l = l),
        background = Color(0xFF0E0F14)
    )
}

private fun rgbToHsl(c: Color): Triple<Float, Float, Float> {
    val r = c.red; val g = c.green; val b = c.blue
    val max = maxOf(r, g, b); val min = minOf(r, g, b)
    val l = (max + min) / 2f
    if (max == min) return Triple(0f, 0f, l)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        r -> ((g - b) / d + (if (g < b) 6f else 0f)) * 60f
        g -> ((b - r) / d + 2f) * 60f
        else -> ((r - g) / d + 4f) * 60f
    }
    return Triple(h, s, l)
}

private fun hslToRgb(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
}

fun backgroundFromTheme(themeName: String): Color = when (themeName) {
    "Aurora Dark" -> Color(0xFF0E0F14)
    "Midnight Blue" -> Color(0xFF0A0E1A)
    "Graphite Slate" -> Color(0xFF141518)
    "OLED Pure Black" -> Color(0xFF000000)
    else -> Color(0xFF0E0F14)
}
