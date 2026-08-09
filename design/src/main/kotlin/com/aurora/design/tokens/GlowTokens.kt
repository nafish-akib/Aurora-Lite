package com.aurora.design.tokens

import com.aurora.design.tokens.AuroraColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AuroraGlow {
    val defaultColor = AuroraColors.auroraBlue
    val focusColor = AuroraColors.auroraBlue
    val accentColor = AuroraColors.auroraPurple
    val ambientColor = AuroraColors.auroraBlue.copy(alpha = 0.15f)
    val focusGlowAlpha = 0.32f
    val spread: Dp = 55.dp
    val ambientRadius = 800.dp
}
