package com.aurora.data.model

data class Download(
    val id: Long = 0,
    val url: String,
    val fileName: String = "",
    val mimeType: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = STATUS_PENDING,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_PAUSED = "PAUSED"
    }
}
