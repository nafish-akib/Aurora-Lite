package com.aurora.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aurora.data.model.Download

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String = "",
    val mimeType: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = Download.STATUS_PENDING,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDownload() = Download(
        id = id, url = url, fileName = fileName, mimeType = mimeType,
        totalBytes = totalBytes, downloadedBytes = downloadedBytes,
        status = status, timestamp = timestamp
    )
}
