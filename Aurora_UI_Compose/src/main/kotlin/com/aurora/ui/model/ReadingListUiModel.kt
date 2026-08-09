package com.aurora.ui.model

import androidx.compose.ui.graphics.Color

data class ReadingListUiModel(
    val id: String,
    val title: String,
    val tag: String,
    val tagColor: Color,
    val subtitle: String,
    val url: String
)
