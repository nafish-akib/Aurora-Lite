package com.aurora.ui.mappers

import androidx.compose.ui.graphics.Color
import com.aurora.data.model.Download
import com.aurora.data.model.Favorite
import com.aurora.data.model.HistoryEntry
import com.aurora.ui.model.DownloadUiModel
import com.aurora.ui.model.FavoriteUiModel
import com.aurora.ui.model.HistoryUiModel
import com.aurora.ui.model.LibraryUiState
import com.aurora.ui.model.ReadingListUiModel

object LibraryMapper {
    fun toUiState(
        favorites: List<Favorite>,
        downloads: List<Download>,
        historyEntries: List<HistoryEntry>,
        activeFilter: String,
        searchQuery: String
    ): LibraryUiState {
        return LibraryUiState(
            bookmarks = BookmarkMapper.toUiList(favorites),
            downloads = DownloadMapper.toUiList(downloads),
            history = HistoryMapper.toUiList(historyEntries),
            readingList = defaultReadingList(),
            activeFilter = activeFilter,
            searchQuery = searchQuery
        )
    }

    fun filterFavorites(bookmarks: List<FavoriteUiModel>, query: String): List<FavoriteUiModel> {
        if (query.isBlank()) return bookmarks
        return bookmarks.filter { it.title.lowercase().contains(query.lowercase()) }
    }

    fun filterDownloads(downloads: List<DownloadUiModel>, filter: String): List<DownloadUiModel> {
        return when (filter) {
            "Videos" -> downloads.filter { it.mimeType == "video/mp4" }
            "PDFs" -> downloads.filter { it.mimeType == "application/pdf" }
            "Images" -> downloads.filter { it.mimeType.startsWith("image/") }
            else -> downloads
        }
    }

    private fun defaultReadingList(): List<ReadingListUiModel> = listOf(
        ReadingListUiModel(
            id = "rl-1",
            title = "Living Glass Design \u2014 Wikipedia",
            tag = "Design Spec",
            tagColor = Color(0xFFBB86FC),
            subtitle = "Offline Available \u2022 6 min read",
            url = "https://wikipedia.org/wiki/living-glass"
        ),
        ReadingListUiModel(
            id = "rl-2",
            title = "Low-Memory Solutions \u2014 Wikipedia",
            tag = "Performance Spec",
            tagColor = Color(0xFFBB86FC),
            subtitle = "Offline Available \u2022 8 min read",
            url = "https://wikipedia.org/wiki/performance-architecture"
        )
    )
}
