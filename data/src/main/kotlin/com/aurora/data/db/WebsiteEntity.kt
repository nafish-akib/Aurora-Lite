package com.aurora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aurora.data.model.WebsiteMetadata

@Entity(
    tableName = "websites",
    indices = [Index("url", unique = true)]
)
data class WebsiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String = "",
    val faviconUri: String? = null,
    val thumbnailUri: String? = null,
    val dominantColor: Int = 0xFF1A1A1A.toInt(),
    val themeColor: Int? = null,
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 0,
    val scrollPosition: Int = 0,
    val pageLanguage: String? = null,
    val isSecure: Boolean = false,
    val certificateIssuer: String = "",
    val isVerified: Boolean = false,
    val isBookmarked: Boolean = false,
    val isPinned: Boolean = false,
    val openTabCount: Int = 0
) {
    fun toMetadata() = WebsiteMetadata(
        url = url,
        title = title,
        faviconUri = faviconUri,
        thumbnailUri = thumbnailUri,
        dominantColor = dominantColor,
        themeColor = themeColor,
        lastVisited = lastVisited,
        visitCount = visitCount,
        scrollPosition = scrollPosition,
        pageLanguage = pageLanguage,
        securityInfo = com.aurora.data.model.SecurityInfo(
            isSecure = isSecure,
            certificateIssuer = certificateIssuer,
            isVerified = isVerified
        ),
        isBookmarked = isBookmarked,
        isPinned = isPinned,
        openTabCount = openTabCount
    )
}
