package com.aurora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.ui.focus.FocusBinding
import com.aurora.ui.focus.FocusEngine
import com.aurora.ui.model.TabUiModel

@Composable
fun TabManagementScreen(
    tabs: List<TabUiModel>,
    activeTabId: String,
    focusEngine: FocusEngine,
    onCloseTab: (String) -> Unit,
    onCloseAll: () -> Unit,
    onSwitchToTab: (String) -> Unit,
    onBack: () -> Unit
) {
    var confirmClearAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070A))
            .padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusBinding(
                id = "tab_back",
                focusEngine = focusEngine,
                group = "tab_header",
                order = 0,
                onClick = { onBack() }
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isFocused) AuroraColors.Blue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Tab Manager",
                style = AuroraTypography.Body,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (confirmClearAll) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FocusBinding(
                        id = "tab_confirm_cancel",
                        focusEngine = focusEngine,
                        group = "tab_confirm",
                        order = 0,
                        onClick = { confirmClearAll = false }
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isFocused) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    FocusBinding(
                        id = "tab_confirm_delete",
                        focusEngine = focusEngine,
                        group = "tab_confirm",
                        order = 1,
                        onClick = { onCloseAll(); confirmClearAll = false }
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isFocused) Color(0xFFFF4444) else AuroraColors.Red,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Clear All", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                FocusBinding(
                    id = "tab_clear_all",
                    focusEngine = focusEngine,
                    group = "tab_header",
                    order = 1,
                    onClick = { if (tabs.isNotEmpty()) confirmClearAll = true }
                ) { isFocused ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isFocused) AuroraColors.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All Tabs",
                            tint = if (isFocused) AuroraColors.Red else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (confirmClearAll) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuroraColors.Red.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, AuroraColors.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "This will close all ${tabs.size} open tabs and start a fresh session. Continue?",
                    style = AuroraTypography.Body,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${tabs.size} TABS OPEN",
            style = AuroraTypography.MonoLabel,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (tabs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No open tabs", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(tabs, key = { _, tab -> tab.id }) { index, tab ->
                    val isActive = tab.id == activeTabId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AuroraShapes.RoundedLg)
                            .background(
                                if (isActive) Color(0xFF14151C) else Color(0xFF0E0F14),
                                AuroraShapes.RoundedLg
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FocusBinding(
                            id = "tab_card_$index",
                            focusEngine = focusEngine,
                            group = "tab_list",
                            order = index * 2,
                            onClick = { onSwitchToTab(tab.id) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) { cardFocused ->
                            Row(
                                modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(72.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tab.thumbnail != null) {
                                        androidx.compose.foundation.Image(
                                            painter = BitmapPainter(tab.thumbnail.asImageBitmap()),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AuroraColors.GlassBackground),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (tab.faviconBitmap != null) {
                                                androidx.compose.foundation.Image(
                                                    painter = BitmapPainter(tab.faviconBitmap.asImageBitmap()),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Text(
                                                    tab.domain.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = tab.title,
                                        style = AuroraTypography.Body,
                                        color = if (isActive) AuroraColors.Blue else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tab.domain,
                                            style = AuroraTypography.MonoLabel,
                                            color = Color.White.copy(alpha = 0.35f),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (tab.isPrivate) AuroraColors.Purple.copy(alpha = 0.2f)
                                                    else AuroraColors.Blue.copy(alpha = 0.2f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (tab.isPrivate) "Private" else if (isActive) "Active" else "Idle",
                                                style = AuroraTypography.MonoLabel,
                                                color = if (tab.isPrivate) AuroraColors.Purple
                                                else if (isActive) AuroraColors.Blue
                                                else Color.White.copy(alpha = 0.4f),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (cardFocused) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .fillMaxHeight()
                                            .background(AuroraColors.Blue, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }

                        FocusBinding(
                            id = "tab_close_$index",
                            focusEngine = focusEngine,
                            group = "tab_list",
                            order = index * 2 + 1,
                            onClick = { onCloseTab(tab.id) }
                        ) { closeFocused ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (closeFocused) AuroraColors.Red.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Tab",
                                    tint = if (closeFocused) AuroraColors.Red else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}
