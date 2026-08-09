package com.aurora.data.repository

import com.aurora.data.model.HistoryEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryHistoryRepository : HistoryRepository {

    private val entries = mutableListOf<HistoryEntry>()
    private var nextId = 1L
    private val mutex = Mutex()

    override suspend fun addEntry(url: String, title: String, favicon: String, sessionId: String?, isPrivate: Boolean) {
        mutex.withLock {
            val existing = entries.find { it.url == url }
            if (existing != null) {
                val idx = entries.indexOf(existing)
                entries[idx] = existing.copy(
                    title = title.ifBlank { existing.title },
                    timestamp = System.currentTimeMillis(),
                    lastVisited = System.currentTimeMillis(),
                    visitCount = existing.visitCount + 1,
                    favicon = favicon.ifBlank { existing.favicon },
                    sessionId = sessionId ?: existing.sessionId,
                    isPrivate = isPrivate
                )
            } else {
                entries.add(HistoryEntry(
                    id = nextId++, url = url, title = title,
                    favicon = favicon, sessionId = sessionId, isPrivate = isPrivate
                ))
            }
        }
    }

    override suspend fun getRecent(limit: Int): List<HistoryEntry> {
        mutex.withLock {
            return entries.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override suspend fun getAll(): List<HistoryEntry> {
        mutex.withLock { return entries.toList() }
    }

    override suspend fun getEntryByUrl(url: String): HistoryEntry? {
        mutex.withLock { return entries.find { it.url == url } }
    }

    override suspend fun search(query: String): List<HistoryEntry> {
        mutex.withLock {
            return entries.filter {
                it.url.contains(query, ignoreCase = true) ||
                    it.title.contains(query, ignoreCase = true)
            }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun deleteEntry(id: Long) {
        mutex.withLock { entries.removeAll { it.id == id } }
    }

    override suspend fun clearToday() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val todayStart = now - (now % 86400000L)
            entries.removeAll { it.timestamp >= todayStart }
        }
    }

    override suspend fun clearLastHour() {
        mutex.withLock {
            val hourAgo = System.currentTimeMillis() - 3600000L
            entries.removeAll { it.timestamp >= hourAgo }
        }
    }

    override suspend fun clear() {
        mutex.withLock { entries.clear() }
    }
}
