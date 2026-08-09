package com.aurora.ui.mappers

import androidx.compose.ui.graphics.Color
import com.aurora.browser.state.BrowserState
import com.aurora.data.model.BrowserAsset
import com.aurora.data.model.HistoryEntry
import com.aurora.engine.BrowserSession
import com.aurora.ui.model.BrowserUiState
import com.aurora.ui.model.RecentlyClosedUiModel
import com.aurora.ui.model.TabUiModel
import com.aurora.ui.viewmodel.TabSession

object BrowserMapper {
    fun toUiState(
        tabs: List<TabSession>,
        activeTabId: String,
        browserState: BrowserState,
        isBookmarked: Boolean,
        toolbarVisible: Boolean,
        recentlyClosed: List<HistoryEntry>,
        tabAssets: Map<String, BrowserAsset> = emptyMap(),
        isDesktopMode: Boolean = false,
        onToggleDesktop: () -> Unit = {}
    ): BrowserUiState {
        val uiTabs = tabs.map { ts ->
            val s = ts.controller.state.value
            val asset = tabAssets[s.currentUrl]
            TabUiModel(
                id = ts.id,
                url = s.currentUrl,
                title = s.pageTitle.ifEmpty { "New Tab" },
                domain = asset?.domain ?: extractDomain(s.currentUrl),
                isLoading = s.isLoading,
                progress = s.loadingProgress,
                isPrivate = ts.session.isPrivate,
                isPinned = asset?.isPinned ?: false,
                faviconBitmap = asset?.favicon,
                thumbnail = asset?.thumbnail,
                accentColor = if (asset != null && asset.dominantColor != 0xFF1A1A1A.toInt()) Color(asset.dominantColor) else Color(0xFF4DA3FF)
            )
        }
        return BrowserUiState(
            tabs = uiTabs,
            activeTabId = activeTabId,
            currentUrl = browserState.currentUrl,
            pageTitle = browserState.pageTitle,
            isLoading = browserState.isLoading,
            loadingProgress = browserState.loadingProgress,
            canGoBack = browserState.canGoBack,
            canGoForward = browserState.canGoForward,
            isSecure = browserState.isSecure,
            isPrivate = tabs.find { it.id == activeTabId }?.session?.isPrivate ?: false,
            isBookmarked = isBookmarked,
            isDesktopMode = isDesktopMode,
            onToggleDesktop = onToggleDesktop,
            errorState = browserState.errorState,
            toolbarVisible = toolbarVisible,
            recentlyClosed = recentlyClosed.map { RecentlyClosedUiModel(id = "rc-${it.id}", title = it.title, url = it.url) }
        )
    }

    fun activeSession(tabs: List<TabSession>, activeTabId: String): BrowserSession? =
        tabs.find { it.id == activeTabId }?.session

    private fun extractDomain(url: String): String = url
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .split("/")
        .firstOrNull()
        ?: url
}
