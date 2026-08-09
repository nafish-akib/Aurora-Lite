package com.aurora.ui.model

data class SystemProcess(
    val pid: Int,
    val name: String,
    val type: String,
    val cpu: Int,
    val memory: Int,
    val status: String = "Healthy",
    val tabId: String? = null
)
