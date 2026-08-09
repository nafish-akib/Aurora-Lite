package com.aurora.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class WebsiteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: WebsiteEntity)

    @Query("SELECT * FROM websites WHERE url = :url LIMIT 1")
    abstract suspend fun getByUrl(url: String): WebsiteEntity?

    @Query("SELECT * FROM websites ORDER BY lastVisited DESC LIMIT :limit")
    abstract suspend fun getRecent(limit: Int): List<WebsiteEntity>

    @Query("SELECT * FROM websites WHERE url LIKE '%' || :domain || '%' ORDER BY lastVisited DESC")
    abstract suspend fun getByDomain(domain: String): List<WebsiteEntity>

    @Query("SELECT * FROM websites WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY lastVisited DESC")
    abstract suspend fun search(query: String): List<WebsiteEntity>

    @Query("DELETE FROM websites WHERE url = :url")
    abstract suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM websites")
    abstract suspend fun deleteAll()

    @Query("UPDATE websites SET lastVisited = :time WHERE url = :url")
    abstract suspend fun updateVisitTime(url: String, time: Long)

    @Query("UPDATE websites SET visitCount = visitCount + 1 WHERE url = :url")
    abstract suspend fun incrementVisitCount(url: String)

    @Query("UPDATE websites SET isBookmarked = :isBookmarked WHERE url = :url")
    abstract suspend fun setBookmark(url: String, isBookmarked: Boolean)

    @Query("UPDATE websites SET isPinned = :isPinned WHERE url = :url")
    abstract suspend fun setPinned(url: String, isPinned: Boolean)

    @Query("UPDATE websites SET scrollPosition = :position WHERE url = :url")
    abstract suspend fun updateScrollPosition(url: String, position: Int)

    @Transaction
    open suspend fun upsert(entity: WebsiteEntity) {
        val existing = getByUrl(entity.url)
        if (existing != null) {
            insert(existing.copy(
                title = entity.title.ifBlank { existing.title },
                faviconUri = entity.faviconUri ?: existing.faviconUri,
                thumbnailUri = entity.thumbnailUri ?: existing.thumbnailUri,
                dominantColor = existing.dominantColor,
                themeColor = entity.themeColor ?: existing.themeColor,
                lastVisited = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1,
                scrollPosition = entity.scrollPosition,
                pageLanguage = entity.pageLanguage ?: existing.pageLanguage,
                isSecure = entity.isSecure,
                certificateIssuer = entity.certificateIssuer.ifBlank { existing.certificateIssuer },
                isVerified = entity.isVerified,
                isBookmarked = entity.isBookmarked,
                isPinned = entity.isPinned,
                openTabCount = entity.openTabCount
            ))
        } else {
            insert(entity)
        }
    }
}
