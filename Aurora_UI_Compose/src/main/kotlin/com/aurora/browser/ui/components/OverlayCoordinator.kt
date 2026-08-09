package com.aurora.browser.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class OverlayCoordinator {
    var state by mutableStateOf<OverlayState>(OverlayState.None)
        private set

    var commandQuery by mutableStateOf("")
        private set

    var findInPageQuery by mutableStateOf("")
        private set

    val isShowing: Boolean get() = state != OverlayState.None

    fun openContextMenu() { state = OverlayState.ContextMenu }
    fun openLinkMenu(url: String) { state = OverlayState.LinkMenu(url) }
    fun openReaderMode(url: String, title: String, text: String) { state = OverlayState.ReaderMode(url, title, text) }
    fun openQuickSettings() { state = OverlayState.QuickSettings }
    fun openCommandPalette() { state = OverlayState.CommandPalette }
    fun openSiteInfo() { state = OverlayState.SiteInfo }
    fun openFindInPage() { state = OverlayState.FindInPage() }
    fun openPermissions(request: com.aurora.engine.PermissionRequest) { state = OverlayState.Permissions(request) }
    fun toggleDesktopMode() {
        commandQuery = ""; state = OverlayState.None
    }

    fun updateCommandQuery(query: String) { commandQuery = query }
    fun updateFindQuery(query: String) { findInPageQuery = query }

    fun close() {
        state = OverlayState.None
        commandQuery = ""
        findInPageQuery = ""
    }

    fun closeKeepQuery() {
        state = OverlayState.None
    }

    fun closeCommandPalette() {
        state = OverlayState.None
        commandQuery = ""
    }

    fun closeFindInPage() {
        state = OverlayState.None
        findInPageQuery = ""
    }

    fun onMenuPressed(
        isBrowserScreen: Boolean,
        isDiagnosticsOpen: Boolean,
        isVoiceListening: Boolean
    ) {
        if (isShowing || isDiagnosticsOpen || isVoiceListening) {
            state = OverlayState.None
        } else if (isBrowserScreen) {
            state = OverlayState.ContextMenu
        } else {
            state = OverlayState.QuickSettings
        }
    }

    fun onBackPressed(): Boolean {
        return if (isShowing) {
            state = OverlayState.None
            commandQuery = ""
            findInPageQuery = ""
            true
        } else {
            false
        }
    }
}
