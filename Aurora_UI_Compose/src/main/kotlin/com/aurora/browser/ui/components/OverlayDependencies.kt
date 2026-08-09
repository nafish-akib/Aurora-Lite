package com.aurora.browser.ui.components

import com.aurora.ui.types.Screen
import com.aurora.ui.types.Bookmark
import com.aurora.ui.viewmodel.TabSession

data class OverlayDependencies(
    val currentScreen: Screen,
    val brightness: Int,
    val developerMode: Boolean,
    val activeAccent: String,
    val activeTheme: String,
    val bookmarks: List<Bookmark>,
    val activeTab: TabSession?,
)
