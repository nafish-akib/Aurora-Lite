package com.aurora.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.aurora.ui.components.theme.AuroraColors

@Composable
fun Focusable(
    id: String,
    focusEngine: FocusEngine,
    group: String = "default",
    order: Int = 0,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusedId by focusEngine.focusedId.collectAsState()
    val isFocused = focusedId == id && enabled

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "focusScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 8f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "focusElevation"
    )

    val node = remember { FocusableNode(id = id, group = group, order = order) }

    DisposableEffect(id) {
        focusEngine.register(node, onSelect = {
            if (enabled) onClick()
        })
        onDispose { focusEngine.unregister(id) }
    }

    val bgColor = when {
        !enabled -> AuroraColors.surface1
        isFocused -> AuroraColors.surface2
        else -> AuroraColors.surface1
    }
    val borderColor = if (isFocused) AuroraColors.primary.copy(alpha = 0.6f) else Color.Transparent
    val contentAlpha = if (enabled) 1f else 0.4f

    Box(
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                shadowElevation = elevation
                clip = true
                alpha = contentAlpha
            }
            .clip(shape)
            .then(
                if (isFocused) {
                    Modifier.border(width = 2.dp, color = borderColor, shape = shape)
                } else Modifier
            )
            .background(color = bgColor, shape = shape)
            .then(
                if (enabled) Modifier.clickable { onClick() } else Modifier
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
