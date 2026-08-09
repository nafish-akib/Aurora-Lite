package com.aurora.data.model

data class Favorite(
    val id: Long = 0,
    val url: String,
    val title: String = "",
    val order: Int = 0,
    val folderId: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
