package com.aurora.ui.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class FavoriteUiModel(
    val id: Long,
    val url: String,
    val title: String,
    val domain: String = "",
    val faviconBitmap: Bitmap? = null,
    val faviconUri: String? = null,
    val accentColor: Color = Color(0xFF4DA3FF)
)
