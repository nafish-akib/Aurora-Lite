package com.aurora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aurora.data.model.HistoryEntry

@Entity(
    tableName = "history",
    indices = [
        Index("url"),
        Index("lastVisited"),
        Index("visitTime")
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String = "",
    val visitTime: Long = System.currentTimeMillis(),
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
    val faviconUrl: String = "",
    val sessionId: String? = null,
    val isPrivate: Boolean = false
) {
    fun toHistoryEntry() = HistoryEntry(
        id = id,
        url = url,
        title = title,
        timestamp = visitTime,
        lastVisited = lastVisited,
        visitCount = visitCount,
        favicon = faviconUrl,
        sessionId = sessionId,
        isPrivate = isPrivate
    )
}
