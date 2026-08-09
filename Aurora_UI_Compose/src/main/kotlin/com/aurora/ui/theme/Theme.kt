package com.aurora.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.aurora.design.theme.LocalAccentColors

val LocalLargerUI = staticCompositionLocalOf { false }

object AuroraColors {
    val auroraBlue: Color get() = com.aurora.browser.ui.theme.AuroraColors.Blue
    val auroraPurple: Color get() = com.aurora.browser.ui.theme.AuroraColors.Purple
    val auroraEmerald: Color get() = com.aurora.browser.ui.theme.AuroraColors.Emerald
    val auroraAmber: Color get() = com.aurora.browser.ui.theme.AuroraColors.Amber
    val auroraRed: Color get() = com.aurora.browser.ui.theme.AuroraColors.Red
    val neutral950: Color get() = com.aurora.browser.ui.theme.AuroraColors.Neutral950
    val neutral900: Color get() = com.aurora.browser.ui.theme.AuroraColors.Neutral900
    val neutral800: Color get() = com.aurora.browser.ui.theme.AuroraColors.Neutral800
    val neutral700: Color get() = com.aurora.browser.ui.theme.AuroraColors.Neutral700
    val neutral850: Color get() = com.aurora.browser.ui.theme.AuroraColors.Neutral850
    val white: Color get() = Color.White
    val white5: Color get() = Color(0x0DFFFFFF)
    val white10: Color get() = Color(0x1AFFFFFF)
    val white20: Color get() = Color(0x33FFFFFF)
    val white30: Color get() = Color(0x4DFFFFFF)
    val white35: Color get() = Color(0x59FFFFFF)
    val white40: Color get() = Color(0x66FFFFFF)
    val white45: Color get() = Color(0x73FFFFFF)
    val white50: Color get() = Color(0x80FFFFFF)
    val white60: Color get() = Color(0x99FFFFFF)
    val white70: Color get() = Color(0xB3FFFFFF)
    val white80: Color get() = Color(0xCCFFFFFF)
    val white90: Color get() = Color(0xE6FFFFFF)
    val BgRoot: Color get() = com.aurora.browser.ui.theme.AuroraColors.BgRoot
    val GlassBackground: Color get() = com.aurora.browser.ui.theme.AuroraColors.GlassBackground
}

@Composable
fun accentPrimary(): Color = LocalAccentColors.current.primary
@Composable
fun accentSecondary(): Color = LocalAccentColors.current.secondary
@Composable
fun accentTertiary(): Color = LocalAccentColors.current.tertiary
@Composable
fun accentAmber(): Color = LocalAccentColors.current.amber
@Composable
fun accentBackground(): Color = LocalAccentColors.current.background

@Composable
fun AuroraTheme(
    accentColorHex: String = "#4DA3FF",
    themeName: String = "Aurora Dark",
    content: @Composable () -> Unit
) = com.aurora.design.theme.AuroraTheme(
    accentColorHex = accentColorHex,
    themeName = themeName,
    content = content
)
