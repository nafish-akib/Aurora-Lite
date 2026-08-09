package com.aurora.ui.mappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aurora.home.QuickActionType
import com.aurora.ui.model.QuickActionUiModel

data class QuickActionPresentation(
    val icon: ImageVector,
    val accentColor: Color,
    val title: String,
    val subtitle: String
)

object QuickActionMapper {
    private val registry = mapOf(
        QuickActionType.HISTORY to QuickActionPresentation(
            icon = Icons.Default.History,
            accentColor = Color(0xFF4DA3FF),
            title = "History Log",
            subtitle = "Explore your browsing trail"
        ),
        QuickActionType.SETTINGS to QuickActionPresentation(
            icon = Icons.Default.Settings,
            accentColor = Color(0xFF34D399),
            title = "Browser Settings",
            subtitle = "Themes, engine & layout"
        ),
        QuickActionType.AI to QuickActionPresentation(
            icon = Icons.Default.Stars,
            accentColor = Color(0xFFA78BFA),
            title = "Ask Aurora AI",
            subtitle = "Integrated helper engine"
        ),
        QuickActionType.DIAGNOSTICS to QuickActionPresentation(
            icon = Icons.Default.BugReport,
            accentColor = Color(0xFF4DA3FF),
            title = "Dev Diagnostics",
            subtitle = "CPU, memory, kernel logs"
        )
    )

    fun toPresentation(type: QuickActionType): QuickActionPresentation =
        registry[type] ?: registry[QuickActionType.SETTINGS]!!

    fun toUiList(types: List<QuickActionType>): List<QuickActionUiModel> =
        types.map { type ->
            val presentation = toPresentation(type)
            QuickActionUiModel(
                id = "action-${type.name.lowercase()}",
                title = presentation.title,
                subtitle = presentation.subtitle,
                icon = presentation.icon,
                accentColor = presentation.accentColor
            )
        }
}
