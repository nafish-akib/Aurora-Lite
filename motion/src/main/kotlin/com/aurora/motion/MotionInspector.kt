package com.aurora.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MotionInspector(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var showInspector by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = enabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .width(220.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aurora Motion Inspector",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = showInspector,
                        onCheckedChange = { showInspector = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4DA3FF)
                        )
                    )
                }

                if (showInspector) {
                    Spacer(Modifier.height(8.dp))

                    MotionDebugOverlay(activeAnimation = "Focus()")

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fast", color = Color(0xFF99A0B0), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("220ms", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Easing", color = Color(0xFF99A0B0), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("Decelerate 1.7", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Spec", color = Color(0xFF99A0B0), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("AuroraTokens.FocusEnter", color = Color(0xFF4DA3FF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
