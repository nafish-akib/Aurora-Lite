package com.aurora.data.repository

import com.aurora.data.model.BookmarkFolder
import com.aurora.data.model.Favorite

interface FavoriteRepository {
    suspend fun add(url: String, title: String, folderId: Long = 0): Long
    suspend fun remove(id: Long)
    suspend fun getAll(): List<Favorite>
    suspend fun getByFolder(folderId: Long): List<Favorite>
    suspend fun getByUrl(url: String): Favorite?
    suspend fun isFavorite(url: String): Boolean
    suspend fun getFolders(): List<BookmarkFolder>
    suspend fun addFolder(name: String): Long
    suspend fun removeFolder(id: Long)
    suspend fun moveToFolder(bookmarkId: Long, folderId: Long)
}
