package com.aurora.ui.model

data class HomeUiState(
    val favorites: List<FavoriteUiModel> = emptyList(),
    val greeting: String = "",
    val profileName: String = "",
    val downloads: List<DownloadUiModel> = emptyList(),
    val continueBrowsing: List<ContinueBrowsingUiModel> = emptyList(),
    val history: List<HistoryUiModel> = emptyList(),
    val quickActions: List<QuickActionUiModel> = emptyList(),
    val developerMode: Boolean = false,
    val isOffline: Boolean = false
)
