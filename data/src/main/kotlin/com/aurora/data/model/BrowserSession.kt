package com.aurora.data.model

data class BrowserSession(
    val id: Long = 0,
    val url: String,
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
