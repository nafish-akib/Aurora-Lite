package com.aurora.browser.service

import com.aurora.browser.state.BrowserState
import com.aurora.data.model.BrowserAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnifiedBrowserState(
    val activeTabId: String? = null,
    val browserState: BrowserState = BrowserState(),
    val tabs: List<TabSummary> = emptyList(),
    val recentlyClosed: List<ClosedTab> = emptyList(),
    val isToolbarVisible: Boolean = true,
    val hasNavigated: Boolean = false
)

data class TabSummary(
    val id: String,
    val url: String,
    val title: String,
    val isPrivate: Boolean = false
)

data class ClosedTab(
    val id: String,
    val url: String,
    val title: String
)

class BrowserStateService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val _unified = MutableStateFlow(UnifiedBrowserState())
    val unified: StateFlow<UnifiedBrowserState> = _unified.asStateFlow()

    private var _recentlyClosed = MutableStateFlow<List<ClosedTab>>(emptyList())

    fun updateBrowserState(state: BrowserState) {
        _unified.update { it.copy(browserState = state) }
    }

    fun updateActiveTab(tabId: String?) {
        _unified.update { it.copy(activeTabId = tabId) }
    }

    fun updateTabs(tabs: List<TabSummary>) {
        _unified.update { it.copy(tabs = tabs) }
    }

    fun addClosedTab(url: String, title: String) {
        val tab = ClosedTab(id = "closed-${System.currentTimeMillis()}", url = url, title = title)
        val updated = listOf(tab) + _recentlyClosed.value.take(9)
        _recentlyClosed.value = updated
        _unified.update { it.copy(recentlyClosed = updated) }
    }

    fun setToolbarVisible(visible: Boolean) {
        _unified.update { it.copy(isToolbarVisible = visible) }
    }

    fun setHasNavigated(navigated: Boolean) {
        _unified.update { it.copy(hasNavigated = navigated) }
    }
}
