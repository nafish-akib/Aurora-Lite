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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

@Composable
fun WeatherAnimation(condition: WeatherCondition, modifier: Modifier = Modifier) {
    when (condition) {
        WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN, WeatherCondition.DRIZZLE -> RainAnimation(modifier, condition)
        WeatherCondition.THUNDERSTORM -> ThunderstormAnimation(modifier)
        WeatherCondition.SNOW -> SnowAnimation(modifier)
        WeatherCondition.CLEAR -> SunAnimation(modifier)
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.CLOUDY -> CloudAnimation(modifier)
        WeatherCondition.FOG -> FogOverlay(modifier)
        WeatherCondition.UNKNOWN -> { /* no animation */ }
    }
}

@Composable
fun RainAnimation(modifier: Modifier = Modifier, condition: WeatherCondition = WeatherCondition.RAIN) {
    val dropCount = when (condition) {
        WeatherCondition.HEAVY_RAIN -> 60
        WeatherCondition.RAIN -> 35
        else -> 20
    }
    val drops = remember { List(dropCount) { RainDrop(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.8f + 0.2f, Random.nextFloat() * 30f + 10f) } }
    val infinite = rememberInfiniteTransition(label = "rain")
    val progress by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), "rainP")
    Canvas(modifier.fillMaxSize()) {
        drops.forEach { d ->
            val y = ((d.baseY + progress) % 1.25f - 0.25f) * size.height
            val x = d.baseX * size.width
            drawLine(Color.White.copy(alpha = d.alpha * 0.6f), Offset(x, y), Offset(x + 2f, y + d.length), strokeWidth = 1.5f)
        }
    }
}

@Composable
fun ThunderstormAnimation(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "thunder")
    val flash by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), "flash")
    RainAnimation(modifier, WeatherCondition.HEAVY_RAIN)
    Canvas(modifier.fillMaxSize()) {
        val alpha = if (flash < 0.03f) 0.4f else if (flash < 0.06f) 0.1f else 0f
        if (alpha > 0) drawRect(Color.White.copy(alpha = alpha))
    }
}

@Composable
fun SnowAnimation(modifier: Modifier = Modifier) {
    val flakes = remember { List(40) { SnowFlake(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f, Random.nextFloat() * 1.5f + 0.5f) } }
    val infinite = rememberInfiniteTransition(label = "snow")
    val progress by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), "snowP")
    Canvas(modifier.fillMaxSize()) {
        flakes.forEach { f ->
            val y = ((f.baseY + progress * 0.3f) % 1.1f - 0.1f) * size.height
            val x = (f.baseX + progress * 0.05f) % 1f * size.width
            drawCircle(Color.White.copy(alpha = 0.7f), f.size, Offset(x, y))
        }
    }
}

@Composable
fun SunAnimation(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "sun")
    val glow by infinite.animateFloat(1f, 1.3f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), "glow")
    Canvas(modifier.fillMaxSize()) {
        val cx = size.width * 0.85f
        val cy = size.height * 0.15f
        val r = 50f * glow
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.15f), r * 2.5f, Offset(cx, cy))
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.3f), r * 1.5f, Offset(cx, cy))
        drawCircle(Color(0xFFFFD700), r, Offset(cx, cy))
    }
}

@Composable
fun CloudAnimation(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "cloud")
    val drift by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), "drift")
    val clouds = remember { List(5) { Cloud(Random.nextFloat() * 0.8f, Random.nextFloat() * 0.5f, Random.nextFloat() * 0.5f + 0.3f) } }
    Canvas(modifier.fillMaxSize()) {
        clouds.forEach { c ->
            val x = ((c.baseX + drift * 0.1f) % 1.2f - 0.1f) * size.width
            val y = c.baseY * size.height * 0.4f
            val alpha = c.alpha * 0.25f
            drawCloud(Offset(x, y), c.scale * 80f, Color.White.copy(alpha = alpha))
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, size: Float, color: Color) {
    val r = size * 0.5f
    drawCircle(color, r, Offset(center.x - r * 0.6f, center.y - r * 0.2f))
    drawCircle(color, r * 0.9f, Offset(center.x + r * 0.4f, center.y - r * 0.3f))
    drawCircle(color, r * 1.1f, Offset(center.x, center.y))
    drawCircle(color, r * 0.7f, Offset(center.x - r * 0.5f, center.y + r * 0.1f))
    drawCircle(color, r * 0.6f, Offset(center.x + r * 0.6f, center.y + r * 0.15f))
}

@Composable
fun FogOverlay(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "fog")
    val drift by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), "fogDrift")
    val layers = remember { List(3) { FogLayer(Random.nextFloat(), Random.nextFloat() * 0.4f, Random.nextFloat() * 0.3f + 0.15f) } }
    Canvas(modifier.fillMaxSize()) {
        layers.forEach { layer ->
            val y = layer.baseY * size.height
            val xOffset = ((drift * 0.3f + layer.baseX) % 1f) * size.width * 2f - size.width
            drawRect(Color.White.copy(alpha = layer.alpha * 0.07f), Offset(xOffset, y), Size(size.width * 2f, size.height * 0.35f))
        }
    }
}

private data class RainDrop(val baseX: Float, val baseY: Float, val alpha: Float, val length: Float)
private data class SnowFlake(val baseX: Float, val baseY: Float, val size: Float, val alpha: Float)
private data class Cloud(val baseX: Float, val baseY: Float, val scale: Float, val alpha: Float = 0.6f)
private data class FogLayer(val baseX: Float, val baseY: Float, val alpha: Float)
