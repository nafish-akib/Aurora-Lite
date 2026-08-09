package com.aurora.motion

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.auroraFocus(isFocused: Boolean, scaleTarget: Float = 1.03f): Modifier = composed {
    val currentScale by animateFloatAsState(
        targetValue = if (isFocused) scaleTarget else 1f,
        animationSpec = AuroraSpec.focusEnter,
        label = "auroraFocus"
    )
    this then graphicsLayer {
        scaleX = currentScale
        scaleY = currentScale
    }
}

fun Modifier.auroraLift(isFocused: Boolean, liftScale: Float = 1.045f, liftY: Float = -6f): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) liftScale else 1f,
        animationSpec = AuroraSpec.cardLift,
        label = "auroraLiftScale"
    )
    this then graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = if (isFocused) liftY else 0f
    }
}

fun Modifier.auroraSweep(isFocused: Boolean): Modifier = composed {
    val sweepOffset by animateFloatAsState(
        targetValue = if (isFocused) 1.5f else -1.5f,
        animationSpec = tween(if (isFocused) 1600 else 0, easing = AuroraEasing.linear),
        label = "auroraSweep"
    )
    this then drawWithContent {
        drawContent()
        if (isFocused) {
            val w = size.width
            val sweepX = w * (sweepOffset + 0.3f) / 2f
            val sweepAlpha = if (sweepOffset < 0.3f) (0.18f * (1f - (sweepOffset + 0.3f) / 0.5f)).coerceAtLeast(0f) else 0f
            if (sweepAlpha > 0) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = sweepAlpha), Color.Transparent),
                        startX = sweepX - w * 0.3f,
                        endX = sweepX + w * 0.3f
                    ),
                    size = size
                )
            }
        }
    }
}

fun Modifier.auroraGlass(): Modifier = composed {
    this then drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.03f), Color.Transparent),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            size = size
        )
        drawRect(
            color = Color.White.copy(alpha = 0.05f),
            topLeft = Offset.Zero,
            size = size.copy(height = 1f)
        )
    }
}

fun Modifier.auroraShimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "auroraShimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(AuroraDurations.shimmerPeriod, easing = AuroraEasing.linear)),
        label = "shimmerOffset"
    )
    this then drawWithContent {
        drawContent()
        val w = size.width
        val x = w * (shimmerOffset * 2 - 0.5f)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.Transparent),
                startX = x - w * 0.3f,
                endX = x + w * 0.3f
            ),
            size = size
        )
    }
}

fun Modifier.auroraPulse(isPulsing: Boolean = true, periodMs: Long = 2250): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "auroraPulseMod")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = if (isPulsing) 0.85f else 1f,
        animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
        label = "pulseScale"
    )
    this then graphicsLayer(scaleX = scale, scaleY = scale)
}

fun Modifier.auroraAmbient(index: Int = 0, periodMs: Long = 4500): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "auroraAmbient$index")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
        label = "ambientScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.35f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
        label = "ambientAlpha"
    )
    this then scale(scale).alpha(alpha)
}

fun Modifier.auroraFade(targetAlpha: Float = 1f): Modifier = composed {
    val currentAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = AuroraSpec.fadeIn,
        label = "auroraFade"
    )
    this then alpha(currentAlpha)
}

fun Modifier.auroraHover(isHovered: Boolean): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = AuroraSpec.focusEnter,
        label = "auroraHover"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0.85f,
        animationSpec = AuroraSpec.focusEnter,
        label = "auroraHoverAlpha"
    )
    this then graphicsLayer(scaleX = scale, scaleY = scale).alpha(alpha)
}

fun Modifier.auroraGlow(
    isGlowing: Boolean = true,
    glowColor: Color = Color.White,
    glowRadius: Dp = 8.dp
): Modifier = composed {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isGlowing) 0.3f else 0f,
        animationSpec = AuroraSpec.normal,
        label = "auroraGlowAlpha"
    )
    this then drawWithContent {
        drawContent()
        if (glowAlpha > 0.01f) {
            drawRect(
                color = glowColor.copy(alpha = glowAlpha),
                size = size,
                alpha = 0.3f
            )
        }
    }
}
