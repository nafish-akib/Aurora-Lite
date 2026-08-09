package com.aurora.ui.mappers

import com.aurora.data.model.Download
import com.aurora.ui.model.DownloadUiModel

object DownloadMapper {
    fun toUi(download: Download): DownloadUiModel {
        val progress = if (download.totalBytes > 0) {
            ((download.downloadedBytes.toFloat() / download.totalBytes) * 100).toInt()
        } else if (download.status == Download.STATUS_COMPLETED) 100 else 0
        return DownloadUiModel(
            id = download.id.toString(),
            fileName = download.fileName,
            progress = progress,
            mimeType = download.mimeType,
            totalSize = download.totalBytes.toString(),
            status = download.status,
            url = download.url
        )
    }

    fun toUiList(downloads: List<Download>): List<DownloadUiModel> {
        return downloads.map { toUi(it) }
    }
}
