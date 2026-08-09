package com.aurora.home

import com.aurora.data.DefaultSite
import com.aurora.data.model.BrowserSession
import com.aurora.data.model.Favorite
import com.aurora.home.UrlDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeController {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val repository = HomeRepository()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var onNavigateToUrl: ((String) -> Unit)? = null

    fun setOnNavigateToUrl(callback: (String) -> Unit) {
        onNavigateToUrl = callback
    }

    fun refresh() {
        val greeting = buildGreeting()
        scope.launch {
            val historyEntries = repository.getRecentHistory(10)
            val sessions = repository.getRecentSessions(10)
            val popularSites = if (sessions.isEmpty()) repository.getPopularSites() else emptyList()
            val allUrls = (sessions.map { it.url } + popularSites.map { it.url }).distinct()
            val assets = repository.getBrowserAssets(allUrls)
            val historyUrls = historyEntries.map { it.url }.distinct()
            val historyAssets = if (historyUrls.isNotEmpty()) repository.getBrowserAssets(historyUrls) else emptyList()
            val favorites = repository.getFavorites()
            val favUrls = favorites.map { it.url }.distinct()
            val favAssets = if (favUrls.isNotEmpty()) repository.getBrowserAssets(favUrls) else emptyList()
            _state.update {
                it.copy(
                    greeting = greeting,
                    continueBrowsing = ContinueBrowsingState(assets = assets),
                    favorites = FavoritesState(items = favorites, assets = favAssets),
                    downloads = DownloadsState(items = repository.getDownloads()),
                    history = HistoryState(items = historyEntries, assets = historyAssets)
                )
            }
        }
    }

    private fun extractDomain(url: String): String {
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?: url
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(search = it.search.copy(query = query)) }
    }

    fun submitSearch(input: String) {
        val url = UrlDetector.toUrl(input)
        if (url.isNotEmpty()) {
            onNavigateToUrl?.invoke(url)
        }
    }

    fun openUrl(url: String) {
        onNavigateToUrl?.invoke(url)
    }

    fun openSession(session: BrowserSession) {
        openUrl(session.url)
    }

    fun openFavorite(favorite: Favorite) {
        openUrl(favorite.url)
    }

    fun addFavorite(url: String, title: String) {
        scope.launch {
            repository.addFavorite(url, title)
            refresh()
        }
    }

    fun removeFavorite(id: Long) {
        scope.launch {
            repository.removeFavorite(id)
            refresh()
        }
    }

    fun toggleFavoriteEditing() {
        _state.update { it.copy(favorites = it.favorites.copy(isEditing = !it.favorites.isEditing)) }
    }

    private fun buildGreeting(): GreetingState {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val title = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        return GreetingState(title = title, subtitle = "Ready to continue browsing?")
    }
}
