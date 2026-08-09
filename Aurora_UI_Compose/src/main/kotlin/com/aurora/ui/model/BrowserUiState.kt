package com.aurora.ui.model

import com.aurora.browser.state.ErrorState

data class BrowserUiState(
    val tabs: List<TabUiModel> = emptyList(),
    val activeTabId: String = "",
    val currentUrl: String = "",
    val pageTitle: String = "",
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val isPrivate: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDesktopMode: Boolean = false,
    val onToggleDesktop: () -> Unit = {},
    val errorState: ErrorState = ErrorState.None,
    val toolbarVisible: Boolean = false,
    val recentlyClosed: List<RecentlyClosedUiModel> = emptyList()
)
