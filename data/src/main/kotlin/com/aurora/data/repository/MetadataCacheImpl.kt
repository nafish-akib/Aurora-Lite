package com.aurora.data.repository

import com.aurora.data.cache.MetadataCache
import com.aurora.data.db.WebsiteDao
import com.aurora.data.db.WebsiteEntity
import com.aurora.data.model.MediaState
import com.aurora.data.model.WebsiteMetadata

class MetadataCacheImpl(private val dao: WebsiteDao) : MetadataCache {

    override suspend fun get(url: String): WebsiteMetadata? {
        return dao.getByUrl(url)?.toMetadata()
    }

    override suspend fun getRecent(limit: Int): List<WebsiteMetadata> {
        return dao.getRecent(limit).map { it.toMetadata() }
    }

    override suspend fun getByDomain(domain: String): List<WebsiteMetadata> {
        return dao.getByDomain(domain).map { it.toMetadata() }
    }

    override suspend fun put(metadata: WebsiteMetadata) {
        dao.upsert(
            WebsiteEntity(
                url = metadata.url,
                title = metadata.title,
                faviconUri = metadata.faviconUri,
                thumbnailUri = metadata.thumbnailUri,
                dominantColor = metadata.dominantColor,
                themeColor = metadata.themeColor,
                lastVisited = metadata.lastVisited,
                visitCount = metadata.visitCount,
                scrollPosition = metadata.scrollPosition,
                pageLanguage = metadata.pageLanguage,
                isSecure = metadata.securityInfo.isSecure,
                certificateIssuer = metadata.securityInfo.certificateIssuer,
                isVerified = metadata.securityInfo.isVerified,
                isBookmarked = metadata.isBookmarked,
                isPinned = metadata.isPinned,
                openTabCount = metadata.openTabCount
            )
        )
    }

    override suspend fun update(url: String, update: (WebsiteMetadata) -> WebsiteMetadata) {
        val existing = dao.getByUrl(url) ?: return
        val updated = update(existing.toMetadata())
        dao.insert(
            existing.copy(
                title = updated.title,
                faviconUri = updated.faviconUri,
                thumbnailUri = updated.thumbnailUri,
                dominantColor = updated.dominantColor,
                themeColor = updated.themeColor,
                lastVisited = updated.lastVisited,
                visitCount = updated.visitCount,
                scrollPosition = updated.scrollPosition,
                pageLanguage = updated.pageLanguage,
                isSecure = updated.securityInfo.isSecure,
                certificateIssuer = updated.securityInfo.certificateIssuer,
                isVerified = updated.securityInfo.isVerified,
                isBookmarked = updated.isBookmarked,
                isPinned = updated.isPinned,
                openTabCount = updated.openTabCount
            )
        )
    }

    override suspend fun remove(url: String) {
        dao.deleteByUrl(url)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }

    override suspend fun search(query: String): List<WebsiteMetadata> {
        return dao.search(query).map { it.toMetadata() }
    }

    override suspend fun updateVisitTime(url: String) {
        dao.updateVisitTime(url, System.currentTimeMillis())
    }

    override suspend fun incrementVisitCount(url: String) {
        dao.incrementVisitCount(url)
    }

    override suspend fun setBookmark(url: String, isBookmarked: Boolean) {
        dao.setBookmark(url, isBookmarked)
    }

    override suspend fun setPinned(url: String, isPinned: Boolean) {
        dao.setPinned(url, isPinned)
    }

    override suspend fun updateMediaState(url: String, state: MediaState) {
    }

    override suspend fun updateScrollPosition(url: String, position: Int) {
        dao.updateScrollPosition(url, position)
    }
}
