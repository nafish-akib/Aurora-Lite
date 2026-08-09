package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography

@Composable
fun VoiceListeningOverlay(
    isListening: Boolean,
    message: String = "",
    waveActive: Boolean = false,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isListening) return

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F14).copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = AuroraColors.Purple,
                    modifier = Modifier
                        .size(64.dp)
                        .background(AuroraColors.Purple.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, AuroraColors.Purple, CircleShape)
                        .padding(16.dp)
                )
                if (waveActive) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(pulseAlpha)
                            .border(2.dp, AuroraColors.Purple, CircleShape)
                    )
                }
            }

            Text(
                text = message.ifEmpty { "Listening to speech command..." },
                style = AuroraTypography.Header,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Speak into your remote control. Command examples: \"Open YouTube\", \"Summarize page\", \"Settings\".",
                style = AuroraTypography.Body,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFF1A1C23), AuroraShapes.RoundedMd)
                    .clickable { onCancel() }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Cancel Voice",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
