package com.aurora.data.repository

import android.graphics.Bitmap
import com.aurora.data.cache.FaviconCache
import com.aurora.data.cache.MetadataCache
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.model.BrowserAsset
import com.aurora.data.service.FaviconService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface BrowserAssetRepository {
    suspend fun getAsset(url: String, withFavicon: Boolean = true, withThumbnail: Boolean = false): BrowserAsset?
    suspend fun getAssets(urls: List<String>, withFavicon: Boolean = true, withThumbnail: Boolean = false): List<BrowserAsset>
    suspend fun getRecentAssets(limit: Int = 20, withFavicon: Boolean = true, withThumbnail: Boolean = false): List<BrowserAsset>
    suspend fun preload(urls: List<String>)
    fun assetFlow(url: String): Flow<BrowserAsset?>
}

class BrowserAssetRepositoryImpl(
    private val metadataCache: MetadataCache,
    private val faviconCache: FaviconCache,
    private val thumbnailCache: ThumbnailCache,
    private val faviconService: FaviconService
) : BrowserAssetRepository {

    override suspend fun getAsset(url: String, withFavicon: Boolean, withThumbnail: Boolean): BrowserAsset? {
        val metadata = metadataCache.get(url)
        if (metadata == null && !withFavicon && !withThumbnail) return null

        val favicon = if (withFavicon) faviconCache.get(extractDomain(url)) else null
        val thumbnail = if (withThumbnail) thumbnailCache.get(url) else null

        if (metadata != null) return BrowserAsset.fromMetadata(metadata, favicon, thumbnail)
        if (favicon != null || thumbnail != null) return BrowserAsset(url = url, favicon = favicon, thumbnail = thumbnail)
        return null
    }

    override suspend fun getAssets(urls: List<String>, withFavicon: Boolean, withThumbnail: Boolean): List<BrowserAsset> {
        return urls.mapNotNull { getAsset(it, withFavicon, withThumbnail) }
    }

    override suspend fun getRecentAssets(limit: Int, withFavicon: Boolean, withThumbnail: Boolean): List<BrowserAsset> {
        val recent = metadataCache.getRecent(limit)
        return recent.map { metadata ->
            val favicon = if (withFavicon) faviconCache.get(extractDomain(metadata.url)) else null
            val thumbnail = if (withThumbnail) thumbnailCache.get(metadata.url) else null
            BrowserAsset.fromMetadata(metadata, favicon, thumbnail)
        }
    }

    override suspend fun preload(urls: List<String>) {
        val domains = urls.map { extractDomain(it) }.distinct()
        withContext(Dispatchers.IO) {
            faviconCache.warmCache(domains)
        }
        domains.forEach { faviconService.preload(it) }
    }

    override fun assetFlow(url: String): Flow<BrowserAsset?> = flow {
        emit(getAsset(url, withFavicon = true, withThumbnail = false))
    }

    companion object {
        private fun extractDomain(url: String): String = url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?: url
    }
}
