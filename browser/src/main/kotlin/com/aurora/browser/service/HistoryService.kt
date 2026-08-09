package com.aurora.browser.service

import com.aurora.data.model.HistoryEntry
import com.aurora.data.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryService(
    private val repository: HistoryRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _allHistory = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val allHistory: StateFlow<List<HistoryEntry>> = _allHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<HistoryEntry>?>(null)
    val searchResults: StateFlow<List<HistoryEntry>?> = _searchResults.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _allHistory.value = repository.getAll()
        }
    }

    fun recordVisit(url: String, title: String, favicon: String = "", sessionId: String? = null) {
        scope.launch {
            repository.addEntry(url, title, favicon, sessionId)
            refresh()
        }
    }

    fun deleteEntry(id: Long) {
        scope.launch {
            repository.deleteEntry(id)
            refresh()
        }
    }

    fun clearAll() {
        scope.launch {
            repository.clear()
            refresh()
        }
    }

    fun clearToday() {
        scope.launch {
            repository.clearToday()
            refresh()
        }
    }

    fun clearLastHour() {
        scope.launch {
            repository.clearLastHour()
            refresh()
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        scope.launch {
            _searchResults.value = repository.search(query)
        }
    }

    fun clearSearch() {
        _searchResults.value = null
    }

    fun close() {
        scope.cancel()
    }
}
