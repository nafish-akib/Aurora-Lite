package com.aurora.ui.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class ContinueBrowsingUiModel(
    val id: String,
    val name: String,
    val title: String,
    val timeText: String,
    val url: String,
    val domain: String,
    val accentColor: Color,
    val faviconUri: String? = null,
    val faviconBitmap: Bitmap? = null,
    val thumbnail: Bitmap? = null
)
