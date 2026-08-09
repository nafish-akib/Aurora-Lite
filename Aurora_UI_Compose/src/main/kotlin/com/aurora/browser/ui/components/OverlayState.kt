package com.aurora.browser.ui.components

import com.aurora.engine.PermissionRequest

sealed class OverlayState {
    data object None : OverlayState()
    data object ContextMenu : OverlayState()
    data class FindInPage(val query: String = "", val currentMatch: Int = 0, val totalMatches: Int = 0) : OverlayState()
    data class Permissions(val request: PermissionRequest) : OverlayState()
    data object SiteInfo : OverlayState()
    data object QuickSettings : OverlayState()
    data object CommandPalette : OverlayState()
    data object Diagnostics : OverlayState()
    data object ExitDialog : OverlayState()
    data object Downloads : OverlayState()
    data class LinkMenu(val url: String) : OverlayState()
    data class ReaderMode(val url: String, val title: String, val text: String) : OverlayState()
}
