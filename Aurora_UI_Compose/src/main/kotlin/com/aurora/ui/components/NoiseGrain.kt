package com.aurora.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Preview
@Composable
fun NoiseGrainPreview() {
    Box(Modifier.requiredSize(200.dp)) { NoiseGrain() }
}

@Composable
fun NoiseGrain(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val random = Random(42)
                val cellSize = 4f
                val cols = (size.width / cellSize).toInt()
                val rows = (size.height / cellSize).toInt()
                for (col in 0 until cols) {
                    for (row in 0 until rows) {
                        val alpha = random.nextFloat() * 0.012f
                        drawRect(
                            color = Color.White.copy(alpha = alpha),
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
    )
}
