package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aurora.browser.ui.theme.AuroraColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Cursor(
    x: Float,
    y: Float,
    isPointerMode: Boolean,
    clicked: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isPointerMode) return

    // Physics-based spring animation matching the 0.25 interpolation factor
    val animatedPos by animateOffsetAsState(
        targetValue = Offset(x, y),
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing)
    )

    // Click expanding wave pulse
    val pulseScale = remember { Animatable(0f) }
    val pulseAlpha = remember { Animatable(1f) }

    LaunchedEffect(clicked) {
        if (clicked) {
            pulseScale.snapTo(0.2f)
            pulseAlpha.snapTo(1f)
            
            // Parallel launch
            launch {
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(350, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                pulseAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(350, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    val density = LocalDensity.current
    val boxSizePx = with(density) { 48.dp.toPx() }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = (animatedPos.x - boxSizePx / 2f).roundToInt(),
                    y = (animatedPos.y - boxSizePx / 2f).roundToInt()
                )
            }
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Click Pulse Wave
        if (clicked) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                        alpha = pulseAlpha.value
                    }
                    .border(2.dp, AuroraColors.Blue.copy(alpha = 0.5f), CircleShape)
            )
        }

        // Main Glowing Orb
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, AuroraColors.Blue, CircleShape)
                // Simulated box-shadow glow
                .graphicsLayer {
                    shadowElevation = 8f
                    spotShadowColor = AuroraColors.Blue
                    ambientShadowColor = AuroraColors.Blue
                },
            contentAlignment = Alignment.Center
        ) {
            // Core Dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(AuroraColors.Blue, CircleShape)
            )
        }

        // Soft Glow Trail Dot (offset slightly behind)
        val deltaX = x - animatedPos.x
        val deltaY = y - animatedPos.y
        
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (deltaX * 0.3f).roundToInt(),
                        y = (deltaY * 0.3f).roundToInt()
                    )
                }
                .size(8.dp)
                .blur(1.dp)
                .background(AuroraColors.Blue.copy(alpha = 0.6f), CircleShape)
        )
    }
}
