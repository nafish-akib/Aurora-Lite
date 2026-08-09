package com.aurora.browser.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FocusState { Idle, Focused, Pressed, Disabled }

data class AuroraFocusStyle(
    val scale: Float,
    val glowColor: Color,
    val glowRadius: Dp,
    val borderColor: Color,
    val borderWidth: Dp,
    val shadowElevation: Dp,
    val brightness: Float
) {
    companion object {
        val Surface = AuroraFocusStyle(
            scale = 1f, glowColor = Color.Transparent, glowRadius = 0.dp,
            borderColor = AuroraColors.BorderGlass, borderWidth = 1.dp,
            shadowElevation = 4.dp, brightness = 1f
        )
        val SurfaceFocused = AuroraFocusStyle(
            scale = 1.12f, glowColor = AuroraColors.Blue.copy(alpha = 0.5f), glowRadius = 50.dp,
            borderColor = AuroraColors.Blue.copy(alpha = 0.9f), borderWidth = 3.dp,
            shadowElevation = 48.dp, brightness = 1.25f
        )
        val Tab = AuroraFocusStyle(
            scale = 1f, glowColor = Color.Transparent, glowRadius = 0.dp,
            borderColor = Color.White.copy(alpha = 0.05f), borderWidth = 1.dp,
            shadowElevation = 0.dp, brightness = 1f
        )
        val TabFocused = AuroraFocusStyle(
            scale = 1.10f, glowColor = AuroraColors.Blue.copy(alpha = 0.4f), glowRadius = 40.dp,
            borderColor = AuroraColors.Blue, borderWidth = 3.dp,
            shadowElevation = 32.dp, brightness = 1.20f
        )
        val Toolbar = AuroraFocusStyle(
            scale = 1f, glowColor = Color.Transparent, glowRadius = 0.dp,
            borderColor = Color.White.copy(alpha = 0.05f), borderWidth = 1.dp,
            shadowElevation = 0.dp, brightness = 1f
        )
        val ToolbarFocused = AuroraFocusStyle(
            scale = 1.12f, glowColor = AuroraColors.Blue.copy(alpha = 0.6f), glowRadius = 60.dp,
            borderColor = AuroraColors.Blue.copy(alpha = 0.9f), borderWidth = 3.dp,
            shadowElevation = 40.dp, brightness = 1.30f
        )
        val Primary = AuroraFocusStyle(
            scale = 1f, glowColor = Color.Transparent, glowRadius = 0.dp,
            borderColor = AuroraColors.Blue.copy(alpha = 0.3f), borderWidth = 1.dp,
            shadowElevation = 0.dp, brightness = 1f
        )
        val PrimaryFocused = AuroraFocusStyle(
            scale = 1.10f, glowColor = AuroraColors.Blue.copy(alpha = 0.6f), glowRadius = 70.dp,
            borderColor = AuroraColors.Blue, borderWidth = 3.dp,
            shadowElevation = 48.dp, brightness = 1.30f
        )
        val Accent = AuroraFocusStyle(
            scale = 1f, glowColor = Color.Transparent, glowRadius = 0.dp,
            borderColor = Color.White.copy(alpha = 0.05f), borderWidth = 1.dp,
            shadowElevation = 0.dp, brightness = 1f
        )
        val AccentFocused = AuroraFocusStyle(
            scale = 1.10f, glowColor = AuroraColors.Purple.copy(alpha = 0.5f), glowRadius = 50.dp,
            borderColor = AuroraColors.Purple.copy(alpha = 0.9f), borderWidth = 3.dp,
            shadowElevation = 40.dp, brightness = 1.25f
        )
    }
}

private val FocusEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
private const val FocusDurationMs = 300

fun Modifier.auroraFocus(
    state: FocusState,
    idleStyle: AuroraFocusStyle = AuroraFocusStyle.Surface,
    focusedStyle: AuroraFocusStyle = AuroraFocusStyle.SurfaceFocused,
    shape: RoundedCornerShape = AuroraShapes.Rounded3Xl
): Modifier = composed {
    val transition = updateTransition(state, label = "focusTransition")

    val scale by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "scale"
    ) { s -> if (s == FocusState.Focused) focusedStyle.scale else idleStyle.scale }

    val glow by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "glow"
    ) { s -> if (s == FocusState.Focused) 1f else 0f }

    val borderAlpha by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "borderAlpha"
    ) { s ->
        (if (s == FocusState.Focused) focusedStyle.borderColor else idleStyle.borderColor).alpha
    }

    val shadowDp by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "shadow"
    ) { s ->
        (if (s == FocusState.Focused) focusedStyle.shadowElevation else idleStyle.shadowElevation).value
    }

    val brightness by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "brightness"
    ) { s -> if (s == FocusState.Focused) focusedStyle.brightness else idleStyle.brightness }

    val bWidth by transition.animateFloat(
        transitionSpec = { tween(FocusDurationMs, easing = FocusEasing) },
        label = "borderWidth"
    ) { s ->
        (if (s == FocusState.Focused) focusedStyle.borderWidth else idleStyle.borderWidth).value
    }

    val s = if (state == FocusState.Focused) focusedStyle else idleStyle
    val borderC = s.borderColor.copy(alpha = borderAlpha)
    val currentGlowColor = s.glowColor.copy(alpha = glow * s.glowColor.alpha)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = brightness
        }
        .shadow(Dp(shadowDp), shape)
        .drawBehind {
            val r = s.glowRadius.toPx()
            if (r > 0f && currentGlowColor.alpha > 0.001f) {
                drawRoundRect(
                    color = currentGlowColor,
                    cornerRadius = CornerRadius(shape.topStart.toPx(size, this))
                )
            }
        }
        .border(Dp(bWidth), borderC, shape)
        .then(if (state == FocusState.Disabled) Modifier else Modifier.focusable())
}
