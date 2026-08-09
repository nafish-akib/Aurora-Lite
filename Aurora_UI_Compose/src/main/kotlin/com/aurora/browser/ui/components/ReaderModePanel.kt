package com.aurora.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.ui.theme.AuroraColors

@Composable
fun ReaderModePanel(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    var fontSize by remember { mutableFloatStateOf(16f) }
    var isDark by remember { mutableFloatStateOf(0f) }

    val bg = if (isDark > 0.5f) Color(0xFF1A1A2E) else Color(0xFFF5F0E8)
    val textColor = if (isDark > 0.5f) Color(0xFFD4D4D4) else Color(0xFF2D2D2D)
    val accent = AuroraColors.auroraBlue

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .clickable(enabled = false) {}
        ) {
            Column(Modifier.fillMaxSize().padding(32.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("READER MODE", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.size(4.dp))
                        Text(title, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.background(if (isDark > 0.5f) accent.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp)).border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { if (isDark == 0f) isDark = 1f else isDark = 0f }) {
                            Text(if (isDark > 0.5f) "Dark" else "Light", color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Close, null, Modifier.size(28.dp).clickable { onDismiss() }, textColor.copy(alpha = 0.6f))
                    }
                }

                Spacer(Modifier.size(24.dp))

                Row(
                    Modifier.fillMaxWidth().background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, null, Modifier.size(18.dp), textColor.copy(alpha = 0.5f))
                    Text("A", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.clickable { fontSize = (fontSize - 1f).coerceAtLeast(10f) })
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 10f..28f,
                        modifier = Modifier.weight(1f).height(24.dp),
                        colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
                    )
                    Text("A", color = textColor.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.clickable { fontSize = (fontSize + 1f).coerceAtMost(28f) })
                    Icon(Icons.Default.ZoomIn, null, Modifier.size(14.dp), textColor.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.size(20.dp))

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = text,
                            color = textColor,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.65f).sp,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}