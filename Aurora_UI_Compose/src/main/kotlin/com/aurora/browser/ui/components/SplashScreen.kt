package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(350); step = 1
        delay(350); step = 2
        delay(350); step = 3
        delay(400); onSplashComplete()
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (step >= 1) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (step >= 2) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (step >= 3) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    val dotScale by animateFloatAsState(
        targetValue = if (step >= 1) 1f else 0.5f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer(scaleX = dotScale, scaleY = dotScale)
                    .alpha(titleAlpha)
                    .background(AuroraColors.Blue, CircleShape)
            )

            Text(
                text = "AURORA",
                style = AuroraTypography.TitleDisplay,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha)
            )

            Text(
                text = "BROWSER",
                style = AuroraTypography.MonoLabel,
                fontSize = 14.sp,
                letterSpacing = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Text(
                text = "Living Glass TV v2.0",
                style = AuroraTypography.MonoLabel,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                color = AuroraColors.Blue.copy(alpha = 0.5f),
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
