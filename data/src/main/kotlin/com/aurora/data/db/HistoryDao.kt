package com.aurora.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY lastVisited DESC")
    abstract suspend fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    abstract suspend fun getByUrl(url: String): HistoryEntity?

    @Query("SELECT * FROM history WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY lastVisited DESC")
    abstract suspend fun search(query: String): List<HistoryEntity>

    @Query("DELETE FROM history WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Query("DELETE FROM history WHERE lastVisited >= :since")
    abstract suspend fun deleteVisitedSince(since: Long)

    @Query("DELETE FROM history")
    abstract suspend fun deleteAll()

    @Query("SELECT * FROM history ORDER BY lastVisited DESC LIMIT :limit")
    abstract suspend fun getRecent(limit: Int): List<HistoryEntity>

    @Transaction
    open suspend fun upsert(url: String, title: String, faviconUrl: String, sessionId: String?, isPrivate: Boolean) {
        val existing = getByUrl(url)
        if (existing != null) {
            insert(existing.copy(
                title = title.ifBlank { existing.title },
                lastVisited = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1,
                faviconUrl = faviconUrl.ifBlank { existing.faviconUrl },
                sessionId = sessionId ?: existing.sessionId,
                isPrivate = isPrivate
            ))
        } else {
            insert(HistoryEntity(
                url = url,
                title = title,
                faviconUrl = faviconUrl,
                sessionId = sessionId,
                isPrivate = isPrivate
            ))
        }
    }
}
