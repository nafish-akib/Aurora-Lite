package com.aurora.ui.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PresentationInfo(
    val icon: ImageVector,
    val accentColor: Color
)

object BookmarkPresentationRegistry {
    private val registry = mapOf(
        "youtube.com" to PresentationInfo(Icons.Default.PlayArrow, Color(0xFFFF0000)),
        "github.com" to PresentationInfo(Icons.Default.Code, Color(0xFF181717)),
        "wikipedia.org" to PresentationInfo(Icons.Default.Book, Color(0xFFFFFFFF)),
        "news.google.com" to PresentationInfo(Icons.Default.List, Color(0xFF4285F4)),
        "reddit.com" to PresentationInfo(Icons.Default.List, Color(0xFFFF4500)),
        "drive.google.com" to PresentationInfo(Icons.Default.Folder, Color(0xFF34A853)),
    )

    fun forUrl(url: String): PresentationInfo {
        val domain = extractDomain(url)
        return registry[domain] ?: PresentationInfo(Icons.Default.Language, Color(0xFF4DA3FF))
    }

    private fun extractDomain(url: String): String {
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?.lowercase()
            ?: url.lowercase()
    }
}
