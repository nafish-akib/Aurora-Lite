package com.aurora.ui.model

data class LibraryUiState(
    val bookmarks: List<FavoriteUiModel> = emptyList(),
    val downloads: List<DownloadUiModel> = emptyList(),
    val history: List<HistoryUiModel> = emptyList(),
    val readingList: List<ReadingListUiModel> = emptyList(),
    val activeFilter: String = "All",
    val searchQuery: String = ""
)
