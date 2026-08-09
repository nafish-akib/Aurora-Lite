package com.aurora.engine

import com.aurora.data.cache.MetadataCache
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.model.MediaProgress
import com.aurora.data.model.MediaState
import com.aurora.data.model.WebsiteMetadata
import com.aurora.data.service.WebsiteMetadataService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Engine-agnostic metadata collector (does not touch any engine API).
 * Used by every engine module.
 */
class WebsiteMetadataServiceImpl(
    private val metadataCache: MetadataCache,
    private val thumbnailCache: ThumbnailCache
) : WebsiteMetadataService {

    private val _current = MutableStateFlow<WebsiteMetadata?>(null)
    override val currentMetadata: Flow<WebsiteMetadata?> = _current.asStateFlow()

    private var currentUrl: String = ""

    override suspend fun collect(
        url: String,
        title: String,
        faviconUri: String?,
        themeColor: Int?,
        isSecure: Boolean
    ): WebsiteMetadata {
        val existing = metadataCache.get(url)
        val metadata = WebsiteMetadata(
            url = url,
            title = title,
            faviconUri = faviconUri,
            themeColor = themeColor,
            securityInfo = com.aurora.data.model.SecurityInfo(isSecure = isSecure),
            visitCount = existing?.visitCount ?: 0,
            lastVisited = System.currentTimeMillis(),
            dominantColor = themeColor ?: existing?.dominantColor ?: 0xFF1A1A1A.toInt(),
            isBookmarked = existing?.isBookmarked ?: false,
            isPinned = existing?.isPinned ?: false,
            scrollPosition = existing?.scrollPosition ?: 0
        )
        metadataCache.put(metadata)
        _current.value = metadata
        return metadata
    }

    override suspend fun collectAfterLoad(
        url: String,
        waitForReady: Boolean
    ): WebsiteMetadata? {
        if (url.isBlank()) return null

        if (waitForReady) {
            delay(800)
        }

        val title = _current.value?.title ?: ""
        val existing = metadataCache.get(url)
        val isSecure = existing?.securityInfo?.isSecure ?: false

        return collect(
            url = url,
            title = title.ifEmpty { extractTitleFromUrl(url) },
            isSecure = isSecure
        )
    }

    override suspend fun updateMediaState(
        url: String,
        state: MediaState,
        progress: MediaProgress?
    ) {
        metadataCache.update(url) { it.copy(mediaPlaying = state) }
        _current.value = _current.value?.copy(mediaPlaying = state)
    }

    override suspend fun updateScrollPosition(url: String, position: Int) {
        metadataCache.update(url) { it.copy(scrollPosition = position) }
    }

    override fun onPageStart(url: String) {
        currentUrl = url
        val cached = _current.value
        _current.value = if (cached != null) {
            cached.copy(isLoading = true, loadProgress = 0)
        } else {
            WebsiteMetadata(url = url, isLoading = true)
        }
    }

    override fun onPageFinished(url: String, success: Boolean) {
        if (success) {
            _current.value = _current.value?.copy(isLoading = false, loadProgress = 100)
        }
    }

    override fun onTitleChanged(url: String, title: String) {
        _current.value = _current.value?.copy(title = title)
    }

    override fun close() {
        _current.value = null
    }

    private fun extractTitleFromUrl(url: String): String {
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?.split(".")
            ?.firstOrNull()
            ?.replaceFirstChar { it.uppercase() }
            ?: url
    }
}