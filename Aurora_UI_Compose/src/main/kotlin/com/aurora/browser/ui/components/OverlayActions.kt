package com.aurora.browser.ui.components

import com.aurora.ui.types.Screen

interface OverlayActions {
    fun dismissQuickSettings()
    fun dismissCommandPalette()
    fun updateCommandQuery(query: String)
    fun updateFindQuery(query: String)
    fun closeFindInPage()
    fun dismissContextMenu()
    fun dismissSiteInfo()
    fun dismissPermissions()
    fun allowPermission()
    fun denyPermission()
    fun cancelVoice()
    fun createNewTab(isPrivate: Boolean)
    fun openInNewTab(url: String, isPrivate: Boolean)
    fun openReaderMode(url: String)
    fun toggleBookmark()
    fun toggleDesktop()
    fun refresh()
    fun share(url: String)
    fun copyUrl(url: String, msg: String)
    fun goSettings()
    fun goScreen(screen: Screen)
    fun clearHistory()
    fun runDiagnostics()
    fun showToast(msg: String)
    fun findNext()
    fun findPrevious()
    fun openFindInPage()
    fun openTranslation()
}
