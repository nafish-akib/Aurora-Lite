package com.aurora.data.repository

import com.aurora.data.db.HistoryDao
import com.aurora.data.db.HistoryEntity
import com.aurora.data.model.HistoryEntry

class PersistentHistoryRepository(private val dao: HistoryDao) : HistoryRepository {

    override suspend fun addEntry(url: String, title: String, favicon: String, sessionId: String?, isPrivate: Boolean) {
        dao.upsert(url, title, favicon, sessionId, isPrivate)
    }

    override suspend fun getRecent(limit: Int): List<HistoryEntry> {
        return dao.getRecent(limit).map { it.toHistoryEntry() }
    }

    override suspend fun getAll(): List<HistoryEntry> {
        return dao.getAll().map { it.toHistoryEntry() }
    }

    override suspend fun getEntryByUrl(url: String): HistoryEntry? {
        return dao.getByUrl(url)?.toHistoryEntry()
    }

    override suspend fun search(query: String): List<HistoryEntry> {
        return dao.search(query).map { it.toHistoryEntry() }
    }

    override suspend fun deleteEntry(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clearToday() {
        val now = System.currentTimeMillis()
        val todayStart = now - (now % 86400000L)
        dao.deleteVisitedSince(todayStart)
    }

    override suspend fun clearLastHour() {
        val hourAgo = System.currentTimeMillis() - 3600000L
        dao.deleteVisitedSince(hourAgo)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
