package com.aurora.home

import com.aurora.data.DefaultSite
import com.aurora.data.model.BrowserAsset
import com.aurora.data.model.BrowserSession
import com.aurora.data.model.Download
import com.aurora.data.model.Favorite
import com.aurora.data.model.HistoryEntry

data class HomeState(
    val greeting: GreetingState = GreetingState(),
    val search: SearchState = SearchState(),
    val continueBrowsing: ContinueBrowsingState = ContinueBrowsingState(),
    val favorites: FavoritesState = FavoritesState(),
    val downloads: DownloadsState = DownloadsState(),
    val history: HistoryState = HistoryState(),
    val quickActions: QuickActionsState = QuickActionsState(),
    val bookmarks: List<Favorite> = emptyList(),
    val suggestions: List<String> = emptyList()
)

data class GreetingState(
    val title: String = "",
    val subtitle: String = ""
)

data class SearchState(
    val query: String = "",
    val placeholder: String = "Search or enter address"
)

data class ContinueBrowsingState(
    val assets: List<BrowserAsset> = emptyList()
)

data class FavoritesState(
    val items: List<Favorite> = emptyList(),
    val assets: List<BrowserAsset> = emptyList(),
    val isEditing: Boolean = false
)

data class DownloadsState(
    val items: List<Download> = emptyList()
)

data class HistoryState(
    val items: List<HistoryEntry> = emptyList(),
    val assets: List<BrowserAsset> = emptyList()
)

data class QuickActionsState(
    val actions: List<QuickActionType> = listOf(QuickActionType.HISTORY, QuickActionType.SETTINGS, QuickActionType.AI)
)

enum class QuickActionType { HISTORY, SETTINGS, AI, DIAGNOSTICS }
