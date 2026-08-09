package com.aurora.home

import com.aurora.data.DataService
import com.aurora.data.DefaultSite
import com.aurora.data.model.BrowserAsset
import com.aurora.data.model.BrowserSession
import com.aurora.data.model.Download
import com.aurora.data.model.Favorite
import com.aurora.data.model.WebsiteMetadata
import android.graphics.Bitmap

class HomeRepository {

    suspend fun getRecentSessions(limit: Int = 10): List<BrowserSession> =
        DataService.sessions.getRecentSessions(limit)

    suspend fun getRecentHistory(limit: Int = 10): List<com.aurora.data.model.HistoryEntry> =
        DataService.history.getRecent(limit)

    suspend fun getBrowserAssets(urls: List<String>): List<BrowserAsset> {
        DataService.browserAssets.preload(urls)
        return DataService.browserAssets.getAssets(urls, withFavicon = true)
    }

    suspend fun getFavorites(): List<Favorite> =
        DataService.favorites.getAll()

    suspend fun getDownloads(): List<Download> =
        DataService.downloads.getAll()

    suspend fun addFavorite(url: String, title: String) {
        DataService.favorites.add(url, title)
    }

    suspend fun removeFavorite(id: Long) {
        DataService.favorites.remove(id)
    }

    fun getDefaultFavorites(): List<DefaultSite> =
        DataService.defaultSites.getDefaultFavorites()

    fun getPopularSites(): List<DefaultSite> =
        DataService.defaultSites.getPopularSites()
}
