package com.aurora.data.repository

import com.aurora.data.model.BookmarkFolder
import com.aurora.data.model.Favorite
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryFavoriteRepository : FavoriteRepository {

    private val items = mutableListOf<Favorite>()
    private val folders = mutableListOf<BookmarkFolder>()
    private var nextId = 1L
    private var nextFolderId = 1L
    private val mutex = Mutex()

    override suspend fun add(url: String, title: String, folderId: Long): Long {
        return mutex.withLock {
            if (items.none { it.url == url }) {
                val f = Favorite(id = nextId++, url = url, title = title, order = items.size, folderId = folderId)
                items.add(f)
                f.id
            } else {
                items.find { it.url == url }!!.id
            }
        }
    }

    override suspend fun remove(id: Long) {
        mutex.withLock { items.removeAll { it.id == id } }
    }

    override suspend fun getAll(): List<Favorite> {
        mutex.withLock { return items.sortedBy { it.order }.toList() }
    }

    override suspend fun getByFolder(folderId: Long): List<Favorite> {
        mutex.withLock { return items.filter { it.folderId == folderId }.sortedBy { it.order } }
    }

    override suspend fun getByUrl(url: String): Favorite? {
        mutex.withLock { return items.find { it.url == url } }
    }

    override suspend fun isFavorite(url: String): Boolean {
        mutex.withLock { return items.any { it.url == url } }
    }

    override suspend fun getFolders(): List<BookmarkFolder> {
        mutex.withLock {
            val knownIds = folders.map { it.id }.toSet() + 0L
            val synthetic = items
                .map { it.folderId }
                .distinct()
                .filter { it !in knownIds }
                .map { BookmarkFolder(id = it, name = "Folder $it") }
            return listOf(BookmarkFolder(id = 0L, name = "Unsorted")) + folders.sortedBy { it.order } + synthetic
        }
    }

    override suspend fun addFolder(name: String): Long {
        return mutex.withLock {
            val cleanName = name.trim().ifEmpty { "New Folder" }
            val existing = folders.find { it.name.equals(cleanName, ignoreCase = true) }
            if (existing != null) {
                existing.id
            } else {
                val id = nextFolderId++
                folders.add(BookmarkFolder(id = id, name = cleanName, order = folders.size + 1))
                id
            }
        }
    }

    override suspend fun removeFolder(id: Long) {
        mutex.withLock {
            if (id == 0L) return@withLock
            folders.removeAll { it.id == id }
            items.indices.forEach { idx ->
                if (items[idx].folderId == id) items[idx] = items[idx].copy(folderId = 0L)
            }
        }
    }

    override suspend fun moveToFolder(bookmarkId: Long, folderId: Long) {
        mutex.withLock {
            val idx = items.indexOfFirst { it.id == bookmarkId }
            if (idx >= 0) items[idx] = items[idx].copy(folderId = folderId)
        }
    }
}
