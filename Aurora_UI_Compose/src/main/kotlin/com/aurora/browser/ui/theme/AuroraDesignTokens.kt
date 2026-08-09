package com.aurora.browser.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AuroraColors {
    val Blue = Color(0xFF4DA3FF)
    val Purple = Color(0xFFA073FF)
    val Emerald = Color(0xFF00D28A)
    val Amber = Color(0xFFFFB800)
    val Red = Color(0xFFFF5555)

    val BgRoot = Color(0xFF0E0F14)
    val BgCard = Color(0xCC14161C)
    val BgInput = Color(0xFF050507)
    val Neutral950 = Color(0xFF0A0A0F)
    val Neutral900 = Color(0xFF17181F)
    val Neutral850 = Color(0xFF1E2028)
    val Neutral800 = Color(0xFF23252F)
    val Neutral700 = Color(0xFF323543)

    val GlassBackground = Color(0xA60E0F14)
    val BorderGlass = Color(0x10FFFFFF)
    val BorderFocused = Color(0xD94DA3FF)
}

object AuroraShapes {
    val RoundedSm = RoundedCornerShape(8.dp)
    val RoundedMd = RoundedCornerShape(12.dp)
    val RoundedLg = RoundedCornerShape(16.dp)
    val Rounded3Xl = RoundedCornerShape(24.dp)
    val Circular = RoundedCornerShape(50)
}

object AuroraSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 16.dp
    val Lg = 24.dp
    val Xl = 32.dp
}

object AuroraTypography {
    val Sans = FontFamily.SansSerif
    val Display = FontFamily.SansSerif
    val Mono = FontFamily.Monospace

    val TitleDisplay = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp,
        color = Color.White
    )

    val Header = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = (-0.2).sp,
        color = Color.White
    )

    val Body = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = Color.White
    )

    val MonoLabel = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = Color.White
    )
}

object AuroraAnimation {
    var speedMultiplier = 1.0f

    fun getDuration(baseMs: Int): Int {
        return (baseMs * speedMultiplier).toInt()
    }
}

fun Modifier.auroraGlow(
    color: Color = AuroraColors.Blue,
    radius: Dp = 15.dp,
    shapeRadius: Dp = 24.dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            val frameworkPaint = asFrameworkPaint()
            frameworkPaint.color = color.toArgb()
            frameworkPaint.setShadowLayer(
                radius.toPx(),
                0f,
                0f,
                color.toArgb()
            )
        }
        canvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            shapeRadius.toPx(),
            shapeRadius.toPx(),
            paint
        )
    }
}

fun Modifier.auroraGlass(
    shape: RoundedCornerShape = AuroraShapes.Rounded3Xl,
    borderColor: Color = AuroraColors.BorderGlass
) = this
    .clip(shape)
    .background(AuroraColors.GlassBackground)
    .blur(24.dp)
    .border(1.dp, borderColor, shape)

fun Modifier.auroraCardLift(
    isFocused: Boolean,
    onFocusedColor: Color = AuroraColors.Blue,
    shape: RoundedCornerShape = AuroraShapes.Rounded3Xl
) = composed {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.10f else 1.0f,
        animationSpec = tween(
            durationMillis = AuroraAnimation.getDuration(300),
            easing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
        )
    )
    val shadowElevation by animateDpAsState(
            targetValue = if (isFocused) 40.dp else 4.dp,
        animationSpec = tween(AuroraAnimation.getDuration(300))
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) onFocusedColor.copy(alpha = 0.85f) else AuroraColors.BorderGlass,
        animationSpec = tween(AuroraAnimation.getDuration(300))
    )

    val animatedModifier = if (isFocused) {
        this.auroraGlow(color = onFocusedColor.copy(alpha = 0.50f), radius = 50.dp, shapeRadius = 24.dp)
    } else {
        this
    }

    animatedModifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .shadow(shadowElevation, shape)
        .border(1.5.dp, borderColor, shape)
        .background(
            if (isFocused) Color(0xD914161C) else Color(0x9914161C),
            shape
        )
        .then(
            if (isFocused) Modifier.drawBehind {
                val g = 10.dp.toPx()
                val h = g / 2
                val r = (24.dp.toPx() - h).coerceAtLeast(0f)
                drawRoundRect(
                    color = onFocusedColor.copy(alpha = 0.08f),
                    topLeft = Offset(h, h),
                    size = Size(size.width - g, size.height - g),
                    cornerRadius = CornerRadius(r),
                    style = Stroke(width = g),
                    alpha = 0.7f
                )
            } else Modifier
        )
        .then(
            if (isFocused) Modifier.drawBehind {
                val outerG = 16.dp.toPx()
                val outerH = outerG / 2
                val outerR = (24.dp.toPx() - outerH).coerceAtLeast(0f)
                drawRoundRect(
                    color = onFocusedColor.copy(alpha = 0.04f),
                    topLeft = Offset(outerH, outerH),
                    size = Size(size.width - outerG, size.height - outerG),
                    cornerRadius = CornerRadius(outerR),
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = 0.5f
                )
            } else Modifier
        )
}

fun Modifier.focusPing(isFocused: Boolean, color: Color = AuroraColors.Blue) = composed {
    if (!isFocused) return@composed this
    val infiniteTransition = rememberInfiniteTransition()
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    this.drawBehind {
        val r = size.minDimension / 2
        drawCircle(
            color = color.copy(alpha = pingAlpha * 0.3f),
            radius = r * pingScale,
            center = center
        )
    }
}

fun Modifier.auroraLightSweep(
    isFocused: Boolean
) = composed {
    if (!isFocused) return@composed this

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(AuroraAnimation.getDuration(2500), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            start = androidx.compose.ui.geometry.Offset(translateAnim, 0f),
            end = androidx.compose.ui.geometry.Offset(translateAnim + 180f, size.height)
        )
        drawRect(brush = brush)
    }
}

@Composable
fun rememberBreathingGlowAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition()
    return infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
}

fun Modifier.auroraGradientBorder(
    isEnabled: Boolean = true,
    colors: List<Color> = listOf(AuroraColors.Blue, AuroraColors.Emerald, AuroraColors.Purple, AuroraColors.Blue),
    strokeWidth: Dp = 1.5.dp,
    shape: RoundedCornerShape = AuroraShapes.Rounded3Xl
) = composed {
    if (!isEnabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = colors,
            start = androidx.compose.ui.geometry.Offset(animatedOffset, 0f),
            end = androidx.compose.ui.geometry.Offset(animatedOffset + 300f, size.height)
        )
        drawRoundRect(
            brush = brush,
            cornerRadius = CornerRadius(shape.topStart.toPx(size, this)),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.toPx()
            )
        )
    }
}

enum class StaggerStep(val delayMs: Int) {
    ZERO(0), ONE(50), TWO(100), THREE(150), FOUR(200), FIVE(250), SIX(300), SEVEN(350)
}

@Composable
fun rememberFocusScale(
    isFocused: Boolean,
    scaleTarget: Float = 1.12f
): State<Float> {
    return animateFloatAsState(
        targetValue = if (isFocused) scaleTarget else 1f,
        animationSpec = tween(
            durationMillis = AuroraAnimation.getDuration(400),
            easing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
        )
    )
}
