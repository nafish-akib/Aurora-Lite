package com.aurora.ui.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WeatherBackground(condition: WeatherCondition, isNight: Boolean, modifier: Modifier = Modifier, overlayOnly: Boolean = false) {
    val bgColors = when (condition) {
        WeatherCondition.CLEAR -> if (isNight) listOf(Color(0xFF0A0E27), Color(0xFF1A1B3A), Color(0xFF0D1130)) else listOf(Color(0xFF1A6DFF), Color(0xFF4A90FF), Color(0xFF87CEEB))
        WeatherCondition.PARTLY_CLOUDY -> if (isNight) listOf(Color(0xFF141832), Color(0xFF1E2348), Color(0xFF2A3060)) else listOf(Color(0xFF5B9BD5), Color(0xFF7FB8E8), Color(0xFFB0D4F1))
        WeatherCondition.CLOUDY -> listOf(Color(0xFF2A3048), Color(0xFF3D4460), Color(0xFF505878))
        WeatherCondition.FOG -> listOf(Color(0xFF2A3040), Color(0xFF3A4250), Color(0xFF4A5260))
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> listOf(Color(0xFF1E2A3A), Color(0xFF2A3A50), Color(0xFF3A4A60))
        WeatherCondition.HEAVY_RAIN -> listOf(Color(0xFF161E2E), Color(0xFF1E283A), Color(0xFF2A384A))
        WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF0E1424), Color(0xFF161E34), Color(0xFF1E2844))
        WeatherCondition.SNOW -> listOf(Color(0xFF1E2838), Color(0xFF2A3648), Color(0xFF364458))
        WeatherCondition.UNKNOWN -> listOf(Color(0xFF1A1C2A), Color(0xFF2A2C3A), Color(0xFF3A3C4A))
    }
    Canvas(modifier.fillMaxSize()) {
        if (!overlayOnly) drawRect(Brush.verticalGradient(bgColors))
    }
    when (condition) {
        WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> RainParticles(modifier, condition)
        WeatherCondition.DRIZZLE -> RainParticles(modifier, WeatherCondition.DRIZZLE)
        WeatherCondition.THUNDERSTORM -> ThunderstormParticles(modifier)
        WeatherCondition.SNOW -> SnowParticles(modifier)
        WeatherCondition.CLEAR -> if (!isNight) SunParticles(modifier) else StarParticles(modifier)
        WeatherCondition.PARTLY_CLOUDY -> { if (!isNight) SunParticles(modifier); CloudParticles(modifier, condition) }
        WeatherCondition.CLOUDY, WeatherCondition.FOG -> CloudParticles(modifier, condition)
        else -> {}
    }
}

@Composable
fun RainParticles(modifier: Modifier = Modifier, condition: WeatherCondition = WeatherCondition.RAIN) {
    val count = when (condition) { WeatherCondition.HEAVY_RAIN -> 80; WeatherCondition.RAIN -> 50; else -> 25 }
    val drops = remember { List(count) { RainParticle(Random.nextFloat(), -Random.nextFloat(), Random.nextFloat() * 8f + 5f, Random.nextFloat() * 0.5f + 0.3f, Random.nextFloat() * 3f + 2f) } }
    val infinite = rememberInfiniteTransition(label = "rain")
    val cycle by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart), "rainCycle")
    val splashCount = 12
    val splashes = remember { List(splashCount) { SplashParticle(Random.nextFloat(), Random.nextFloat() * 0.3f + 0.65f, 0f, 0f) } }
    Canvas(modifier.fillMaxSize()) {
        drops.forEach { d ->
            val y = ((d.startY + cycle) % 1.3f - 0.3f) * size.height
            val x = d.x * size.width
            val len = d.length * size.height / 1080f
            val wind = d.wind * size.width / 1920f
            drawLine(Color.White.copy(alpha = d.alpha), Offset(x, y), Offset(x + wind, y + len), strokeWidth = 1.5f, cap = StrokeCap.Round)
        }
        splashes.forEachIndexed { i, s ->
            val phase = (cycle * 3f + i * 0.25f) % 1f
            if (phase < 0.15f) {
                val progress = phase / 0.15f
                val alpha = (1f - progress) * 0.4f
                val radius = progress * 25f * size.width / 1920f
                val cx = s.x * size.width
                val cy = (0.85f + Random.nextFloat() * 0.1f) * size.height
                drawCircle(Color.White.copy(alpha = alpha), radius, Offset(cx, cy), style = Stroke(1.5f))
            }
        }
    }
}

@Composable
fun SnowParticles(modifier: Modifier = Modifier) {
    val count = 50
    val flakes = remember { List(count) { SnowParticle(Random.nextFloat(), -Random.nextFloat(), Random.nextFloat() * 4f + 1.5f, Random.nextFloat() * 0.8f + 0.2f, Random.nextFloat() * 30f, Random.nextFloat() * 0.5f + 0.5f) } }
    val infinite = rememberInfiniteTransition(label = "snow")
    val cycle by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), "snowCycle")
    Canvas(modifier.fillMaxSize()) {
        flakes.forEach { f ->
            val fallY = ((f.startY + cycle * 0.4f) % 1.25f - 0.25f) * size.height
            val swayX = kotlin.math.sin((cycle * f.sway * PI.toFloat() * 2f)).toFloat() * 15f * size.width / 1920f
            val x = (f.x * size.width + swayX).coerceIn(0f, size.width)
            drawCircle(Color.White.copy(alpha = f.alpha), f.size, Offset(x, fallY))
            drawCircle(Color.White.copy(alpha = f.alpha * 0.3f), f.size * 2f, Offset(x, fallY))
        }
    }
}

@Composable
fun SunParticles(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "sun")
    val pulse by infinite.animateFloat(0.85f, 1.15f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), "sunPulse")
    val rayRotate by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), "sunRay")
    Canvas(modifier.fillMaxSize()) {
        val cx = size.width * 0.82f; val cy = size.height * 0.18f; val r = 45f * pulse
        for (i in 0 until 12) {
            val angle = Math.toRadians(i * 30.0 + rayRotate)
            val cosA = cos(angle).toFloat(); val sinA = sin(angle).toFloat()
            val startR = r * 1.2f; val endR = r * (1.8f + kotlin.math.sin(pulse * PI * 2f).toFloat() * 0.3f)
            drawLine(Color(0xFFFFD700).copy(alpha = 0.25f), Offset(cx + cosA * startR, cy + sinA * startR), Offset(cx + cosA * endR, cy + sinA * endR), strokeWidth = 2.5f, cap = StrokeCap.Round)
        }
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.08f), r * 3.5f, Offset(cx, cy))
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.15f), r * 2.2f, Offset(cx, cy))
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.35f), r * 1.3f, Offset(cx, cy))
        drawCircle(Color(0xFFFFF0), r, Offset(cx, cy))
    }
}

@Composable
fun StarParticles(modifier: Modifier = Modifier) {
    val stars = remember { List(60) { StarParticle(Random.nextFloat(), Random.nextFloat() * 0.5f, Random.nextFloat() * 0.6f + 0.4f, Random.nextFloat() * 0.5f + 0.5f) } }
    val infinite = rememberInfiniteTransition(label = "star")
    val twinkle by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), "twinkle")
    Canvas(modifier.fillMaxSize()) {
        stars.forEachIndexed { i, s ->
            val alpha = s.baseAlpha * (0.5f + 0.5f * sin((twinkle + s.phase) * PI * 2f).toFloat())
            drawCircle(Color.White.copy(alpha = alpha), 1.5f, Offset(s.x * size.width, s.y * size.height))
        }
        val moonCx = size.width * 0.82f; val moonCy = size.height * 0.18f; val mr = 35f
        drawCircle(Color(0xFFF5F0D0).copy(alpha = 0.15f), mr * 1.6f, Offset(moonCx, moonCy))
        drawCircle(Color(0xFFF5F0D0), mr, Offset(moonCx, moonCy))
        drawCircle(Color(0xFF0A0E27), mr * 0.85f, Offset(moonCx - mr * 0.3f, moonCy - mr * 0.15f))
    }
}

@Composable
fun CloudParticles(modifier: Modifier = Modifier, condition: WeatherCondition = WeatherCondition.CLOUDY) {
    val count = when (condition) { WeatherCondition.CLOUDY -> 8; WeatherCondition.FOG -> 5; else -> 5 }
    val clouds = remember { List(count) { CloudParticle(Random.nextFloat(), Random.nextFloat() * 0.4f, Random.nextFloat() * 0.5f + 0.5f, Random.nextFloat() * 0.3f + 0.3f) } }
    val infinite = rememberInfiniteTransition(label = "cloud")
    val drift by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart), "cloudDrift")
    val alphaMod = if (condition == WeatherCondition.FOG) 0.18f else 0.3f
    Canvas(modifier.fillMaxSize()) {
        clouds.forEach { c ->
            val x = ((c.x + drift * 0.08f) % 1.4f - 0.2f) * size.width
            val y = c.y * size.height * 0.4f
            val scale = c.scale * 70f * size.width / 1920f
            drawCloudCluster(Offset(x, y), scale, Color.White.copy(alpha = c.alpha * alphaMod))
        }
    }
}

private fun DrawScope.drawCloudCluster(center: Offset, r: Float, color: Color) {
    drawCircle(color, r * 0.9f, Offset(center.x - r * 0.5f, center.y))
    drawCircle(color, r, Offset(center.x + r * 0.3f, center.y - r * 0.15f))
    drawCircle(color, r * 0.85f, Offset(center.x + r * 0.1f, center.y + r * 0.1f))
    drawCircle(color, r * 0.7f, Offset(center.x - r * 0.6f, center.y + r * 0.05f))
    drawCircle(color, r * 0.65f, Offset(center.x - r * 0.2f, center.y - r * 0.4f))
}

@Composable
fun ThunderstormParticles(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "storm")
    val flash by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), "flashId")
    val flash2 by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart), "flash2")
    RainParticles(modifier, WeatherCondition.HEAVY_RAIN)
    Canvas(modifier.fillMaxSize()) {
        val alpha1 = if (flash < 0.015f) 0.5f else if (flash < 0.03f) 0.15f else 0f
        val alpha2 = if (flash2 < 0.01f) 0.6f else if (flash2 < 0.025f) 0.2f else 0f
        if (alpha1 > 0 || alpha2 > 0) {
            drawRect(Color.White.copy(alpha = (alpha1 + alpha2).coerceAtMost(0.7f)))
            if (alpha1 > 0.3f) {
                val boltX = size.width * 0.3f
                val boltY = size.height * 0.1f
                val path = Path().apply {
                    moveTo(boltX, boltY); lineTo(boltX + 15f, boltY + 80f); lineTo(boltX - 5f, boltY + 85f)
                    lineTo(boltX + 25f, boltY + 200f); lineTo(boltX + 5f, boltY + 210f); lineTo(boltX + 40f, boltY + 350f)
                }
                drawPath(path, Color.White.copy(alpha = 0.9f), style = Stroke(3f))
                drawPath(path, Color(0xFFF5F0D0).copy(alpha = 0.4f), style = Stroke(6f))
            }
        }
    }
}

private data class RainParticle(val x: Float, val startY: Float, val length: Float, val alpha: Float, val wind: Float)
private data class SplashParticle(val x: Float, val baseY: Float, val radius: Float, val alpha: Float)
private data class SnowParticle(val x: Float, val startY: Float, val size: Float, val alpha: Float, val sway: Float, val phase: Float = 0f)
private data class StarParticle(val x: Float, val y: Float, val baseAlpha: Float, val phase: Float)
private data class CloudParticle(val x: Float, val y: Float, val scale: Float, val alpha: Float)
