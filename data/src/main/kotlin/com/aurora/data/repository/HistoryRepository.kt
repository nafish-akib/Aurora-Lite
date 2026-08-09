package com.aurora.data.repository

import com.aurora.data.model.HistoryEntry

interface HistoryRepository {
    suspend fun addEntry(url: String, title: String, favicon: String = "", sessionId: String? = null, isPrivate: Boolean = false)
    suspend fun getRecent(limit: Int = 50): List<HistoryEntry>
    suspend fun getAll(): List<HistoryEntry>
    suspend fun getEntryByUrl(url: String): HistoryEntry?
    suspend fun search(query: String): List<HistoryEntry>
    suspend fun deleteEntry(id: Long)
    suspend fun clearToday()
    suspend fun clearLastHour()
    suspend fun clear()
}
