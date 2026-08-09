package com.aurora.browser.ui.components

data class DownloadItem(
    val id: String,
    val fileName: String,
    val progress: Int,
    val mimeType: String = "",
    val totalSize: String = "",
    val status: String = "Completed",
    val url: String = "",
    val fileData: Any? = null
)
