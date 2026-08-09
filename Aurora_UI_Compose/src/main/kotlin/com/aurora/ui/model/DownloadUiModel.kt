package com.aurora.ui.model

data class DownloadUiModel(
    val id: String,
    val fileName: String,
    val progress: Int,
    val mimeType: String = "",
    val totalSize: String = "",
    val status: String = "Completed",
    val url: String = ""
)
