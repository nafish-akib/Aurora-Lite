package com.aurora.ui.viewmodel

import com.aurora.browser.service.HistoryService
import com.aurora.data.DataService
import com.aurora.data.model.HistoryEntry
import com.aurora.ui.mappers.HistoryMapper
import com.aurora.ui.model.HistoryGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryScreenState(
    val groups: List<HistoryGroup> = emptyList(),
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val expandedMenu: Boolean = false
)

class HistoryViewModel(
    private val historyService: HistoryService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _screenState = MutableStateFlow(HistoryScreenState())
    val screenState: StateFlow<HistoryScreenState> = _screenState.asStateFlow()

    init {
        scope.launch {
            historyService.allHistory.collect { entries ->
                _screenState.update { it.copy(entries = entries) }
                buildGroups(entries)
            }
        }
        scope.launch {
            historyService.searchResults.collect { results ->
                if (results != null && _screenState.value.isSearchActive) {
                    _screenState.update { it.copy(entries = results) }
                    buildGroups(results)
                }
            }
        }
    }

    private fun buildGroups(entries: List<HistoryEntry>) {
        scope.launch {
            val urls = entries.map { it.url }.distinct()
            val assets = DataService.browserAssets.getAssets(urls)
            val assetMap = assets.associateBy { it.url }
            val groups = HistoryMapper.toGroupedList(entries, assetMap)
            _screenState.update { it.copy(groups = groups) }
        }
    }

    fun refresh() {
        historyService.refresh()
    }

    fun onSearchQueryChange(query: String) {
        _screenState.update {
            it.copy(searchQuery = query, isSearchActive = query.isNotBlank())
        }
        historyService.search(query)
    }

    fun clearSearch() {
        _screenState.update {
            it.copy(searchQuery = "", isSearchActive = false)
        }
        historyService.clearSearch()
    }

    fun activateSearch() {
        _screenState.update { it.copy(isSearchActive = true) }
    }

    fun deleteEntry(id: Long) {
        historyService.deleteEntry(id)
    }

    fun clearAll() {
        historyService.clearAll()
    }

    fun clearToday() {
        historyService.clearToday()
    }

    fun clearLastHour() {
        historyService.clearLastHour()
    }

    fun toggleMenu() {
        _screenState.update { it.copy(expandedMenu = !it.expandedMenu) }
    }

    fun closeMenu() {
        _screenState.update { it.copy(expandedMenu = false) }
    }

    fun close() {
        scope.cancel()
        historyService.close()
    }
}
