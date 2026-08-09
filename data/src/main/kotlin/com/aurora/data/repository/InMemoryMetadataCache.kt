package com.aurora.data.repository

import com.aurora.data.cache.MetadataCache
import com.aurora.data.model.MediaState
import com.aurora.data.model.WebsiteMetadata

class InMemoryMetadataCache : MetadataCache {
    private val store = mutableMapOf<String, WebsiteMetadata>()

    override suspend fun get(url: String): WebsiteMetadata? = store[url]
    override suspend fun getRecent(limit: Int): List<WebsiteMetadata> =
        store.values.sortedByDescending { it.lastVisited }.take(limit)

    override suspend fun getByDomain(domain: String): List<WebsiteMetadata> =
        store.values.filter { it.url.contains(domain, ignoreCase = true) }

    override suspend fun put(metadata: WebsiteMetadata) { store[metadata.url] = metadata }
    override suspend fun update(url: String, update: (WebsiteMetadata) -> WebsiteMetadata) {
        store[url]?.let { store[url] = update(it) }
    }
    override suspend fun remove(url: String) { store.remove(url) }
    override suspend fun clear() { store.clear() }
    override suspend fun search(query: String): List<WebsiteMetadata> =
        store.values.filter { it.url.contains(query, ignoreCase = true) || it.title.contains(query, ignoreCase = true) }

    override suspend fun updateVisitTime(url: String) {
        store[url]?.let { store[url] = it.copy(lastVisited = System.currentTimeMillis()) }
    }
    override suspend fun incrementVisitCount(url: String) {
        store[url]?.let { store[url] = it.copy(visitCount = it.visitCount + 1) }
    }
    override suspend fun setBookmark(url: String, isBookmarked: Boolean) {
        store[url]?.let { store[url] = it.copy(isBookmarked = isBookmarked) }
    }
    override suspend fun setPinned(url: String, isPinned: Boolean) {
        store[url]?.let { store[url] = it.copy(isPinned = isPinned) }
    }
    override suspend fun updateMediaState(url: String, state: MediaState) {
        store[url]?.let { store[url] = it.copy(mediaPlaying = state) }
    }
    override suspend fun updateScrollPosition(url: String, position: Int) {
        store[url]?.let { store[url] = it.copy(scrollPosition = position) }
    }
}
