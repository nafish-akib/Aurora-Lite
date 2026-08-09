package com.aurora.data.model

data class BookmarkFolder(
    val id: Long = 0,
    val name: String,
    val order: Int = 0
)

data class EnrichedBookmark(
    val id: Long,
    val url: String,
    val title: String,
    val folderId: Long,
    val folderName: String = "",
    val faviconBitmap: android.graphics.Bitmap? = null,
    val faviconUri: String? = null,
    val thumbnail: android.graphics.Bitmap? = null,
    val dominantColor: Int = 0xFF1A1A1A.toInt(),
    val domain: String = ""
) {
    val displayDomain: String get() = domain
}
