package com.aurora.ui.screens

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.service.ActiveDownload
import com.aurora.browser.service.DownloadManager
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.data.model.Download

private enum class DownloadFilter { All, Active, Completed, Video, Audio, Image, Document, App }

@Composable
fun DownloadScreen(
    downloadManager: DownloadManager,
    context: Context,
    onHome: () -> Unit
) {
    val active by downloadManager.active.collectAsState()
    var history by remember { mutableStateOf(emptyList<Download>()) }
    var filter by remember { mutableStateOf(DownloadFilter.All) }

    LaunchedEffect(Unit) {
        downloadManager.getHistory { history = it }
    }

    val allHistory = history
    val filteredActive = when (filter) {
        DownloadFilter.All, DownloadFilter.Active -> active
        else -> emptyList()
    }
    val filteredHistory = when (filter) {
        DownloadFilter.All, DownloadFilter.Completed -> allHistory
        DownloadFilter.Video -> allHistory.filter { DownloadManager.detectFileType(it.fileName, it.mimeType).label == "Video" }
        DownloadFilter.Audio -> allHistory.filter { DownloadManager.detectFileType(it.fileName, it.mimeType).label == "Audio" }
        DownloadFilter.Image -> allHistory.filter { DownloadManager.detectFileType(it.fileName, it.mimeType).label == "Image" }
        DownloadFilter.Document -> allHistory.filter {
            val t = DownloadManager.detectFileType(it.fileName, it.mimeType).label
            t == "PDF" || t == "Document"
        }
        DownloadFilter.App -> allHistory.filter { DownloadManager.detectFileType(it.fileName, it.mimeType).label == "App" }
        else -> emptyList()
    }

    val filters = DownloadFilter.entries

    Column(Modifier.fillMaxSize().background(Color(0xFF0E0F12)).padding(24.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var backFocused by remember { mutableStateOf(false) }
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
                Text(
                    "DOWNLOADS MANAGER",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val activeCount = active.count { it.status == Download.STATUS_DOWNLOADING || it.status == Download.STATUS_PAUSED }
            val completedCount = allHistory.count { it.status == Download.STATUS_COMPLETED }
            Text(
                "${activeCount + completedCount} files",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        var searchQuery by remember { mutableStateOf("") }
        val searchActive = searchQuery.isNotEmpty()
        val displayActive = if (searchActive) {
            filteredActive.filter { it.fileName.contains(searchQuery, true) }
        } else {
            filteredActive
        }
        val displayHistory = if (searchActive) {
            filteredHistory.filter { it.fileName.contains(searchQuery, true) }
        } else {
            filteredHistory
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { f ->
                val selected = f == filter
                val label = when (f) {
                    DownloadFilter.All -> "All"
                    DownloadFilter.Active -> "Active"
                    DownloadFilter.Completed -> "Done"
                    DownloadFilter.Video -> "Video"
                    DownloadFilter.Audio -> "Audio"
                    DownloadFilter.Image -> "Img"
                    DownloadFilter.Document -> "Doc"
                    DownloadFilter.App -> "App"
                }
                val isFirst = f == DownloadFilter.All
                var tabFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .onFocusChanged { tabFocused = it.isFocused }
                        .auroraFocus(
                            state = if (tabFocused || selected) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Tab,
                            focusedStyle = AuroraFocusStyle.TabFocused
                            )
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) AuroraColors.Blue.copy(alpha = 0.2f)
                        else AuroraColors.Neutral900.copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (selected) AuroraColors.Blue.copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { filter = f }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        label, color = if (selected) AuroraColors.Blue else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        }

        if (displayActive.isEmpty() && displayHistory.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Download, null, Modifier.size(48.dp),
                        AuroraColors.Blue.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (searchActive) "No results for \"$searchQuery\"" else "No downloads yet",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (displayActive.isNotEmpty()) {
                    item {
                        Text(
                            "ACTIVE", color = AuroraColors.Blue,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(displayActive, key = { it.id }) { dl ->
                        ActiveDownloadCard(
                            dl,
                            onPause = { downloadManager.pauseDownload(dl.id) },
                            onResume = { downloadManager.resumeDownload(dl.id) },
                            onCancel = { downloadManager.cancelDownload(dl.id) }
                        )
                    }
                }
                if (displayHistory.isNotEmpty()) {
                    item {
                        val headerLabel = when (filter) {
                            DownloadFilter.Completed -> "COMPLETED"
                            DownloadFilter.Video -> "VIDEOS"
                            DownloadFilter.Audio -> "AUDIO"
                            DownloadFilter.Image -> "IMAGES"
                            DownloadFilter.Document -> "DOCUMENTS"
                            DownloadFilter.App -> "APPS"
                            else -> "COMPLETED"
                        }
                        Text(
                            headerLabel, color = AuroraColors.Emerald,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(displayHistory, key = { it.id }) { dl ->
                        HistoryDownloadCard(
                            dl,
                            onOpen = { downloadManager.openFile(context, dl.id, dl.fileName, dl.mimeType) },
                            onRetry = { downloadManager.retryDownload(dl.url, dl.fileName, dl.mimeType); downloadManager.getHistory { history = it } },
                            onDelete = {
                                downloadManager.deleteFromHistory(dl.id, deleteFile = true)
                                history = history.filter { it.id != dl.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveDownloadCard(
    dl: ActiveDownload,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val progress = if (dl.totalBytes > 0) (dl.downloadedBytes.toFloat() / dl.totalBytes).coerceIn(0f, 1f) else 0f
    val isDownloading = dl.status == Download.STATUS_DOWNLOADING
    val isPaused = dl.status == Download.STATUS_PAUSED
    val isFailed = dl.status == Download.STATUS_FAILED
    val fileType = DownloadManager.detectFileType(dl.fileName, dl.mimeType)
    val typeColor = fileTypeColor(fileType.label)
    val typeIcon = fileTypeIcon(fileType.label)

    var cardFocused by remember { mutableStateOf(false) }
    var pauseFocused by remember { mutableStateOf(false) }
    var cancelFocused by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth()
            .onFocusChanged { cardFocused = it.isFocused }
            .auroraFocus(
                state = if (cardFocused) FocusState.Focused else FocusState.Idle,
            idleStyle = AuroraFocusStyle.Surface,
            focusedStyle = AuroraFocusStyle.SurfaceFocused
        )
            .clip(RoundedCornerShape(12.dp))
            .background(AuroraColors.Neutral900.copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { if (isPaused) onResume() else if (isDownloading) onPause() }
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        typeIcon, null, Modifier.size(16.dp), typeColor
                    )
                }
                Column {
                    Text(
                        dl.fileName, color = Color.White, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fileType.label, color = typeColor,
                            fontSize = 8.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            "·", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp
                        )
                        Text(
                            if (isFailed) "Failed"
                            else if (isPaused) "Paused"
                                else if (dl.speed.isNotEmpty()) dl.speed else "Starting...",
                            color = when {
                                isFailed -> AuroraColors.Red
                                isPaused -> Color.White.copy(alpha = 0.4f)
                                else -> Color.White.copy(alpha = 0.6f)
                            },
                            fontSize = 8.sp
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .size(30.dp)
                        .onFocusChanged { pauseFocused = it.isFocused }
                        .auroraFocus(
                            state = if (pauseFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Accent,
                            focusedStyle = AuroraFocusStyle.AccentFocused
                        )
                        .clip(CircleShape)
                        .background(AuroraColors.Neutral800)
                        .clickable { if (isDownloading) onPause() else if (isPaused) onResume() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDownloading) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, Modifier.size(14.dp),
                        if (isPaused) AuroraColors.Emerald else Color.White.copy(alpha = 0.6f)
                    )
                }
                Box(
                    Modifier.size(30.dp)
                        .onFocusChanged { cancelFocused = it.isFocused }
                        .auroraFocus(
                            state = if (cancelFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Accent,
                            focusedStyle = AuroraFocusStyle.AccentFocused
                        )
                        .clip(CircleShape)
                        .background(AuroraColors.Neutral800)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete, null, Modifier.size(14.dp),
                        AuroraColors.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (isDownloading) {
            val infiniteTransition = rememberInfiniteTransition()
            val animatedProgress by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(1200, easing = LinearEasing),
                    RepeatMode.Restart
                )
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AuroraColors.Blue,
                trackColor = AuroraColors.Neutral800,
                strokeCap = StrokeCap.Round
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    isFailed -> AuroraColors.Red
                    isPaused -> Color.White.copy(alpha = 0.3f)
                    else -> AuroraColors.Emerald
                },
                trackColor = AuroraColors.Neutral800,
                strokeCap = StrokeCap.Round
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatFileSize(dl.downloadedBytes),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp, fontFamily = FontFamily.Monospace
            )
            Text(
                formatFileSize(dl.totalBytes),
                color = Color.White.copy(alpha = 0.3f),
            fontSize = 8.sp, fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HistoryDownloadCard(
    dl: Download,
    onOpen: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val type = DownloadManager.detectFileType(dl.fileName, dl.mimeType)
    val typeColor = fileTypeColor(type.label)
    val typeIcon = fileTypeIcon(type.label)
    val isCompleted = dl.status == Download.STATUS_COMPLETED
    val isFailed = dl.status == Download.STATUS_FAILED

    var cardFocused by remember { mutableStateOf(false) }
    var openFocused by remember { mutableStateOf(false) }
    var deleteFocused by remember { mutableStateOf(false) }
    var retryFocused by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth()
            .onFocusChanged { cardFocused = it.isFocused }
            .auroraFocus(
                state = if (cardFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .clickable { if (isCompleted) onOpen() else if (isFailed) onRetry() }
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCompleted) AuroraColors.Neutral900.copy(alpha = 0.5f)
                else AuroraColors.Red.copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                if (isFailed) AuroraColors.Red.copy(alpha = 0.2f)
            else Color.White.copy(alpha = 0.04f),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                .background(typeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(typeIcon, null, Modifier.size(14.dp), typeColor)
        }
        Column(Modifier.weight(1f)) {
            Text(
                dl.fileName, color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    type.label, color = typeColor,
                    fontSize = 8.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    formatFileSize(dl.downloadedBytes),
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 8.sp, fontFamily = FontFamily.Monospace
                )
            }
        }
        if (isCompleted) {
            Box(
                Modifier
                    .onFocusChanged { openFocused = it.isFocused }
                    .auroraFocus(
                        state = if (openFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(AuroraColors.Emerald.copy(alpha = 0.15f))
                    .clickable { onOpen() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.OpenInNew, null, Modifier.size(10.dp),
                        AuroraColors.Emerald
                    )
                    Text("Open", color = AuroraColors.Emerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (isFailed) {
            Box(
                Modifier
                    .onFocusChanged { retryFocused = it.isFocused }
                    .auroraFocus(
                        state = if (retryFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(AuroraColors.Blue.copy(alpha = 0.15f))
                    .clickable { onRetry() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh, null, Modifier.size(10.dp),
                        AuroraColors.Blue
                    )
                    Text("Retry", color = AuroraColors.Blue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(
            Modifier.size(24.dp)
                .onFocusChanged { deleteFocused = it.isFocused }
                .auroraFocus(
                    state = if (deleteFocused) FocusState.Focused else FocusState.Idle,
                    idleStyle = AuroraFocusStyle.Accent,
                    focusedStyle = AuroraFocusStyle.AccentFocused
                )
                .clip(CircleShape)
                .background(AuroraColors.Neutral800)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Delete, null, Modifier.size(12.dp),
                AuroraColors.Red.copy(alpha = 0.6f)
            )
        }
    }
}

private fun fileTypeColor(typeName: String): Color = when (typeName) {
    "Video" -> AuroraColors.Purple
    "Audio" -> AuroraColors.Emerald
    "Image" -> AuroraColors.Blue
    "PDF" -> AuroraColors.Red
    "Archive" -> Color(0xFFFFB800)
    "App" -> AuroraColors.Emerald
    else -> AuroraColors.Blue
}

private fun fileTypeIcon(typeName: String): ImageVector = when (typeName) {
    "Video" -> Icons.Default.VideoFile
    "Audio" -> Icons.Default.MusicNote
    "Image" -> Icons.Default.Image
    "PDF" -> Icons.Default.Description
    "Document" -> Icons.Default.Description
    "App" -> Icons.Default.PhoneAndroid
    "Archive" -> Icons.Default.Download
    else -> Icons.Default.DownloadDone
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
        bytes > 0 -> "$bytes B"
        else -> "0 B"
    }
}