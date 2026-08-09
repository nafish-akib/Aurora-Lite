package com.aurora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.ui.model.HistoryGroup
import com.aurora.ui.model.HistoryUiModel
import com.aurora.ui.viewmodel.HistoryViewModel

private enum class TimeFilter { All, LastHour, Today, Week, Older }

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigate: (String) -> Unit,
    onHome: () -> Unit
) {
    val state by viewModel.screenState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    var multiSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var activeTimeFilter by remember { mutableStateOf(TimeFilter.All) }

    fun exitMultiSelect() { multiSelectMode = false; selectedIds = emptySet() }

    Column(Modifier.fillMaxSize().background(Color(0xFF0E0F12)).padding(24.dp)) {
        var backFocused by remember { mutableStateOf(false) }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .onFocusChanged { backFocused = it.isFocused }
                    .auroraFocus(
                        state = if (backFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Toolbar,
                        focusedStyle = AuroraFocusStyle.ToolbarFocused
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onHome() }
                    .padding(6.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null,
                    Modifier.size(24.dp), Color.White
                )
                Column {
                    Text(
                        "History", color = Color.White,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    val total = state.entries.size
                    val selected = selectedIds.size
                    Text(
                        if (multiSelectMode && selected > 0) "$selected of $total selected"
                        else "$total entries",
                        color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (multiSelectMode) {
                    var cancelFocused by remember { mutableStateOf(false) }
                    var deleteFocused by remember { mutableStateOf(false) }
                    Box(
                        Modifier
                            .onFocusChanged { cancelFocused = it.isFocused }
                            .auroraFocus(
                                state = if (cancelFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Accent,
                                focusedStyle = AuroraFocusStyle.AccentFocused
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuroraColors.Neutral900)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .clickable { exitMultiSelect() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                    }
                    Box(
                        Modifier
                            .onFocusChanged { deleteFocused = it.isFocused }
                            .auroraFocus(
                                state = if (deleteFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Accent,
                                focusedStyle = AuroraFocusStyle.AccentFocused
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuroraColors.Red.copy(alpha = 0.15f))
                            .border(1.dp, AuroraColors.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                selectedIds.forEach { s ->
                                    val id = s.removePrefix("history-").toLongOrNull()
                                    if (id != null) viewModel.deleteEntry(id)
                                }
                                exitMultiSelect()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Delete ${selectedIds.size}",
                            color = AuroraColors.Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    if (state.entries.isNotEmpty()) {
                        var selectFocused by remember { mutableStateOf(false) }
                        var menuFocused by remember { mutableStateOf(false) }
                        Box(
                            Modifier
                                .onFocusChanged { selectFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (selectFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Accent,
                                    focusedStyle = AuroraFocusStyle.AccentFocused
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuroraColors.Blue.copy(alpha = 0.12f))
                                .border(1.dp, AuroraColors.Blue.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable { multiSelectMode = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Select", color = AuroraColors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (state.expandedMenu) {
                            var clearAllFocused by remember { mutableStateOf(false) }
                            var closeMenuFocused by remember { mutableStateOf(false) }
                            Box(
                                Modifier
                                    .onFocusChanged { clearAllFocused = it.isFocused }
                                    .auroraFocus(
                                        state = if (clearAllFocused) FocusState.Focused else FocusState.Idle,
                                        idleStyle = AuroraFocusStyle.Accent,
                                        focusedStyle = AuroraFocusStyle.AccentFocused
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuroraColors.Neutral900)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.clearAll(); viewModel.closeMenu() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Clear All", color = AuroraColors.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                Modifier
                                    .onFocusChanged { closeMenuFocused = it.isFocused }
                                    .auroraFocus(
                                        state = if (closeMenuFocused) FocusState.Focused else FocusState.Idle,
                                        idleStyle = AuroraFocusStyle.Accent,
                                        focusedStyle = AuroraFocusStyle.AccentFocused
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuroraColors.Neutral900)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.closeMenu() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Close", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                            }
                        } else {
                            Box(
                                Modifier
                                    .onFocusChanged { menuFocused = it.isFocused }
                                    .auroraFocus(
                                        state = if (menuFocused) FocusState.Focused else FocusState.Idle,
                                        idleStyle = AuroraFocusStyle.Accent,
                                        focusedStyle = AuroraFocusStyle.AccentFocused
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuroraColors.Neutral900)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleMenu() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Clear...", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        val filters = TimeFilter.entries
        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { f ->
                var tabFocused by remember { mutableStateOf(false) }
                val isActive = activeTimeFilter == f
                val label = when (f) {
                    TimeFilter.All -> "All"
                    TimeFilter.LastHour -> "Last Hour"
                    TimeFilter.Today -> "Today"
                    TimeFilter.Week -> "Week"
                    TimeFilter.Older -> "Older"
                }
                Box(
                    modifier = Modifier
                        .onFocusChanged { tabFocused = it.isFocused }
                        .auroraFocus(
                            state = if (tabFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Tab,
                            focusedStyle = AuroraFocusStyle.TabFocused
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) AuroraColors.Blue.copy(alpha = 0.15f) else AuroraColors.Neutral900.copy(alpha = 0.5f))
                        .border(1.dp, if (isActive) AuroraColors.Blue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable { activeTimeFilter = f }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        label, color = if (isActive) AuroraColors.Blue else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp, fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            var searchFocused by remember { mutableStateOf(false) }
            Box(
                Modifier.fillMaxWidth()
                    .onFocusChanged { searchFocused = it.isFocused }
                    .auroraFocus(
                        state = if (searchFocused || state.isSearchActive) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Surface,
                        focusedStyle = AuroraFocusStyle.SurfaceFocused
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuroraColors.Neutral900)
                    .border(
                        1.dp,
                        if (state.isSearchActive) AuroraColors.Blue.copy(alpha = 0.4f)
                        else Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.activateSearch() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search, null,
                        Modifier.size(14.dp),
                        if (state.isSearchActive) AuroraColors.Blue else Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        if (state.searchQuery.isEmpty()) "Search history..." else state.searchQuery,
                        color = if (state.searchQuery.isEmpty()) Color.White.copy(alpha = 0.4f) else Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isSearchActive) {
                        var closeSearchFocused by remember { mutableStateOf(false) }
                        Icon(
                            Icons.Default.Close, null,
                            Modifier.size(14.dp)
                                .onFocusChanged { closeSearchFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (closeSearchFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Accent,
                                    focusedStyle = AuroraFocusStyle.AccentFocused
                                )
                                .clickable { viewModel.clearSearch() },
                            Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        if (state.groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (state.isSearchActive) "No matching history entries" else "No browsing history yet",
                    color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp, textAlign = TextAlign.Center
                )
            }
        } else {
            val filteredGroups = if (activeTimeFilter == TimeFilter.All) state.groups else {
                state.groups.filter { group ->
                    when (activeTimeFilter) {
                        TimeFilter.LastHour -> group.label == "Today"
                        TimeFilter.Today -> group.label in listOf("Today", "Yesterday")
                        TimeFilter.Week -> group.label in listOf("Today", "Yesterday") || group.label.startsWith("This")
                        TimeFilter.Older -> group.label == "Older" || group.label.contains("month") || group.label.contains("year")
                        else -> true
                    }
                }
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(filteredGroups, key = { it.label }) { group ->
                    HistoryRail(
                        group, onNavigate, viewModel,
                        multiSelectMode = multiSelectMode,
                        selectedIds = selectedIds,
                        onToggleSelect = { entryId ->
                            selectedIds = if (entryId in selectedIds) selectedIds - entryId else selectedIds + entryId
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRail(
    group: HistoryGroup,
    onNavigate: (String) -> Unit,
    viewModel: HistoryViewModel,
    multiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    onToggleSelect: (String) -> Unit = {}
) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(6.dp)
                        .clip(CircleShape)
                        .background(AuroraColors.Blue)
                )
                Text(
                    group.label.uppercase(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                "${group.items.size} pages",
                color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp
            )
        }

        Box(
            Modifier.fillMaxWidth().padding(vertical = 2.dp)
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(group.items, key = { it.id }) { item ->
                HistoryCard(
                    item,
                    onClick = {
                        if (multiSelectMode) {
                            onToggleSelect(item.id)
                        } else {
                            onNavigate(item.url)
                        }
                    },
                    onDelete = {
                        val id = item.id.removePrefix("history-").toLongOrNull()
                        if (id != null) viewModel.deleteEntry(id)
                    },
                    isSelectMode = multiSelectMode,
                    isSelected = item.id in selectedIds
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isSelectMode: Boolean = false,
    isSelected: Boolean = false
) {
    var cardFocused by remember { mutableStateOf(false) }
    var deleteFocused by remember { mutableStateOf(false) }

    Column(
        Modifier.width(210.dp)
            .onFocusChanged { cardFocused = it.isFocused }
            .auroraFocus(
                state = if (cardFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) AuroraColors.Blue.copy(alpha = 0.12f)
                else AuroraColors.Neutral900.copy(alpha = 0.6f)
            )
            .border(
                1.dp,
                if (isSelected) AuroraColors.Blue.copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Box(Modifier.fillMaxWidth().height(100.dp).background(item.accentColor.copy(alpha = 0.12f))) {
            if (item.thumbnail != null) {
                androidx.compose.foundation.Image(
                    painter = BitmapPainter(item.thumbnail.asImageBitmap()),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (isSelectMode) {
                Box(
                    Modifier.align(Alignment.TopStart).padding(8.dp)
                        .size(24.dp).clip(CircleShape)
                        .background(if (isSelected) AuroraColors.Blue else Color.White.copy(alpha = 0.15f))
                        .border(
                            1.5.dp,
                            if (isSelected) AuroraColors.Blue else Color.White.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckBox, null,
                            Modifier.size(14.dp), Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckBoxOutlineBlank, null,
                            Modifier.size(14.dp), Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .size(28.dp).clip(RoundedCornerShape(8.dp))
                    .background(AuroraColors.GlassBackground)
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.faviconBitmap != null) {
                    androidx.compose.foundation.Image(
                        painter = BitmapPainter(item.faviconBitmap.asImageBitmap()),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        item.domain.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                item.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(6.dp).clip(CircleShape).background(item.accentColor)
                )
                Text(
                    item.domain,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${item.actionVerb} ${item.timeText}",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.visitCount > 1) {
                        Text(
                            "${item.visitCount} visits",
                            color = item.accentColor.copy(alpha = 0.7f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                if (!isSelectMode) {
                    Box(
                        Modifier.size(16.dp)
                            .onFocusChanged { deleteFocused = it.isFocused }
                            .auroraFocus(
                                state = if (deleteFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Accent,
                                focusedStyle = AuroraFocusStyle.AccentFocused
                            )
                            .clip(CircleShape)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete, null,
                            Modifier.size(12.dp),
                            Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}