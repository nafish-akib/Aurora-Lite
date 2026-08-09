package com.aurora.ui.types

data class Tab(
    val id: String,
    val url: String,
    val title: String,
    val isLoading: Boolean = false,
    val progress: Int = 100,
    val isPrivate: Boolean = false,
    val isMuted: Boolean = false,
    val isPiP: Boolean = false,
    val thumbnail: String? = null,
    val lastActive: Long = 0L,
    val scrollPosition: Int = 0,
    val isSleeping: Boolean = false
)

data class Bookmark(
    val id: String,
    val url: String,
    val title: String,
    val category: String,
    val folderId: Long = 0L,
    val isFavorite: Boolean = true,
    val icon: String? = null
)

data class Download(
    val id: String,
    val fileName: String,
    val url: String,
    val totalSize: String,
    val mimeType: String,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.Downloading,
    val timestamp: Long = 0L,
    val speed: String? = null,
    val fileData: Any? = null
)

enum class DownloadStatus { Downloading, Completed, Paused, Failed }

data class Profile(
    val id: String,
    val name: String,
    val avatar: String,
    val isGuest: Boolean = false,
    val isSynced: Boolean = true
)

data class MockFile(
    val id: String,
    val fileName: String,
    val totalSize: String,
    val mimeType: String,
    val url: String,
    val fileData: Any? = null
)

data class MockVideo(
    val id: String,
    val title: String,
    val channel: String,
    val views: String,
    val time: String,
    val thumbnail: String,
    val videoUrl: String,
    val duration: String,
    val description: String
)

data class MockArticle(
    val title: String,
    val author: String,
    val published: String,
    val readingTime: String,
    val content: String
)

enum class Screen { Home, Browser, Library, Bookmarks, Settings, PerformanceCenter, History, Downloads, TabManagement, PasswordManager }

enum class FocusZone { Search, Continue, Favorites, Trending, Downloads, QuickActions }

enum class InteractionMode { Toolbar, Pointer, TextInput }
