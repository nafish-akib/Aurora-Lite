package com.aurora.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.ui.model.DownloadUiModel
import com.aurora.ui.model.LibraryUiState

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onBackToDashboard: () -> Unit,
    onOpenViewer: (DownloadUiModel) -> Unit,
    onRemoveDownload: (Long) -> Unit,
    onRemoveBookmark: (Long) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Library",
                    tint = AuroraColors.Purple,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "AURORA UNIFIED LIBRARY",
                        style = AuroraTypography.Header,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Bookmarks, Downloads, Reading List, and History",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }

            var backFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .onFocusChanged { backFocused = it.isFocused }
                    .auroraFocus(
                        state = if (backFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Toolbar,
                        focusedStyle = AuroraFocusStyle.ToolbarFocused
                    )
                    .background(Color(0xFF17181F), AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                    .clickable { onBackToDashboard() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Back to Dashboard",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Videos", "PDFs", "Images", "Articles").forEach { filter ->
                    val isSelected = uiState.activeFilter == filter
                    var filterFocused by remember(filter) { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .onFocusChanged { filterFocused = it.isFocused }
                            .auroraFocus(
                                state = if (filterFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Tab,
                                focusedStyle = AuroraFocusStyle.TabFocused
                            )
                            .background(
                                if (isSelected) AuroraColors.Blue.copy(alpha = 0.15f) else Color(0xFF17181F),
                                AuroraShapes.RoundedSm
                            )
                            .border(
                                1.dp,
                                if (isSelected) AuroraColors.Blue else Color.White.copy(alpha = 0.05f),
                                AuroraShapes.RoundedSm
                            )
                            .clickable { onFilterChange(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            style = AuroraTypography.MonoLabel,
                            color = if (isSelected) AuroraColors.Blue else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.activeFilter == "All" || uiState.activeFilter == "Articles") {
                Column {
                    Text(
                        text = "Bookmarks",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val query = uiState.searchQuery
                        val filtered = if (query.isBlank()) uiState.bookmarks
                        else uiState.bookmarks.filter { it.title.lowercase().contains(query.lowercase()) }
                        filtered.forEach { b ->
                            var cardFocused by remember(b.id) { mutableStateOf(false) }
                            var deleteFocused by remember("del_${b.id}") { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 96.dp)
                                    .onFocusChanged { cardFocused = it.isFocused }
                                    .auroraFocus(
                                        state = if (cardFocused) FocusState.Focused else FocusState.Idle,
                                        idleStyle = AuroraFocusStyle.Surface,
                                        focusedStyle = AuroraFocusStyle.SurfaceFocused
                                    )
                                    .background(Color(0xFF17181F).copy(alpha = 0.6f), AuroraShapes.RoundedLg)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                                    .clickable { onNavigate(b.url) }
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(b.accentColor.copy(alpha = 0.2f), AuroraShapes.RoundedSm)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "General",
                                            style = AuroraTypography.MonoLabel,
                                            color = b.accentColor,
                                            fontSize = 8.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .onFocusChanged { deleteFocused = it.isFocused }
                                            .auroraFocus(
                                                state = if (deleteFocused) FocusState.Focused else FocusState.Idle,
                                                idleStyle = AuroraFocusStyle.Accent,
                                                focusedStyle = AuroraFocusStyle.AccentFocused
                                            )
                                            .clickable { onRemoveBookmark(b.id) }
                                    )
                                }
                                Column {
                                    Text(
                                        text = b.title,
                                        style = AuroraTypography.Body,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = b.url,
                                        style = AuroraTypography.MonoLabel,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.activeFilter == "All" || uiState.activeFilter == "Articles") {
                Column {
                    Text(
                        text = "Reading List (Offline Available)",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.readingList.forEach { entry ->
                            ReadingListCard(
                                title = entry.title,
                                tag = entry.tag,
                                tagColor = entry.tagColor,
                                subtitle = entry.subtitle,
                                onClick = { onNavigate(entry.url) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (uiState.activeFilter == "All" || uiState.activeFilter == "Videos" || uiState.activeFilter == "PDFs" || uiState.activeFilter == "Images") {
                Column {
                    Text(
                        text = "Downloaded Files Sandbox",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.downloads.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A0A0D).copy(alpha = 0.2f), AuroraShapes.RoundedLg)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No downloads yet. Try downloading files inside GitHub or Drive simulator.",
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        uiState.downloads.forEach { dl ->
                            var dlOpenFocused by remember(dl.id) { mutableStateOf(false) }
                            var dlDelFocused by remember("dldel_${dl.id}") { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF17181F).copy(alpha = 0.5f), AuroraShapes.RoundedMd)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dl.fileName,
                                        style = AuroraTypography.Body,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${dl.mimeType} \u2022 ${dl.totalSize} \u2022 ${dl.status}",
                                        style = AuroraTypography.MonoLabel,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (dl.status == "COMPLETED") {
                                        Box(
                                            modifier = Modifier
                                                .onFocusChanged { dlOpenFocused = it.isFocused }
                                                .auroraFocus(
                                                    state = if (dlOpenFocused) FocusState.Focused else FocusState.Idle,
                                                    idleStyle = AuroraFocusStyle.Primary,
                                                    focusedStyle = AuroraFocusStyle.PrimaryFocused
                                                )
                                                .background(AuroraColors.Blue, AuroraShapes.RoundedSm)
                                                .clickable { onOpenViewer(dl) }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Open Built-In",
                                                style = AuroraTypography.MonoLabel,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "DOWNLOADING ${dl.progress}%",
                                            style = AuroraTypography.MonoLabel,
                                            color = AuroraColors.Amber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .onFocusChanged { dlDelFocused = it.isFocused }
                                            .auroraFocus(
                                                state = if (dlDelFocused) FocusState.Focused else FocusState.Idle,
                                                idleStyle = AuroraFocusStyle.Accent,
                                                focusedStyle = AuroraFocusStyle.AccentFocused
                                            )
                                            .clickable { onRemoveDownload(dl.id.toLongOrNull() ?: 0L) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.activeFilter == "All") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History Log",
                            style = AuroraTypography.MonoLabel,
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        var clearFocused by remember { mutableStateOf(false) }
                        Text(
                            text = "Clear All History",
                            style = AuroraTypography.MonoLabel,
                            color = AuroraColors.Red,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .onFocusChanged { clearFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (clearFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Accent,
                                    focusedStyle = AuroraFocusStyle.AccentFocused
                                )
                                .clip(AuroraShapes.RoundedSm)
                                .clickable { onClearHistory() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.history.forEach { h ->
                        var histFocused by remember(h.hashCode()) { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { histFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (histFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Surface,
                                    focusedStyle = AuroraFocusStyle.SurfaceFocused
                                )
                                .background(Color(0xFF17181F).copy(alpha = 0.4f), AuroraShapes.RoundedSm)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
                                .clickable { onNavigate(h.url) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = h.timeText,
                                    style = AuroraTypography.MonoLabel,
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = h.title,
                                    style = AuroraTypography.Body,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = h.url,
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingListCard(
    title: String,
    tag: String,
    tagColor: Color,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rlFocused by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .heightIn(min = 80.dp)
            .onFocusChanged { rlFocused = it.isFocused }
            .auroraFocus(
                state = if (rlFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .background(Color(0xFF17181F).copy(alpha = 0.6f), AuroraShapes.RoundedLg)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag,
                style = AuroraTypography.MonoLabel,
                color = tagColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = AuroraTypography.Body,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = subtitle,
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open",
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}
