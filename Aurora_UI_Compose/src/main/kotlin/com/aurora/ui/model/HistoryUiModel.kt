package com.aurora.ui.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class HistoryUiModel(
    val id: String,
    val title: String,
    val url: String,
    val domain: String,
    val timeText: String,
    val actionVerb: String = "Visited",
    val visitCount: Int,
    val accentColor: Color = Color(0xFF4DA3FF),
    val faviconUri: String? = null,
    val faviconBitmap: Bitmap? = null,
    val thumbnail: Bitmap? = null
)

data class HistoryGroup(
    val label: String,
    val items: List<HistoryUiModel>
)
