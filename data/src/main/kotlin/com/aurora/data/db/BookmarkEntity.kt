package com.aurora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aurora.data.model.Favorite

@Entity(tableName = "bookmarks", indices = [Index("url", unique = true)])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String = "",
    val order: Int = 0,
    val folderId: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toFavorite() = Favorite(id = id, url = url, title = title, order = order, folderId = folderId, createdAt = addedAt)
}
