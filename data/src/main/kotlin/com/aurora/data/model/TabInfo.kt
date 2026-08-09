package com.aurora.data.model

data class TabInfo(
    val id: String,
    val url: String,
    val title: String,
    val isPrivate: Boolean,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisited: Long = System.currentTimeMillis(),
    val scrollPosition: Int = 0,
    val zoomLevel: Float = 1.0f,
    val readerModeEnabled: Boolean = false,
    val tabOrder: Int = 0
)
