package com.aurora.data.cache

import com.aurora.data.model.WebsiteMetadata

interface MetadataCache {
    suspend fun get(url: String): WebsiteMetadata?
    suspend fun getRecent(limit: Int = 50): List<WebsiteMetadata>
    suspend fun getByDomain(domain: String): List<WebsiteMetadata>
    suspend fun put(metadata: WebsiteMetadata)
    suspend fun update(url: String, update: (WebsiteMetadata) -> WebsiteMetadata)
    suspend fun remove(url: String)
    suspend fun clear()

    suspend fun search(query: String): List<WebsiteMetadata>

    suspend fun updateVisitTime(url: String)
    suspend fun incrementVisitCount(url: String)
    suspend fun setBookmark(url: String, isBookmarked: Boolean)
    suspend fun setPinned(url: String, isPinned: Boolean)
    suspend fun updateMediaState(url: String, state: com.aurora.data.model.MediaState)
    suspend fun updateScrollPosition(url: String, position: Int)
}
