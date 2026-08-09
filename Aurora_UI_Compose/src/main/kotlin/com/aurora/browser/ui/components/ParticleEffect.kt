package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurora.browser.ui.theme.AuroraColors
import kotlin.random.Random

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val angle: Double,
    val velocity: Float
)

@Composable
fun ParticleSparkles(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        particles.forEach { particle ->
            val infiniteTransition = rememberInfiniteTransition()
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            val dx = (kotlin.math.cos(particle.angle) * particle.velocity).toFloat()
            val dy = (kotlin.math.sin(particle.angle) * particle.velocity).toFloat()

            Box(
                modifier = Modifier
                    .offset(
                        x = (particle.x + dx * progress).dp,
                        y = (particle.y + dy * progress).dp
                    )
                    .size(3.dp)
                    .alpha(1f - progress)
                    .graphicsLayer(scaleX = 1f - progress, scaleY = 1f - progress)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

fun generateParticleBurst(
    centerX: Float,
    centerY: Float,
    count: Int = 12
): List<Particle> {
    return (0 until count).map { i ->
        Particle(
            id = i.toLong(),
            x = centerX,
            y = centerY,
            angle = Random.nextDouble() * 360.0,
            velocity = Random.nextFloat() * 4f + 1f
        )
    }
}
