package com.aurora.data.repository

import com.aurora.data.db.DownloadDao
import com.aurora.data.db.DownloadEntity
import com.aurora.data.model.Download

class PersistentDownloadRepository(private val dao: DownloadDao) : DownloadRepository {

    override suspend fun add(download: Download): Long {
        return dao.insert(DownloadEntity(
            url = download.url, fileName = download.fileName,
            mimeType = download.mimeType, totalBytes = download.totalBytes,
            downloadedBytes = download.downloadedBytes, status = download.status,
            timestamp = download.timestamp
        ))
    }

    override suspend fun update(download: Download) {
        dao.update(DownloadEntity(
            id = download.id, url = download.url, fileName = download.fileName,
            mimeType = download.mimeType, totalBytes = download.totalBytes,
            downloadedBytes = download.downloadedBytes, status = download.status,
            timestamp = download.timestamp
        ))
    }

    override suspend fun get(id: Long): Download? {
        return dao.getById(id)?.toDownload()
    }

    override suspend fun getAll(): List<Download> {
        return dao.getAll().map { it.toDownload() }
    }

    override suspend fun remove(id: Long) {
        dao.deleteById(id)
    }
}
