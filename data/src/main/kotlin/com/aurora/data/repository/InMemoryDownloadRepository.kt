package com.aurora.data.repository

import com.aurora.data.model.Download
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryDownloadRepository : DownloadRepository {

    private val items = mutableListOf<Download>()
    private val mutex = Mutex()
    private var nextId = 1L

    override suspend fun add(download: Download): Long {
        return mutex.withLock {
            val d = download.copy(id = nextId++)
            items.add(d)
            d.id
        }
    }

    override suspend fun update(download: Download) {
        mutex.withLock {
            val idx = items.indexOfFirst { it.id == download.id }
            if (idx >= 0) items[idx] = download
        }
    }

    override suspend fun get(id: Long): Download? {
        return mutex.withLock { items.find { it.id == id } }
    }

    override suspend fun getAll(): List<Download> {
        mutex.withLock { return items.toList() }
    }

    override suspend fun remove(id: Long) {
        mutex.withLock { items.removeAll { it.id == id } }
    }
}
