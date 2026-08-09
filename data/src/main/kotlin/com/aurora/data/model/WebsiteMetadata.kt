package com.aurora.data.model

import android.graphics.Bitmap

data class WebsiteMetadata(
    val url: String,
    val title: String = "",
    val favicon: Bitmap? = null,
    val faviconUri: String? = null,
    val thumbnail: Bitmap? = null,
    val thumbnailUri: String? = null,
    val dominantColor: Int = 0xFF1A1A1A.toInt(),
    val themeColor: Int? = null,
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 0,
    val scrollPosition: Int = 0,
    val pageLanguage: String? = null,
    val securityInfo: SecurityInfo = SecurityInfo(),
    val mediaPlaying: MediaState = MediaState.None,
    val isBookmarked: Boolean = false,
    val isPinned: Boolean = false,
    val openTabCount: Int = 0,
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

    val shortTitle: String
        get() {
            val d = domain.split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: url
            return title.ifEmpty { d }
        }
}

enum class MediaState {
    None,
    Playing,
    Paused,
    Finished
}

data class SecurityInfo(
    val isSecure: Boolean = false,
    val certificateIssuer: String = "",
    val isVerified: Boolean = false
)

data class MediaProgress(
    val title: String = "",
    val duration: String = "",
    val remaining: String = "",
    val episode: String = "",
    val season: String = ""
)
