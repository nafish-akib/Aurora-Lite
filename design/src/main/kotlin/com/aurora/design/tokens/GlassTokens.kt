package com.aurora.design.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AuroraGlass {
    val overlayAlpha = 0.03f
    val borderAlpha = 0.05f
    val topHighlightAlpha = 0.05f
    val blurRadius = 130.dp
    val cornerRadius: Dp = 24.dp
    val reflectionColor = Color.White.copy(alpha = overlayAlpha)
    val borderColor = Color.White.copy(alpha = borderAlpha)
    val topHighlightColor = Color.White.copy(alpha = topHighlightAlpha)
}
