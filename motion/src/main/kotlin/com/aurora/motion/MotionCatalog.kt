package com.aurora.motion

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object AuroraMotion {

    @Composable
    fun Pulse(periodMs: Long = AuroraDurations.pulsePeriod, minScale: Float = 0.85f): Float {
        val transition = rememberInfiniteTransition(label = "auroraPulse")
        return transition.animateFloat(
            initialValue = 1f, targetValue = minScale,
            animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
            label = "pulseScale"
        ).value
    }

    @Composable
    fun Glow(periodMs: Long = AuroraDurations.glowPeriod, minAlpha: Float = 0.2f, maxAlpha: Float = 0.35f): Float {
        val transition = rememberInfiniteTransition(label = "auroraGlow")
        return transition.animateFloat(
            initialValue = minAlpha, targetValue = maxAlpha,
            animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
            label = "glowAlpha"
        ).value
    }

    @Composable
    fun VoicePulse(periodMs: Long = AuroraDurations.voicePulsePeriod): Float {
        val transition = rememberInfiniteTransition(label = "auroraVoicePulse")
        return transition.animateFloat(
            initialValue = 0.5f, targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
            label = "voicePulseAlpha"
        ).value
    }

    @Composable
    fun CursorBlink(periodMs: Long = AuroraDurations.blinkPeriod): Boolean {
        val transition = rememberInfiniteTransition(label = "auroraCursorBlink")
        val alpha = transition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween((periodMs / 2).toInt(), easing = AuroraEasing.linear), RepeatMode.Reverse),
            label = "cursorBlinkAlpha"
        ).value
        return alpha > 0.5f
    }

    @Composable
    fun SparkleRotation(periodMs: Long = AuroraDurations.sparklePeriod): Float {
        val transition = rememberInfiniteTransition(label = "auroraSparkle")
        return transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(periodMs.toInt(), easing = AuroraEasing.linear)),
            label = "sparkleRotate"
        ).value
    }

    @Composable
    fun DriftOffset(index: Int = 0, periodMs: Long = AuroraDurations.driftPeriod, rangeX: Float = 25f, rangeY: Float = 20f): Pair<Float, Float> {
        var dx by remember { mutableFloatStateOf(0f) }
        var dy by remember { mutableFloatStateOf(0f) }
        val delayMs = index * 3000L
        LaunchedEffect(index) {
            delay(delayMs)
            while (true) {
                val t = (System.currentTimeMillis() % periodMs).toFloat() / periodMs
                dx = (sin(t * kotlin.math.PI * 2) * rangeX).toFloat()
                dy = (-cos(t * kotlin.math.PI * 2) * rangeY).toFloat()
                delay(50)
            }
        }
        return dx to dy
    }

    @Composable
    fun StaggerAlpha(stagger: AuroraStaggerDelay, durationMs: Long = AuroraDurations.staggerBase.toLong()): Float {
        var alpha by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(stagger) {
            delay(stagger.ms)
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < durationMs) {
                alpha = ((System.currentTimeMillis() - start).toFloat() / durationMs).coerceAtMost(1f)
                delay(16)
            }
            alpha = 1f
        }
        return alpha
    }

    @Composable
    fun SettingsSlide(durationMs: Long = AuroraDurations.slideIn.toLong()): Pair<Float, Float> {
        var alpha by remember { mutableFloatStateOf(0f) }
        var translateY by remember { mutableFloatStateOf(24f) }
        LaunchedEffect(Unit) {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < durationMs) {
                val progress = ((System.currentTimeMillis() - start).toFloat() / durationMs).coerceAtMost(1f)
                val eased = 1f - (1f - progress).let { it * it * it }
                alpha = eased
                translateY = 24f * (1f - eased)
                delay(16)
            }
            alpha = 1f
            translateY = 0f
        }
        return alpha to translateY
    }

    @Composable
    fun Counter(target: Int, durationMs: Long = 500): Int {
        var value by remember { mutableIntStateOf(0) }
        LaunchedEffect(target) {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < durationMs) {
                val progress = ((System.currentTimeMillis() - start).toFloat() / durationMs).coerceAtMost(1f)
                value = (progress * target).toInt()
                delay(16)
            }
            value = target
        }
        return value
    }
}

enum class AuroraStaggerDelay(val ms: Long) {
    Stagger0(0), Stagger1(50), Stagger2(100), Stagger3(150), Stagger4(200), Stagger5(250)
}

data class AuroraParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val dx: Float,
    val dy: Float
)

fun generateParticleBurst(x: Float, y: Float, count: Int = 16): List<AuroraParticle> {
    return (0 until count).map { i ->
        val angle = (i / count.toFloat()) * kotlin.math.PI * 2 + (Random.nextFloat() * 0.4f - 0.2f)
        val velocity = 40f + Random.nextFloat() * 60f
        AuroraParticle(
            id = System.nanoTime() + i,
            startX = x,
            startY = y,
            dx = (cos(angle) * velocity).toFloat(),
            dy = (sin(angle) * velocity).toFloat()
        )
    }
}
