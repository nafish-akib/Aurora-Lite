package com.aurora.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus

data class ContextMenuItem(
    val label: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun ContextMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    items: List<ContextMenuItem>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuroraColors.BgRoot.copy(alpha = 0.97f))
                    .border(1.dp, AuroraColors.BorderGlass, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Column {
                    var closeFocused by remember { mutableStateOf(false) }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aurora Menu", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Box(
                            Modifier.size(24.dp)
                                .onFocusChanged { closeFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (closeFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Accent,
                                    focusedStyle = AuroraFocusStyle.AccentFocused
                                )
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuroraColors.Neutral800)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(12.dp), Color.White.copy(alpha = 0.6f))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    items.forEachIndexed { index, item ->
                        var itemFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { itemFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (itemFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Surface,
                                    focusedStyle = AuroraFocusStyle.SurfaceFocused
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { item.action(); onDismiss() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AuroraColors.Blue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, null, Modifier.size(14.dp), AuroraColors.Blue)
                            }
                            Text(item.label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
