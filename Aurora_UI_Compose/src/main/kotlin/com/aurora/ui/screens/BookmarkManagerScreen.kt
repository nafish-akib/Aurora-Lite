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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.data.model.BookmarkFolder
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.types.Bookmark

@Composable
fun BookmarkManagerScreen(
    bookmarks: List<Bookmark>,
    folders: List<BookmarkFolder>,
    selectedFolderId: Long?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRemoveFolder: (BookmarkFolder) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onMoveBookmark: (Bookmark, Long) -> Unit,
    onRemoveBookmark: (Bookmark) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedQuery = searchQuery.trim().lowercase()
    val folderList = remember(folders) {
        folders.ifEmpty { listOf(BookmarkFolder(id = 0L, name = "Unsorted")) }
            .distinctBy { it.id }
            .sortedWith(compareBy<BookmarkFolder> { if (it.id == 0L) -1 else it.order }.thenBy { it.name.lowercase() })
    }
    val selectedFolder = folderList.firstOrNull { it.id == selectedFolderId }
    val filteredBookmarks = bookmarks
        .filter { selectedFolderId == null || it.folderId == selectedFolderId }
        .filter { bookmark ->
            normalizedQuery.isEmpty() ||
                bookmark.title.lowercase().contains(normalizedQuery) ||
                bookmark.url.lowercase().contains(normalizedQuery) ||
                bookmark.category.lowercase().contains(normalizedQuery)
        }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BookmarkFolderRail(
            folders = folderList,
            bookmarks = bookmarks,
            selectedFolderId = selectedFolderId,
            onSelectFolder = onSelectFolder,
            onCreateFolder = onCreateFolder,
            onRemoveFolder = onRemoveFolder,
            modifier = Modifier
                .fillMaxHeight()
                .width(248.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookmarkManagerHeader(
                title = selectedFolder?.name ?: "All Bookmarks",
                bookmarkCount = filteredBookmarks.size,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onHome = onHome
            )

            if (filteredBookmarks.isEmpty()) {
                EmptyBookmarkState(
                    hasSearch = searchQuery.isNotBlank(),
                    selectedFolderName = selectedFolder?.name
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookmarks, key = { it.id }) { bookmark ->
                        BookmarkGridCard(
                            bookmark = bookmark,
                            folders = folderList,
                            onOpenBookmark = onOpenBookmark,
                            onMoveBookmark = onMoveBookmark,
                            onRemoveBookmark = onRemoveBookmark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkFolderRail(
    folders: List<BookmarkFolder>,
    bookmarks: List<Bookmark>,
    selectedFolderId: Long?,
    onSelectFolder: (Long?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRemoveFolder: (BookmarkFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCreating by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .background(AuroraColors.neutral950.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("BOOKMARKS", color = AuroraColors.white50, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            var addFolderFocused by remember { mutableStateOf(false) }
            Icon(Icons.Default.Add, contentDescription = "Add folder", tint = AuroraColors.auroraBlue, modifier = Modifier
                .size(20.dp)
                .onFocusChanged { addFolderFocused = it.isFocused }
                .auroraFocus(
                    state = if (addFolderFocused) FocusState.Focused else FocusState.Idle,
                    idleStyle = AuroraFocusStyle.Accent,
                    focusedStyle = AuroraFocusStyle.AccentFocused
                )
                .clickable { isCreating = true })
        }

        FolderPill(
            label = "All Bookmarks",
            count = bookmarks.size,
            isSelected = selectedFolderId == null,
            onClick = { onSelectFolder(null) },
            onDelete = null
        )

        folders.forEach { folder ->
            val count = bookmarks.count { it.folderId == folder.id }
            FolderPill(
                label = folder.name,
                count = count,
                isSelected = selectedFolderId == folder.id,
                onClick = { onSelectFolder(folder.id) },
                onDelete = if (folder.id == 0L) null else ({ onRemoveFolder(folder) })
            )
        }

        if (isCreating) {
            FolderNameEditor(
                value = draftName,
                onValueChange = { draftName = it },
                onConfirm = {
                    val name = draftName.trim()
                    if (name.isNotEmpty()) {
                        onCreateFolder(name)
                        draftName = ""
                        isCreating = false
                    }
                },
                onCancel = {
                    draftName = ""
                    isCreating = false
                }
            )
        }
    }
}

@Composable
private fun FolderPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var pillFocused by remember { mutableStateOf(false) }
    var pillDelFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { pillFocused = it.isFocused }
            .auroraFocus(
                state = if (pillFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .background(if (isSelected) AuroraColors.auroraBlue.copy(alpha = 0.16f) else AuroraColors.neutral900, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) AuroraColors.auroraBlue.copy(alpha = 0.7f) else AuroraColors.white5, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = if (isSelected) AuroraColors.auroraBlue else AuroraColors.white50, modifier = Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = AuroraColors.white90, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count saved", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
        if (onDelete != null) {
            Icon(Icons.Default.Delete, contentDescription = "Delete folder", tint = AuroraColors.white30, modifier = Modifier
                .size(14.dp)
                .onFocusChanged { pillDelFocused = it.isFocused }
                .auroraFocus(
                    state = if (pillDelFocused) FocusState.Focused else FocusState.Idle,
                    idleStyle = AuroraFocusStyle.Accent,
                    focusedStyle = AuroraFocusStyle.AccentFocused
                )
                .clickable { onDelete() })
        }
    }
}

@Composable
private fun FolderNameEditor(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var confirmFocused by remember { mutableStateOf(false) }
    var cancelFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
            .border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("Folder name", color = AuroraColors.white30, fontSize = 11.sp)
                }
                innerTextField()
            }
        )
        Icon(Icons.Default.Check, contentDescription = "Create folder", tint = AuroraColors.auroraEmerald, modifier = Modifier
            .size(16.dp)
            .onFocusChanged { confirmFocused = it.isFocused }
            .auroraFocus(
                state = if (confirmFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Accent,
                focusedStyle = AuroraFocusStyle.AccentFocused
            )
            .clickable { onConfirm() })
        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AuroraColors.white40, modifier = Modifier
            .size(16.dp)
            .onFocusChanged { cancelFocused = it.isFocused }
            .auroraFocus(
                state = if (cancelFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Accent,
                focusedStyle = AuroraFocusStyle.AccentFocused
            )
            .clickable { onCancel() })
    }
}

@Composable
private fun BookmarkManagerHeader(
    title: String,
    bookmarkCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onHome: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AuroraColors.auroraBlue.copy(alpha = 0.16f), CircleShape)
                    .border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = AuroraColors.auroraBlue, modifier = Modifier.size(21.dp))
            }
            Column {
                Text(title, color = AuroraColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$bookmarkCount visible bookmarks", color = AuroraColors.white40, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BookmarkSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.widthIn(min = 220.dp, max = 360.dp)
            )
            var homeFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .onFocusChanged { homeFocused = it.isFocused }
                    .auroraFocus(
                        state = if (homeFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Toolbar,
                        focusedStyle = AuroraFocusStyle.ToolbarFocused
                    )
                    .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
                    .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                    .clickable { onHome() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = AuroraColors.white70, modifier = Modifier.size(14.dp))
                    Text("Dashboard", color = AuroraColors.white70, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BookmarkSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = AuroraColors.white35, modifier = Modifier.size(16.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = AuroraColors.white, fontSize = 11.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("Search title, URL, folder", color = AuroraColors.white35, fontSize = 11.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun BookmarkGridCard(
    bookmark: Bookmark,
    folders: List<BookmarkFolder>,
    onOpenBookmark: (Bookmark) -> Unit,
    onMoveBookmark: (Bookmark, Long) -> Unit,
    onRemoveBookmark: (Bookmark) -> Unit
) {
    val nextFolderId = remember(bookmark.folderId, folders) {
        if (folders.isEmpty()) {
            0L
        } else {
            val currentIndex = folders.indexOfFirst { it.id == bookmark.folderId }.takeIf { it >= 0 } ?: 0
            folders[(currentIndex + 1) % folders.size].id
        }
    }

    var gridFocused by remember { mutableStateOf(false) }
    var moveFocused by remember { mutableStateOf(false) }
    var delFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .height(144.dp)
            .onFocusChanged { gridFocused = it.isFocused }
            .auroraFocus(
                state = if (gridFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .background(AuroraColors.neutral950.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp))
            .clickable { onOpenBookmark(bookmark) }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .background(AuroraColors.neutral800, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(bookmark.category, color = AuroraColors.white50, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = "Move bookmark", tint = AuroraColors.white40, modifier = Modifier
                    .size(16.dp)
                    .onFocusChanged { moveFocused = it.isFocused }
                    .auroraFocus(
                        state = if (moveFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clickable { onMoveBookmark(bookmark, nextFolderId) })
                Icon(Icons.Default.Delete, contentDescription = "Delete bookmark", tint = AuroraColors.white30, modifier = Modifier
                    .size(16.dp)
                    .onFocusChanged { delFocused = it.isFocused }
                    .auroraFocus(
                        state = if (delFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clickable { onRemoveBookmark(bookmark) })
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(bookmark.title, color = AuroraColors.white, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(bookmark.url, color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Open", color = AuroraColors.auroraBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = AuroraColors.auroraBlue, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun EmptyBookmarkState(
    hasSearch: Boolean,
    selectedFolderName: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuroraColors.neutral950.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = AuroraColors.white20, modifier = Modifier.size(36.dp))
            Text(
                text = if (hasSearch) "No matching bookmarks" else "No bookmarks in ${selectedFolderName ?: "this view"}",
                color = AuroraColors.white60,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Use the browser bookmark action to save the current page.", color = AuroraColors.white35, fontSize = 10.sp)
        }
    }
}
