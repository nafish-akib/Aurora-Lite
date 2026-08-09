package com.aurora.data.repository

import com.aurora.data.db.BookmarkDao
import com.aurora.data.db.BookmarkEntity
import com.aurora.data.db.BookmarkFolderEntity
import com.aurora.data.model.BookmarkFolder
import com.aurora.data.model.Favorite

class PersistentBookmarkRepository(private val dao: BookmarkDao) : FavoriteRepository {

    override suspend fun add(url: String, title: String, folderId: Long): Long {
        return dao.insert(BookmarkEntity(url = url, title = title, order = System.currentTimeMillis().toInt(), folderId = folderId))
    }

    override suspend fun remove(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun getAll(): List<Favorite> {
        return dao.getAll().map { it.toFavorite() }
    }

    override suspend fun getByFolder(folderId: Long): List<Favorite> {
        return dao.getByFolder(folderId).map { it.toFavorite() }
    }

    override suspend fun getByUrl(url: String): Favorite? {
        return dao.getByUrl(url)?.toFavorite()
    }

    override suspend fun isFavorite(url: String): Boolean {
        return dao.exists(url)
    }

    override suspend fun getFolders(): List<BookmarkFolder> {
        val persisted = dao.getFolders().map { it.toModel() }
        val knownIds = persisted.map { it.id }.toSet() + 0L
        val synthetic = dao.getAll()
            .map { it.folderId }
            .distinct()
            .filter { it !in knownIds }
            .map { BookmarkFolder(id = it, name = "Folder $it") }
        return listOf(BookmarkFolder(id = 0L, name = "Unsorted")) + persisted + synthetic
    }

    override suspend fun addFolder(name: String): Long {
        val cleanName = name.trim().ifEmpty { "New Folder" }
        val existing = dao.getFolderByName(cleanName)
        if (existing != null) return existing.id
        val nextOrder = dao.getFolders().size + 1
        val insertedId = dao.insertFolder(BookmarkFolderEntity(name = cleanName, order = nextOrder))
        return if (insertedId == -1L) dao.getFolderByName(cleanName)?.id ?: 0L else insertedId
    }

    override suspend fun removeFolder(id: Long) {
        if (id == 0L) return
        dao.moveFolderBookmarksToRoot(id)
        dao.deleteFolder(id)
    }

    override suspend fun moveToFolder(bookmarkId: Long, folderId: Long) {
        dao.moveToFolder(bookmarkId, folderId)
    }
}
