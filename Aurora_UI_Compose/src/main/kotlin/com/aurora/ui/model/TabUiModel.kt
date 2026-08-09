package com.aurora.ui.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class TabUiModel(
    val id: String,
    val url: String,
    val title: String,
    val domain: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 100,
    val isPrivate: Boolean = false,
    val isPinned: Boolean = false,
    val faviconBitmap: Bitmap? = null,
    val thumbnail: Bitmap? = null,
    val accentColor: Color = Color(0xFF4DA3FF)
)
