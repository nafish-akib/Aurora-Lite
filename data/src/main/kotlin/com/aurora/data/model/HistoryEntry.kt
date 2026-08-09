package com.aurora.data.model

data class HistoryEntry(
    val id: Long = 0,
    val url: String,
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
    val favicon: String = "",
    val sessionId: String? = null,
    val isPrivate: Boolean = false
)
