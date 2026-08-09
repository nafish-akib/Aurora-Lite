package com.aurora.data.model

import android.graphics.Bitmap

data class BrowserAsset(
    val url: String,
    val title: String = "",
    val favicon: Bitmap? = null,
    val faviconUri: String? = null,
    val thumbnail: Bitmap? = null,
    val dominantColor: Int = 0xFF1A1A1A.toInt(),
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 0,
    val isBookmarked: Boolean = false,
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val loadProgress: Int = 0
) {
    val domain: String
        get() = url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?: url

    val displayDomain: String
        get() = url
            .removePrefix("https://")
            .removePrefix("http://")
            .split("/")
            .firstOrNull()
            ?: url

    companion object {
        fun fromMetadata(metadata: WebsiteMetadata, favicon: Bitmap? = null, thumbnail: Bitmap? = null) = BrowserAsset(
            url = metadata.url,
            title = metadata.title,
            favicon = favicon ?: metadata.favicon,
            faviconUri = metadata.faviconUri,
            thumbnail = thumbnail ?: metadata.thumbnail,
            dominantColor = metadata.dominantColor,
            lastVisited = metadata.lastVisited,
            visitCount = metadata.visitCount,
            isBookmarked = metadata.isBookmarked,
            isPinned = metadata.isPinned,
            isLoading = metadata.isLoading,
            loadProgress = metadata.loadProgress
        )

        fun fromHistory(entry: HistoryEntry, favicon: Bitmap? = null, thumbnail: Bitmap? = null) = BrowserAsset(
            url = entry.url,
            title = entry.title,
            favicon = favicon,
            faviconUri = entry.favicon.takeIf { it.isNotBlank() },
            thumbnail = thumbnail,
            dominantColor = 0xFF1A1A1A.toInt(),
            lastVisited = entry.lastVisited,
            visitCount = entry.visitCount
        )
    }
}
