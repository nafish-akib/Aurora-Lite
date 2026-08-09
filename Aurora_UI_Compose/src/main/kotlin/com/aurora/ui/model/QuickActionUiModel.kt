package com.aurora.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class QuickActionUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    companion object {
        val Default = QuickActionUiModel(
            id = "",
            title = "",
            subtitle = "",
            icon = Icons.Default.BugReport,
            accentColor = Color(0xFF4DA3FF)
        )
    }
}
