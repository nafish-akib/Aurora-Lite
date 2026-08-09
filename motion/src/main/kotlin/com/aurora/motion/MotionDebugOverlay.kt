package com.aurora.motion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class MotionDebugFrame(
    val timestamp: Long,
    val durationMs: Float,
    val droppedFrame: Boolean
)

@Composable
fun MotionDebugOverlay(
    activeAnimation: String = "Focus()",
    modifier: Modifier = Modifier
) {
    var fps by remember { mutableStateOf(60f) }
    var recompositionCount by remember { mutableStateOf(0) }
    var droppedFrames by remember { mutableStateOf(0) }

    val frameHistory = remember { mutableStateListOf<MotionDebugFrame>() }
    var lastFrameTime by remember { mutableStateOf(System.nanoTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.nanoTime()
            val elapsed = (now - lastFrameTime) / 1_000_000f
            lastFrameTime = now
            val isDropped = elapsed > 20f
            if (isDropped) droppedFrames++
            frameHistory.add(MotionDebugFrame(now, elapsed, isDropped))
            if (frameHistory.size > 60) frameHistory.removeFirst()
            val recentFrames = frameHistory.takeLast(30)
            fps = if (recentFrames.isEmpty()) 60f
            else (1000f / (recentFrames.sumOf { it.durationMs.toDouble() } / recentFrames.size)).toFloat().coerceIn(0f, 120f)
            recompositionCount++
            delay(50)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .fillMaxWidth(0.3f)
        ) {
            Text(
                text = "Motion Inspector",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Animation: $activeAnimation",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Duration: ${AuroraDurations.focusEnter} ms",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "FPS: ${fps.roundToInt()}",
                color = if (fps > 55f) Color(0xFF34D399) else Color(0xFFF87171),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Dropped: $droppedFrames",
                color = if (droppedFrames < 5) Color(0xFF34D399) else Color(0xFFF87171),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(8.dp))
            MotionFrameGraph(
                frames = frameHistory.takeLast(60),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            )
        }
    }
}

@Composable
private fun MotionFrameGraph(
    frames: List<MotionDebugFrame>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (frames.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val barWidth = w / frames.size
        frames.forEachIndexed { i, frame ->
            val barHeight = (frame.durationMs / 33.33f * h).coerceAtMost(h)
            val color = if (frame.droppedFrame) Color(0xFFF87171) else Color(0xFF34D399)
            drawRect(
                color = color,
                topLeft = Offset(i * barWidth, h - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth - 1f, barHeight)
            )
        }
    }
}
