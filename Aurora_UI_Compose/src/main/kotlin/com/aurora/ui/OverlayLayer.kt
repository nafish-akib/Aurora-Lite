package com.aurora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.components.ContextMenu
import com.aurora.browser.ui.components.ContextMenuItem
import com.aurora.browser.ui.components.FindInPagePanel
import com.aurora.browser.ui.components.OverlayActions
import com.aurora.browser.ui.components.OverlayDependencies
import com.aurora.browser.ui.components.OverlayState
import com.aurora.browser.ui.components.ReaderModePanel
import com.aurora.browser.ui.components.SiteInfoPanel
import com.aurora.browser.ui.components.SitePermissionsPanel
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.theme.rememberVoicePulse
import com.aurora.ui.types.Screen

@Composable
fun OverlayLayer(
    state: OverlayState,
    commandQuery: String,
    deps: OverlayDependencies,
    actions: OverlayActions,
    toastMessage: String?,
    isVoiceListening: Boolean,
    voiceWave: Boolean,
    voiceOutputMessage: String,
) {
    if (toastMessage != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.padding(bottom = 24.dp).background(AuroraColors.neutral900.copy(alpha = 0.9f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white10, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Check, null, Modifier.size(16.dp), AuroraColors.auroraEmerald); Text(toastMessage, color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (isVoiceListening) {
        val vpa = rememberVoicePulse()
        Box(Modifier.fillMaxSize().background(AuroraColors.neutral950.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(80.dp).graphicsLayer(alpha = vpa).border(2.dp, AuroraColors.auroraPurple.copy(alpha = vpa), CircleShape))
                    Icon(Icons.Default.Mic, null, Modifier.size(64.dp), AuroraColors.auroraPurple)
                    if (voiceWave) Box(Modifier.size(80.dp).border(2.dp, AuroraColors.auroraPurple.copy(alpha = 0.7f), CircleShape).graphicsLayer { alpha = 1f - vpa })
                }
                Text(voiceOutputMessage, color = AuroraColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Speak into your remote control.", color = AuroraColors.white40, fontSize = 11.sp, textAlign = TextAlign.Center)
                Box(Modifier.background(AuroraColors.neutral800, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp).clickable { actions.cancelVoice() }) { Text("Cancel Voice", color = AuroraColors.white70, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (state is OverlayState.QuickSettings) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Box(Modifier.fillMaxHeight().width(320.dp).background(AuroraColors.neutral900, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)).border(1.dp, AuroraColors.white10)) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("QUICK SETTINGS", color = AuroraColors.white, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Icon(Icons.Default.Close, null, Modifier.size(16.dp).clickable { actions.dismissQuickSettings() }, AuroraColors.white40) }
                Spacer(Modifier.height(16.dp)); Text("Brightness HUD", color = AuroraColors.white60, fontSize = 10.sp); Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("${deps.brightness}%", color = AuroraColors.auroraBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).background(AuroraColors.neutral800, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).drawWithContent { drawContent(); drawRect(AuroraColors.auroraBlue, size = Size(size.width * (deps.brightness - 30) / 70f, size.height)) })
                Spacer(Modifier.height(16.dp)); Text("Simulated Network", color = AuroraColors.white60, fontSize = 10.sp); Text("? High Speed", color = AuroraColors.auroraEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(vertical = 10.dp).clickable { actions.dismissQuickSettings(); actions.goScreen(Screen.Settings) }, contentAlignment = Alignment.Center) { Text("Full Control Center", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                Box(Modifier.fillMaxWidth().padding(top = 8.dp).background(AuroraColors.neutral800, RoundedCornerShape(12.dp)).padding(vertical = 10.dp).clickable { actions.dismissQuickSettings() }, contentAlignment = Alignment.Center) { Text("Close Settings", color = AuroraColors.white80, fontSize = 11.sp) }
            }
        }
    }

    if (state is OverlayState.CommandPalette) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
            Column(Modifier.width(400.dp).background(AuroraColors.neutral900, RoundedCornerShape(24.dp)).border(1.dp, AuroraColors.white10, RoundedCornerShape(24.dp)).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Aurora Command Palette", color = AuroraColors.white50, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Icon(Icons.Default.Close, null, Modifier.size(18.dp).clickable { actions.dismissCommandPalette() }, AuroraColors.white40) }
                Box(Modifier.fillMaxWidth().background(AuroraColors.neutral950, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Search, null, Modifier.size(16.dp), AuroraColors.white30); Text(if (commandQuery.isEmpty()) "Type command name..." else commandQuery, color = if (commandQuery.isEmpty()) AuroraColors.white30 else AuroraColors.white, fontSize = 11.sp) } }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    listOf("Find in Page" to { actions.openFindInPage(); actions.showToast("Find in Page") },
                        "Toggle Desktop Mode" to { actions.toggleDesktop(); actions.showToast("Desktop Mode Toggled") },
                        "Open Bookmarks" to { actions.goScreen(Screen.Bookmarks); actions.showToast("Opened Bookmarks") },
                        "Open Downloads" to { actions.goScreen(Screen.Downloads); actions.showToast("Opened Downloads") },
                        "Clear Browsing History" to { actions.clearHistory(); actions.showToast("History Cleared") },
                        "Toggle Dark Mode / Theme" to { actions.showToast("Theme Swapped") },
                        "Open System Settings" to { actions.goScreen(Screen.Settings) },
                        "Trigger Diagnostics Self-Test" to { actions.runDiagnostics() },
                        "Create New Private Tab" to { actions.createNewTab(true) })
                        .filter { it.first.lowercase().contains(commandQuery.lowercase()) }.forEach { (label, action) ->
                            Box(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp).clickable { action(); actions.dismissCommandPalette() }) { Text(label, color = AuroraColors.white80, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                }
            }
        }
    }

    if (state is OverlayState.FindInPage) {
        FindInPagePanel(
            visible = true, query = state.query, onQueryChange = actions::updateFindQuery,
            currentMatch = state.currentMatch, totalMatches = state.totalMatches,
            onFindNext = { actions.findNext() }, onFindPrevious = { actions.findPrevious() },
            onClose = actions::closeFindInPage
        )
    }

    if (state is OverlayState.ContextMenu) {
        val pageUrl = deps.activeTab?.controller?.state?.value?.currentUrl ?: ""
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ContextMenu(visible = true, onDismiss = actions::dismissContextMenu, items = listOf(
                ContextMenuItem("New Tab", Icons.AutoMirrored.Filled.OpenInNew) { actions.createNewTab(false) },
                ContextMenuItem("Bookmark", Icons.Default.Bookmark) { actions.toggleBookmark() },
                ContextMenuItem("Copy URL", Icons.Default.ContentCopy) { actions.copyUrl(pageUrl, "URL copied") },
                ContextMenuItem("Refresh", Icons.Default.Refresh) { actions.refresh() },
                ContextMenuItem("Desktop Mode", Icons.Default.DesktopWindows) { actions.toggleDesktop(); actions.showToast("Desktop Toggled") },
                ContextMenuItem("Share", Icons.Default.Share) { actions.share(pageUrl) },
                ContextMenuItem("Reader Mode", Icons.Default.Bookmark) { actions.openReaderMode(pageUrl) },
                ContextMenuItem("Page Info", Icons.Default.Info) { actions.dismissContextMenu(); actions.goScreen(Screen.Home) }
            ))
        }
    }

    if (state is OverlayState.LinkMenu) {
        val url = state.url
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ContextMenu(visible = true, onDismiss = actions::dismissContextMenu, items = listOf(
                ContextMenuItem("Open in New Tab", Icons.AutoMirrored.Filled.OpenInNew) { actions.openInNewTab(url, false) },
                ContextMenuItem("Open in Private Tab", Icons.Default.Lock) { actions.openInNewTab(url, true) },
                ContextMenuItem("Copy Link", Icons.Default.ContentCopy) { actions.copyUrl(url, "Link copied") },
                ContextMenuItem("Share Link", Icons.Default.Share) { actions.share(url) }
            ))
        }
    }

    if (state is OverlayState.ReaderMode) {
        ReaderModePanel(
            title = state.title,
            text = state.text,
            onDismiss = actions::dismissContextMenu
        )
    }

    if (state is OverlayState.Permissions) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SitePermissionsPanel(
                request = state.request, visible = true,
                onDismiss = actions::dismissPermissions,
                onAllow = actions::allowPermission,
                onDeny = actions::denyPermission
            )
        }
    }

    if (state is OverlayState.SiteInfo) {
        val bs = deps.activeTab?.controller?.state?.value
        val domain = bs?.currentUrl?.removePrefix("https://")?.removePrefix("http://")?.removePrefix("www.")?.split("/")?.firstOrNull() ?: ""
        val perms = deps.activeTab?.session?.getPermissionsService()?.getSitePermissions(domain) ?: emptyMap()
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SiteInfoPanel(
                visible = true, url = bs?.currentUrl ?: "", domain = domain,
                isSecure = bs?.isSecure ?: false,
                isBookmarked = deps.bookmarks.any { it.url == (bs?.currentUrl ?: "") },
                isDesktopMode = deps.activeTab?.controller?.isDesktopMode() ?: false,
                permissions = perms, onDismiss = actions::dismissSiteInfo,
                onToggleBookmark = actions::toggleBookmark,
                onToggleDesktopMode = actions::toggleDesktop,
                onClearSiteData = { actions.showToast("Site data cleared") }
            )
        }
    }
    }
}
