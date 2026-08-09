package com.aurora.ui.controller

import com.aurora.data.DataService
import com.aurora.data.model.Favorite
import com.aurora.ui.mappers.LibraryMapper
import com.aurora.ui.model.LibraryUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryController(
    private val historyRepo: com.aurora.data.repository.HistoryRepository = DataService.history,
    private val favoritesRepo: com.aurora.data.repository.FavoriteRepository = DataService.favorites,
    private val downloadRepo: com.aurora.data.repository.DownloadRepository = DataService.downloads
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    fun refresh() {
        scope.launch {
            val favorites = favoritesRepo.getAll()
            val downloads = downloadRepo.getAll()
            val history = historyRepo.getRecent(50)
            _state.update { current ->
                LibraryMapper.toUiState(favorites, downloads, history, current.activeFilter, current.searchQuery)
            }
        }
    }

    fun setFilter(filter: String) {
        _state.update { it.copy(activeFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun removeBookmark(id: Long) {
        scope.launch {
            favoritesRepo.remove(id)
            refresh()
        }
    }

    fun removeDownload(id: Long) {
        scope.launch {
            downloadRepo.remove(id)
            refresh()
        }
    }

    fun clearHistory() {
        scope.launch {
            historyRepo.clear()
            refresh()
        }
    }

    fun addBookmark(url: String, title: String) {
        scope.launch {
            favoritesRepo.add(url, title)
            refresh()
        }
    }
}
