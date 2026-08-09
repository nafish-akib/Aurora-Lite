package com.aurora.data.service

import com.aurora.data.model.WebsiteMetadata
import com.aurora.data.model.MediaProgress
import kotlinx.coroutines.flow.Flow

interface WebsiteMetadataService {

    val currentMetadata: Flow<WebsiteMetadata?>

    suspend fun collect(
        url: String,
        title: String,
        faviconUri: String? = null,
        themeColor: Int? = null,
        isSecure: Boolean = false
    ): WebsiteMetadata

    suspend fun collectAfterLoad(
        url: String,
        waitForReady: Boolean = true
    ): WebsiteMetadata?

    suspend fun updateMediaState(
        url: String,
        state: com.aurora.data.model.MediaState,
        progress: MediaProgress? = null
    )

    suspend fun updateScrollPosition(url: String, position: Int)

    fun onPageStart(url: String)
    fun onPageFinished(url: String, success: Boolean)
    fun onTitleChanged(url: String, title: String)

    fun close()
}
