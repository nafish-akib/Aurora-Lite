package com.aurora.data.repository

import com.aurora.data.model.Download

interface DownloadRepository {
    suspend fun add(download: Download): Long
    suspend fun update(download: Download)
    suspend fun get(id: Long): Download?
    suspend fun getAll(): List<Download>
    suspend fun remove(id: Long)
}
