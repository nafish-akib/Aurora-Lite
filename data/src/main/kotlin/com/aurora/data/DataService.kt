package com.aurora.data

import android.content.Context
import android.util.Log
import com.aurora.data.cache.MetadataCache
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.cache.FaviconCache
import com.aurora.data.db.AppDatabase
import com.aurora.data.preferences.SessionPreferences
import com.aurora.data.repository.DownloadRepository
import com.aurora.data.repository.FavoriteRepository
import com.aurora.data.repository.BrowserAssetRepository
import com.aurora.data.repository.BrowserAssetRepositoryImpl
import com.aurora.data.repository.FaviconCacheImpl
import com.aurora.data.repository.FaviconServiceImpl
import com.aurora.data.repository.HistoryRepository
import com.aurora.data.repository.InMemoryDownloadRepository
import com.aurora.data.repository.InMemoryFaviconCache
import com.aurora.data.repository.InMemoryFavoriteRepository
import com.aurora.data.repository.InMemoryHistoryRepository
import com.aurora.data.repository.InMemoryMetadataCache
import com.aurora.data.repository.InMemorySessionRepository
import com.aurora.data.repository.InMemoryThumbnailCache
import com.aurora.data.repository.MetadataCacheImpl
import com.aurora.data.repository.PersistentBookmarkRepository
import com.aurora.data.repository.PersistentDownloadRepository
import com.aurora.data.repository.PersistentHistoryRepository
import com.aurora.data.repository.PersistentSessionRepository
import com.aurora.data.repository.SessionRepository
import com.aurora.data.repository.ThumbnailCacheImpl
import com.aurora.data.service.FaviconService

object DataService {
    val defaultSites: DefaultSitesProvider = SimpleDefaultSitesProvider()

    var history: HistoryRepository = InMemoryHistoryRepository()
        private set
    var favorites: FavoriteRepository = InMemoryFavoriteRepository()
        private set
    var sessions: SessionRepository = InMemorySessionRepository()
        private set
    var downloads: DownloadRepository = InMemoryDownloadRepository()
        private set
    var metadataCache: MetadataCache = InMemoryMetadataCache()
        private set
    var thumbnailCache: ThumbnailCache = InMemoryThumbnailCache()
        private set
    var faviconCache: FaviconCache = InMemoryFaviconCache()
        private set
    lateinit var faviconService: FaviconService
        private set
    lateinit var browserAssets: BrowserAssetRepository
        private set

    val isFaviconServiceReady: Boolean get() = ::faviconService.isInitialized

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        try {
            val preferences = SessionPreferences(appContext)
            sessions = PersistentSessionRepository(preferences)
        } catch (e: Exception) {
            Log.e("AuroraData", "Session preferences initialization failed; using in-memory sessions", e)
            sessions = InMemorySessionRepository()
        }

        try {
            val db = AppDatabase.getInstance(appContext)
            history = PersistentHistoryRepository(db.historyDao())
            favorites = PersistentBookmarkRepository(db.bookmarkDao())
            downloads = PersistentDownloadRepository(db.downloadDao())
            metadataCache = MetadataCacheImpl(db.websiteDao())
        } catch (e: Exception) {
            Log.e("AuroraData", "Database initialization failed; using in-memory repositories", e)
            history = InMemoryHistoryRepository()
            favorites = InMemoryFavoriteRepository()
            downloads = InMemoryDownloadRepository()
            metadataCache = InMemoryMetadataCache()
        }

        thumbnailCache = try {
            ThumbnailCacheImpl(appContext)
        } catch (e: Exception) {
            Log.e("AuroraData", "Thumbnail cache initialization failed; using in-memory cache", e)
            InMemoryThumbnailCache()
        }
        faviconCache = try {
            FaviconCacheImpl(appContext)
        } catch (e: Exception) {
            Log.e("AuroraData", "Favicon cache initialization failed; using in-memory cache", e)
            InMemoryFaviconCache()
        }
        faviconService = FaviconServiceImpl(faviconCache)
        browserAssets = BrowserAssetRepositoryImpl(metadataCache, faviconCache, thumbnailCache, faviconService)
    }
}
