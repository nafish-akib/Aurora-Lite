package com.aurora.ui

import android.content.Context
import android.util.Log
import android.os.Build
import android.content.pm.PackageManager
import android.Manifest
import java.io.File
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.state.BrowserState
import com.aurora.browser.ui.components.AudioPlayer
import com.aurora.browser.ui.components.BrowserCoordinator
import com.aurora.browser.ui.components.DiagnosticsDashboard
import com.aurora.browser.ui.components.Download as ViewerDownload
import com.aurora.browser.ui.components.HomeCoordinator
import com.aurora.browser.ui.components.ImageGallery
import com.aurora.browser.ui.components.LogEvent as AuroraLogEvent
import com.aurora.browser.ui.components.OverlayActions
import com.aurora.browser.ui.components.OverlayCoordinator
import com.aurora.browser.ui.components.OverlayDependencies
import com.aurora.browser.ui.components.PasswordSavePrompt
import com.aurora.browser.ui.components.PasswordManagerScreen
import com.aurora.browser.ui.components.PasswordPromptState
import com.aurora.browser.ui.components.OverlayState
import com.aurora.browser.ui.components.PDFViewer
import com.aurora.browser.ui.components.Process as AuroraProcess
import com.aurora.browser.ui.components.RemoteControl
import com.aurora.browser.ui.components.SettingsCoordinator
import com.aurora.browser.ui.components.TextViewer
import com.aurora.browser.ui.components.VideoPlayer
import com.aurora.data.model.BookmarkFolder
import com.aurora.data.model.HistoryEntry
import com.aurora.data.search.SearchEngineRegistry
import com.aurora.engine.InputBridge
import com.aurora.ui.engine.EngineView
import com.aurora.ui.focus.FocusBinding
import com.aurora.ui.focus.FocusDirection
import com.aurora.ui.focus.FocusEngine
import com.aurora.ui.data.MockData
import com.aurora.ui.mappers.BookmarkMapper
import com.aurora.ui.mappers.BrowserMapper
import com.aurora.ui.model.ContinueBrowsingUiModel
import com.aurora.ui.model.DownloadUiModel
import com.aurora.ui.model.FavoriteUiModel
import com.aurora.ui.model.HistoryUiModel
import com.aurora.ui.model.HomeUiState
import com.aurora.ui.model.QuickActionUiModel
import com.aurora.ui.screens.BrowserScreen
import com.aurora.ui.screens.BookmarkManagerScreen
import com.aurora.ui.screens.HomeScreen
import com.aurora.ui.screens.TabManagementScreen
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.theme.StaggerDelay
import com.aurora.ui.theme.cardLift
import com.aurora.ui.theme.focusScale
import com.aurora.ui.theme.lightSweep
import com.aurora.ui.theme.rememberAmbientPulseOffset
import com.aurora.ui.theme.rememberBrandDotPulse
import com.aurora.ui.theme.rememberCursorBlink
import com.aurora.ui.theme.rememberFocusGlowAlpha
import com.aurora.ui.theme.rememberSettingsAlpha
import com.aurora.ui.theme.rememberSparkleRotation
import com.aurora.ui.theme.rememberStaggerAlpha
import com.aurora.ui.types.Bookmark
import com.aurora.ui.components.NetworkInfoPanel
import com.aurora.ui.types.Download
import com.aurora.ui.types.DownloadStatus
import com.aurora.ui.types.Profile
import com.aurora.ui.types.Screen
import com.aurora.ui.viewmodel.SessionManager
import com.aurora.ui.viewmodel.TabSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SiteCardData(
    val name: String, val url: String, val title: String, val timeText: String,
    val bgGrad: String, val color: Long, val icon: String, val shortName: String
)

data class EnginePrefs(
    val allowJavaScript: Boolean = true,
    val allowCookies: Boolean = true,
    val allowPopups: Boolean = false,
    val allowLocation: Boolean = true,
    val allowNotifications: Boolean = true,
    val allowThirdPartyCookies: Boolean = true,
    val adBlockingEnabled: Boolean = false,
    val textZoom: Int = 100
)

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> "${diff / 86_400_000} days ago"
        else -> "Last visited"
    }
}

@Composable
fun getIconComponent(iconName: String, tint: Color = Color.White, iconSize: androidx.compose.ui.unit.Dp = 20.dp) {
    val icon = when (iconName) {
        "Youtube" -> Icons.Default.Tv; "Github" -> Icons.Default.Terminal
        "BookOpen", "Newspaper" -> Icons.Default.Book; "MessageSquare" -> Icons.Default.Link
        "HardDrive" -> Icons.Default.DesktopWindows
        "Movie" -> Icons.Filled.Movie; "PlayArrow" -> Icons.Filled.PlayArrow
        "Globe" -> Icons.Filled.Public; "Star" -> Icons.Filled.Star
        "MusicNote" -> Icons.Filled.MusicNote
        else -> Icons.Default.Book
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
}

@Composable
fun getGreeting(): String {
    val hr = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return if (hr < 12) "Good Morning" else if (hr < 18) "Good Afternoon" else "Good Evening"
}

fun toUiDownload(d: com.aurora.data.model.Download): Download {
    val prog = if (d.totalBytes > 0) ((d.downloadedBytes.toFloat() / d.totalBytes) * 100).toInt() else if (d.status == "COMPLETED") 100 else 0
    val tSize = when {
        d.totalBytes >= 1_000_000_000 -> "%.1f GB".format(d.totalBytes / 1_000_000_000.0)
        d.totalBytes >= 1_000_000 -> "%.1f MB".format(d.totalBytes / 1_000_000.0)
        d.totalBytes >= 1_000 -> "%.0f KB".format(d.totalBytes / 1_000.0)
        else -> "${d.totalBytes} B"
    }
    return Download(
        id = d.id.toString(),
        fileName = d.fileName,
        url = d.url,
        totalSize = tSize,
        mimeType = d.mimeType,
        progress = prog,
        status = when (d.status) {
            "COMPLETED" -> DownloadStatus.Completed
            "FAILED" -> DownloadStatus.Failed
            "PAUSED" -> DownloadStatus.Paused
            else -> DownloadStatus.Downloading
        },
        timestamp = d.timestamp
    )
}

@Composable
fun NonBrowserDashboard(
    currentScreen: Screen,
    isOffline: Boolean,
    isRendererCrashed: Boolean,
    crashedTabId: String?,
    isDiagnosticsOpen: Boolean,
    developerMode: Boolean,
    benchmarkMode: Boolean,
    isVoiceListening: Boolean,
    voiceWave: Boolean,
    voiceOutputMessage: String,
    activeMediaViewer: ViewerDownload?,
    pipVideo: ViewerDownload?,
    backgroundAudioActive: Boolean,
    bookmarks: List<Bookmark>,
    bookmarkFolders: List<BookmarkFolder>,
    downloads: List<Download>,
    processes: List<AuroraProcess>,
    timeline: List<AuroraLogEvent>,
    librarySearchQuery: String,
    libraryActiveFilter: String,
    bookmarkSearchQuery: String,
    selectedBookmarkFolderId: Long?,
    currentProfile: Profile,
    activeTab: TabSession?,
    toastMessage: String?,
    settings: SettingsCoordinator,
    home: HomeCoordinator,
    browser: BrowserCoordinator,
    overlayCoordinator: OverlayCoordinator,
    historyVm: com.aurora.ui.viewmodel.HistoryViewModel,
    downloadManager: com.aurora.browser.service.DownloadManager,
    context: Context,
    triggerToast: (String) -> Unit,
    addLog: (String, String, String) -> Unit,
    handleWebNavigation: (String) -> Unit,
    handleHomePress: () -> Unit,
    handleDpadPress: (String) -> Boolean,
    handleSelectPress: () -> Boolean,
    handleBackPress: () -> Unit,
    focusEngine: FocusEngine,
    handleCreateNewTab: (Boolean) -> Unit,
    handleOpenInNewTab: (String, Boolean) -> Unit,
    handleOpenReader: (String) -> Unit,
    handleBenchmarkToggle: (Boolean) -> Unit,
    handleToggleBookmark: () -> Unit,
    handleKillProcess: (Int) -> Unit,
    handleRestoreCrashedTab: () -> Unit,
    handleRunDiagnostics: () -> Unit,
    handleVoiceSearch: () -> Unit,
    handleSearchPress: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onSetCurrentScreen: (Screen) -> Unit,
    onSetOffline: (Boolean) -> Unit,
    onSetIsDiagnosticsOpen: (Boolean) -> Unit,
    onSetActiveMediaViewer: (ViewerDownload?) -> Unit,
    onSetPipVideo: (ViewerDownload?) -> Unit,
    onToggleBackgroundAudio: () -> Unit,
    onSetIsVoiceListening: (Boolean) -> Unit,
    onSetBookmarks: (List<Bookmark>) -> Unit,
    onSetDownloads: (List<Download>) -> Unit,
    onSetTimeline: (List<AuroraLogEvent>) -> Unit,
    onSetCrashedTabId: (String?) -> Unit,
    onSetIsRendererCrashed: (Boolean) -> Unit,
    onSetLibrarySearchQuery: (String) -> Unit,
    onSetLibraryActiveFilter: (String) -> Unit,
    onSetBookmarkSearchQuery: (String) -> Unit,
    onSetSelectedBookmarkFolderId: (Long?) -> Unit,
    onCreateBookmarkFolder: (String) -> Unit,
    onRemoveBookmarkFolder: (BookmarkFolder) -> Unit,
    onRemoveBookmark: (Bookmark) -> Unit,
    onMoveBookmark: (Bookmark, Long) -> Unit,
    onDevClick: () -> Unit,
    continueBrowsing: List<ContinueBrowsingUiModel> = emptyList(),
    favoriteSites: List<FavoriteUiModel> = emptyList(),
    openTabs: List<com.aurora.ui.model.TabUiModel> = emptyList(),
    activeTabId: String = "",
    onTabManagementClose: (String) -> Unit = {},
    onTabManagementCloseAll: () -> Unit = {},
    onTabManagementSwitch: (String) -> Unit = {},
    offlineFocusIndex: Int = 0,
    crashFocusIndex: Int = 0
) {
    var showNetworkPanel by remember { mutableStateOf(false) }
    val isDark = settings.activeTheme == "Aurora Dark"
    val historyState by historyVm.screenState.collectAsState()
    val homeHistoryUi = historyState.entries.take(4).map { e ->
        val host = e.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        HistoryUiModel(
            id = e.url, title = e.title.ifEmpty { host.ifEmpty { "Untitled" } }, url = e.url,
            domain = host.ifEmpty { e.url }, timeText = formatRelativeTime(e.timestamp), visitCount = e.visitCount
        )
    }
    val homeQuickActions = listOf(
        QuickActionUiModel("qa0", "Library", "Bookmarks, downloads & files", Icons.Default.Book, AuroraColors.auroraBlue),
        QuickActionUiModel("qa1", "Settings", "Themes, engine & layout", Icons.Default.Settings, AuroraColors.auroraEmerald),
        QuickActionUiModel("qa2", "Passwords", "Saved login credentials", Icons.Default.Lock, AuroraColors.auroraAmber),
        QuickActionUiModel("qa3", "Ask Aurora AI", "Integrated helper engine", Icons.Default.AutoAwesome, AuroraColors.auroraPurple),
        QuickActionUiModel("qa4", "Diagnostics", "CPU, memory, kernel logs", Icons.Default.Terminal, AuroraColors.auroraBlue),
        QuickActionUiModel("qa5", "Manage Tabs", "Close or switch browser tabs", Icons.Default.DesktopWindows, AuroraColors.auroraPurple)
    )
    val effectiveDpad: (String) -> Boolean = { dir -> handleDpadPress(dir) }
    val effectiveSelect: () -> Boolean = { handleSelectPress() }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.fillMaxSize()) {
                    if (currentScreen != Screen.TabManagement) {
                        DashboardHeader(
                            home = home, developerMode = developerMode,
                            activeTab = activeTab, backgroundAudioActive = backgroundAudioActive,
                            isOffline = isOffline, isDiagnosticsOpen = isDiagnosticsOpen,
                            currentScreen = currentScreen, focusEngine = focusEngine,
                            onDevClick = onDevClick, onSetOffline = onSetOffline,
                            onShowNetworkInfo = { showNetworkPanel = true },
                            onSetIsDiagnosticsOpen = onSetIsDiagnosticsOpen,
                            onSetCurrentScreen = onSetCurrentScreen
                        )
                    }
                    if (pipVideo != null) {
                        Box(Modifier.align(Alignment.End).padding(24.dp).size(288.dp, 176.dp).background(Color.Black, RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white10, RoundedCornerShape(16.dp))) {
                            Box(Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Icon(Icons.Default.Tv, null, Modifier.size(12.dp), AuroraColors.auroraBlue); Text("PiP Active", color = AuroraColors.white80, fontSize = 9.sp) }
                            }
                            Box(Modifier.align(Alignment.TopEnd).padding(4.dp).clickable { onSetPipVideo(null) }) { Icon(Icons.Default.Close, null, Modifier.size(14.dp), AuroraColors.white) }
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            isOffline -> OfflineScreen({ onSetCurrentScreen(Screen.Library) }, { handleWebNavigation("https://wikipedia.org/wiki/living-glass"); onSetOffline(false) }, { onSetOffline(false) }, focusedIndex = offlineFocusIndex)
                            isRendererCrashed -> CrashRecoveryScreen({ handleRestoreCrashedTab() }, { onSetIsRendererCrashed(false); onSetCrashedTabId(null); onSetCurrentScreen(Screen.Home) }, focusedIndex = crashFocusIndex)
                            isDiagnosticsOpen -> DiagnosticsDashboard(processes = processes, onKillProcess = { handleKillProcess(it) }, timeline = timeline, onClearTimeline = { onSetTimeline(emptyList()) }, onRunDiagnostics = handleRunDiagnostics, modifier = Modifier)
                            activeMediaViewer != null -> when (activeMediaViewer.mimeType) {
                                "application/pdf" -> PDFViewer(download = activeMediaViewer, onClose = { onSetActiveMediaViewer(null) })
                                "video/mp4" -> VideoPlayer(download = activeMediaViewer, onClose = { onSetActiveMediaViewer(null) }, onTogglePiP = { onSetPipVideo(activeMediaViewer); onSetActiveMediaViewer(null) })
                                "audio/mp3" -> AudioPlayer(download = activeMediaViewer, onClose = { onSetActiveMediaViewer(null) }, isPlayingBackground = backgroundAudioActive, onToggleBackgroundPlay = { onToggleBackgroundAudio() })
                                "image/png" -> ImageGallery(download = activeMediaViewer, onClose = { onSetActiveMediaViewer(null) })
                                "text/plain" -> TextViewer(download = activeMediaViewer, onClose = { onSetActiveMediaViewer(null) })
                            }
                            currentScreen == Screen.PerformanceCenter -> PerformanceCenterScreen(processes, timeline, { handleKillProcess(it) }, handleRunDiagnostics, onBack = { onSetCurrentScreen(Screen.Home) }, onClearTimeline = { onSetTimeline(emptyList()) })
                            currentScreen == Screen.Home -> HomeScreen(
                                searchQuery = home.searchQuery,
                                currentProfile = com.aurora.ui.screens.Profile(currentProfile.id, currentProfile.name, currentProfile.avatar, currentProfile.isGuest, currentProfile.isSynced),
                                downloads = downloads.map { DownloadUiModel(id = it.id, fileName = it.fileName, progress = it.progress, mimeType = it.mimeType, totalSize = it.totalSize, url = it.url) },
                                developerMode = developerMode,
                                focusedZone = home.focusedZone,
                                focusedItemIndex = home.focusedItemIndex,
                                onZoneFocusChange = { zone, idx -> home.focusedZone = zone; home.focusedItemIndex = idx },
                                onNavigate = handleWebNavigation,
                                onSearchFocus = { browser.isKeyboardOpen = true; home.isOmniboxFocused = true },
                                onOpenLibrary = { onSetCurrentScreen(Screen.Library) },
                                onOpenSettings = { onSetCurrentScreen(Screen.Settings) },
                                onOpenPasswords = { onSetCurrentScreen(Screen.PasswordManager) },
                                onOpenDiagnostics = { onSetIsDiagnosticsOpen(true); triggerToast("Diagnostics") },
                                onAIAssistant = { browser.isKeyboardOpen = true; home.isOmniboxFocused = true; home.searchQuery = "Ask Aurora..." },
                                uiState = HomeUiState(
                                    favorites = favoriteSites,
                                    continueBrowsing = continueBrowsing,
                                    downloads = downloads.map { DownloadUiModel(id = it.id, fileName = it.fileName, progress = it.progress, mimeType = it.mimeType, totalSize = it.totalSize, url = it.url) },
                                    history = homeHistoryUi,
                                    quickActions = homeQuickActions
                                )
                            )
                            currentScreen == Screen.Settings -> HomeScreenContent(
                                currentScreen, home.focusedZone, home.focusedItemIndex, { home.focusedZone = it }, { home.focusedItemIndex = it }, handleWebNavigation, { browser.isKeyboardOpen = true; home.isOmniboxFocused = true }, isDark, downloads, settings.activeAccent, currentProfile, developerMode, { onSetCurrentScreen(it) }, { onSetLibraryActiveFilter(it) }, { idx -> when (idx) { 0 -> { onSetCurrentScreen(Screen.History); triggerToast("History") }; 1 -> { onSetCurrentScreen(Screen.Settings); triggerToast("Settings") }; 2 -> { browser.isKeyboardOpen = true; home.isOmniboxFocused = true; home.searchQuery = "Ask Aurora..." }; 3 -> if (developerMode) { onSetIsDiagnosticsOpen(true); triggerToast("Diagnostics") } } },
                                searchQuery = home.searchQuery,
                                activeTheme = settings.activeTheme, setActiveTheme = { settings.activeTheme = it }, activeSettingsCategory = settings.activeSettingsCategory, setActiveSettingsCategory = { settings.activeSettingsCategory = it }, largerUI = settings.largerUI, setLargerUI = { settings.largerUI = it }, activeAccentColor = settings.activeAccent, setActiveAccent = { settings.activeAccent = it }, searchEngine = settings.searchEngine, setSearchEngine = { settings.searchEngine = it }, isPerfOverlayEnabled = settings.isPerfOverlayEnabled, setIsPerfOverlayEnabled = { settings.isPerfOverlayEnabled = it }, isRemoteVisible = settings.isRemoteVisible, setIsRemoteVisible = { settings.isRemoteVisible = it }, isFpsCounterEnabled = settings.isFpsCounterEnabled, setIsFpsCounterEnabled = { settings.isFpsCounterEnabled = it }, isMemoryUsageEnabled = settings.isMemoryUsageEnabled, setIsMemoryUsageEnabled = { settings.isMemoryUsageEnabled = it }, animationSpeedMultiplier = settings.animationSpeedMultiplier, setAnimationSpeedMultiplier = { settings.animationSpeedMultiplier = it }, devMode = developerMode, setBookmarks = { onSetBookmarks(it) }, triggerToast = triggerToast, handleHomePress = handleHomePress, onNavigatePerformanceCenter = { onSetCurrentScreen(Screen.PerformanceCenter) }, bookmarks = bookmarks, benchmarkMode = benchmarkMode, handleBenchmarkToggle = handleBenchmarkToggle, focusEngine = focusEngine,
                                showNetworkPanel = showNetworkPanel, isOffline = isOffline, onDismissNetworkPanel = { showNetworkPanel = false }
                            )
                            currentScreen == Screen.Bookmarks -> BookmarkManagerScreen(
                                bookmarks = bookmarks,
                                folders = bookmarkFolders,
                                selectedFolderId = selectedBookmarkFolderId,
                                searchQuery = bookmarkSearchQuery,
                                onSearchChange = onSetBookmarkSearchQuery,
                                onSelectFolder = onSetSelectedBookmarkFolderId,
                                onCreateFolder = onCreateBookmarkFolder,
                                onRemoveFolder = onRemoveBookmarkFolder,
                                onOpenBookmark = { handleWebNavigation(it.url) },
                                onMoveBookmark = onMoveBookmark,
                                onRemoveBookmark = onRemoveBookmark,
                                onHome = handleHomePress
                            )
                            currentScreen == Screen.History -> com.aurora.ui.screens.HistoryScreen(viewModel = historyVm, onNavigate = handleWebNavigation, onHome = handleHomePress)
                            currentScreen == Screen.Downloads -> com.aurora.ui.screens.DownloadScreen(
                                downloadManager = downloadManager,
                                context = context,
                                onHome = handleHomePress
                            )
                            currentScreen == Screen.Library -> LibraryScreen(bookmarks = bookmarks, setBookmarks = { onSetBookmarks(it) }, downloads = downloads, setDownloads = { onSetDownloads(it) }, onNavigate = handleWebNavigation, onOpenViewer = { onSetActiveMediaViewer(ViewerDownload(id = it.id, fileName = it.fileName, url = it.url, totalSize = it.totalSize, mimeType = it.mimeType)) }, librarySearchQuery = librarySearchQuery, setLibrarySearchQuery = { onSetLibrarySearchQuery(it) }, libraryActiveFilter = libraryActiveFilter, setLibraryActiveFilter = { onSetLibraryActiveFilter(it) }, onHome = handleHomePress, triggerToast = triggerToast, addLog = addLog)
                            currentScreen == Screen.TabManagement -> TabManagementScreen(
                                tabs = openTabs,
                                activeTabId = activeTabId,
                                focusEngine = focusEngine,
                                onCloseTab = onTabManagementClose,
                                onCloseAll = onTabManagementCloseAll,
                                onSwitchToTab = onTabManagementSwitch,
                                onBack = handleHomePress
                            )
                            currentScreen == Screen.PasswordManager -> PasswordManagerScreen(
                                loginStorage = activeTab?.session?.getLoginStorage(),
                                onClose = { onSetCurrentScreen(Screen.Home) }
                            )
                        }
                        val overlayActions = object : OverlayActions {
                            override fun dismissQuickSettings() { overlayCoordinator.close() }
                            override fun dismissCommandPalette() { overlayCoordinator.closeCommandPalette() }
                            override fun updateCommandQuery(query: String) { overlayCoordinator.updateCommandQuery(query) }
                            override fun updateFindQuery(query: String) {
                                overlayCoordinator.updateFindQuery(query)
                                val session = activeTab?.session
                                if (query.isNotEmpty()) session?.findInPage(query)
                            }
                            override fun closeFindInPage() { overlayCoordinator.closeFindInPage() }
                            override fun dismissContextMenu() { overlayCoordinator.close() }
                            override fun dismissSiteInfo() { overlayCoordinator.close() }
                            override fun dismissPermissions() {
                                val req = (overlayCoordinator.state as? OverlayState.Permissions)?.request
                                req?.deny(); overlayCoordinator.close()
                            }
                            override fun allowPermission() {
                                val session = activeTab?.session
                                val req = (overlayCoordinator.state as? OverlayState.Permissions)?.request
                                session?.getPermissionsService()?.setPermission(req?.domain ?: "", req?.permission ?: "", true)
                                req?.grant(); overlayCoordinator.close(); triggerToast("Permission granted")
                            }
                            override fun denyPermission() {
                                val session = activeTab?.session
                                val req = (overlayCoordinator.state as? OverlayState.Permissions)?.request
                                session?.getPermissionsService()?.setPermission(req?.domain ?: "", req?.permission ?: "", false)
                                req?.deny(); overlayCoordinator.close()
                            }
                            override fun cancelVoice() { onSetIsVoiceListening(false) }
                            override fun createNewTab(isPrivate: Boolean) { handleCreateNewTab(isPrivate) }
                            override fun openInNewTab(url: String, isPrivate: Boolean) { handleOpenInNewTab(url, isPrivate); overlayCoordinator.close() }
                            override fun openReaderMode(url: String) { handleOpenReader(url); overlayCoordinator.close() }
                            override fun toggleBookmark() { handleToggleBookmark() }
                            override fun toggleDesktop() { activeTab?.controller?.setDesktopMode(!(activeTab?.controller?.isDesktopMode() ?: false)) }
                            override fun refresh() { activeTab?.controller?.reload() }
                            override fun share(url: String) { val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { putExtra(android.content.Intent.EXTRA_TEXT, url); type = "text/plain" }; context.startActivity(android.content.Intent.createChooser(intent, "Share via")) }
                            override fun copyUrl(url: String, msg: String) { val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager; cm.setPrimaryClip(android.content.ClipData.newPlainText("URL", url)); triggerToast(msg) }
                            override fun goSettings() { onSetCurrentScreen(Screen.Settings) }
                            override fun goScreen(screen: Screen) { onSetCurrentScreen(screen) }
                            override fun clearHistory() { historyVm.clearAll() }
                            override fun runDiagnostics() { handleRunDiagnostics() }
                            override fun showToast(msg: String) { triggerToast(msg) }
                            override fun findNext() { activeTab?.session?.findNextInPage(true) }
                            override fun findPrevious() { activeTab?.session?.findNextInPage(false) }
                            override fun openFindInPage() { onOpenFindInPage() }
                            override fun openTranslation() { }
                        }
                        val overlayDeps = OverlayDependencies(
                            currentScreen = currentScreen,
                            brightness = settings.brightness,
                            developerMode = developerMode,
                            activeAccent = settings.activeAccent,
                            activeTheme = settings.activeTheme,
                            bookmarks = bookmarks,
                            activeTab = activeTab,
                        )
                        OverlayLayer(
                            state = overlayCoordinator.state,
                            commandQuery = overlayCoordinator.commandQuery,
                            deps = overlayDeps,
                            actions = overlayActions,
                            toastMessage = toastMessage,
                            isVoiceListening = isVoiceListening,
                            voiceWave = voiceWave,
                            voiceOutputMessage = voiceOutputMessage,
                        )
                    }
                    if (currentScreen != Screen.TabManagement) Box(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp), contentAlignment = Alignment.Center) { Text("Designed for couch viewing \u2022 Rec. 2020 Display Compliant", color = AuroraColors.white10, fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp) }
                }
            }
            if (settings.isRemoteVisible) RemoteControl(onDpadPress = { d -> effectiveDpad(d); Unit }, onSelectPress = { effectiveSelect(); Unit }, onBackPress = handleBackPress, onMenuPress = {
                if (overlayCoordinator.isShowing || isDiagnosticsOpen || isVoiceListening) {
                    overlayCoordinator.close(); onSetIsDiagnosticsOpen(false)
                } else {
                    overlayCoordinator.onMenuPressed(
                        isBrowserScreen = currentScreen == Screen.Browser,
                        isDiagnosticsOpen = isDiagnosticsOpen,
                        isVoiceListening = isVoiceListening
                    )
                }
            }, onHomePress = handleHomePress, onSearchPress = { handleSearchPress() }, onVoicePress = { handleVoiceSearch() }, isKeyboardMode = !browser.isPointerMode, onToggleInputMode = { browser.isPointerMode = !browser.isPointerMode }, modifier = Modifier.fillMaxHeight(0.9f))
        }
    }
}

@Composable
fun DashboardHeader(
    home: HomeCoordinator, developerMode: Boolean,
    activeTab: TabSession?, backgroundAudioActive: Boolean,
    isOffline: Boolean, isDiagnosticsOpen: Boolean,
    currentScreen: Screen, focusEngine: FocusEngine,
    onDevClick: () -> Unit, onSetOffline: (Boolean) -> Unit,
    onShowNetworkInfo: () -> Unit = {},
    onSetIsDiagnosticsOpen: (Boolean) -> Unit,
    onSetCurrentScreen: (Screen) -> Unit
) {
    Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.6f)).border(1.dp, AuroraColors.white5).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
FocusBinding(id = "header_brand", focusEngine = focusEngine!!, group = "header", order = 0, onClick = { onDevClick() }, externalFocused = home.focusedZone == "header_brand") { isFocused ->
            Row(Modifier.background(if (isFocused) AuroraColors.auroraBlue.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                val ds = rememberBrandDotPulse(); Box(Modifier.size(10.dp).graphicsLayer(scaleX = ds, scaleY = ds).background(AuroraColors.auroraBlue, CircleShape))
                Text("AURORA BROWSER", color = AuroraColors.white, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                if (developerMode) Box(Modifier.background(AuroraColors.auroraBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("DEV", color = AuroraColors.auroraBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            }
            }
            if (activeTab?.session?.isPrivate == true) Box(Modifier.background(AuroraColors.auroraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.auroraPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text("Private Session", color = AuroraColors.auroraPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            if (backgroundAudioActive) Box(Modifier.background(AuroraColors.auroraPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.auroraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.MusicNote, null, Modifier.size(12.dp), AuroraColors.auroraPurple); Text("Background Audio Playing", color = AuroraColors.auroraPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(AuroraColors.neutral900.copy(alpha = 0.8f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("${home.searchQuery}", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
            Text("|", color = AuroraColors.white10, fontSize = 12.sp)
            val nc = if (isOffline) AuroraColors.auroraAmber else AuroraColors.auroraEmerald
            FocusBinding(id = "header_wifi", focusEngine = focusEngine!!, group = "header", order = 1, onClick = { onShowNetworkInfo() }, externalFocused = home.focusedZone == "header_wifi") { isFocused ->
            Box(Modifier.background(if (isFocused) nc.copy(alpha = 0.25f) else if (isOffline) AuroraColors.auroraAmber.copy(alpha = 0.2f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) nc else if (isOffline) AuroraColors.auroraAmber.copy(alpha = 0.2f) else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { val od = rememberBrandDotPulse(); Box(Modifier.size(6.dp).graphicsLayer(scaleX = od, scaleY = od).background(nc, CircleShape)); Icon(Icons.Default.Wifi, null, Modifier.size(14.dp), nc); Text(if (isOffline) "OFFLINE" else "ONLINE", color = nc, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
            }
            Text("|", color = AuroraColors.white10, fontSize = 12.sp)
            FocusBinding(id = "header_settings", focusEngine = focusEngine!!, group = "header", order = 2, onClick = { onSetCurrentScreen(Screen.Settings) }, externalFocused = home.focusedZone == "header_settings") { isFocused ->
            Box(Modifier.background(if (isFocused) AuroraColors.auroraEmerald.copy(alpha = 0.3f) else if (currentScreen == Screen.Settings) AuroraColors.auroraEmerald.copy(alpha = 0.2f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) AuroraColors.auroraEmerald else if (currentScreen == Screen.Settings) AuroraColors.auroraEmerald else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, Modifier.size(14.dp), AuroraColors.white70); Text("SETTINGS", color = AuroraColors.white70, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
            }
            if (developerMode) { Text("|", color = AuroraColors.white10, fontSize = 12.sp); FocusBinding(id = "header_diagnostics", focusEngine = focusEngine!!, group = "header", order = 3, onClick = { onSetIsDiagnosticsOpen(!isDiagnosticsOpen) }, externalFocused = home.focusedZone == "header_diagnostics") { isFocused -> Box(Modifier.background(if (isFocused) AuroraColors.auroraBlue.copy(alpha = 0.3f) else if (isDiagnosticsOpen) AuroraColors.auroraBlue.copy(alpha = 0.2f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) AuroraColors.auroraBlue else if (isDiagnosticsOpen) AuroraColors.auroraBlue else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Terminal, null, Modifier.size(14.dp), AuroraColors.white70); Text("DIAGNOSTICS", color = AuroraColors.white70, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } } }
        }
    }
}

@Composable
fun QuickActionsSection(
    developerMode: Boolean, focusEngine: FocusEngine?,
    onQuickAction: (Int) -> Unit
) {
    val s5 = rememberStaggerAlpha(StaggerDelay.Stagger5)
    Column(Modifier.graphicsLayer { alpha = s5 }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraEmerald, CircleShape)); Text("CONTROL CENTER QUICK ACTIONS", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("SYSTEM CONFIG • D-PAD INTERACTIVE", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(Triple("History Log", "Explore your browsing trail", 0) to AuroraColors.auroraBlue, Triple("Browser Settings", "Themes, engine & layout", 1) to AuroraColors.auroraEmerald, Triple("Ask Aurora AI", "Integrated helper engine", 2) to AuroraColors.auroraPurple).forEach { (item, ac) -> val (title, desc, idx) = item; val sr3 = rememberSparkleRotation(); FocusBinding(id = "qa_$idx", focusEngine = focusEngine!!, group = "quick_actions", order = idx, onClick = { onQuickAction(idx) }) { isFocused -> Box(Modifier.weight(1f).cardLift(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).border(1.dp, if (isFocused) ac else AuroraColors.white5, RoundedCornerShape(24.dp)).padding(16.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).background(if (isFocused) ac.copy(alpha = 0.2f) else ac.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, ac.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).graphicsLayer { if (isFocused) { scaleX = 1.1f; scaleY = 1.1f } }, contentAlignment = Alignment.Center) { when (idx) { 0 -> Icon(Icons.Default.Book, null, Modifier.size(18.dp), ac); 1 -> Icon(Icons.Default.Settings, null, Modifier.size(18.dp), ac); 2 -> Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp).graphicsLayer { if (isFocused) rotationZ = sr3 else rotationZ = 0f }, ac) } }; Column { Text(title, color = if (isFocused) ac else AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(desc, color = AuroraColors.white45, fontSize = 8.sp) } } } } }
            if (developerMode) { FocusBinding(id = "qa_3", focusEngine = focusEngine!!, group = "quick_actions", order = 3, onClick = { onQuickAction(3) }) { isFocused -> Box(Modifier.weight(1f).cardLift(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).border(1.dp, if (isFocused) AuroraColors.auroraBlue else AuroraColors.white5, RoundedCornerShape(24.dp)).padding(16.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).background(AuroraColors.auroraBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).graphicsLayer { if (isFocused) { scaleX = 1.1f; scaleY = 1.1f } }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Terminal, null, Modifier.size(16.dp), AuroraColors.auroraBlue) }; Column { Text("Dev Diagnostics", color = if (isFocused) AuroraColors.auroraBlue else AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("CPU, memory, kernel logs", color = AuroraColors.white45, fontSize = 8.sp) } } } } }
        }
    }
}

@Composable
fun TelemetryCard(title: String, value: String, subtitle: String, color: Color, percent: Float, modifier: Modifier) {
    val animatedProgress by animateFloatAsState(targetValue = percent, animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "progress")
    Column(modifier = modifier.padding(4.dp).background(AuroraColors.neutral900.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, color = AuroraColors.white50, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("• Healthy", color = AuroraColors.auroraEmerald, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(16.dp)); Text(value, color = AuroraColors.white, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace); Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).background(AuroraColors.neutral800, RoundedCornerShape(4.dp))) { Box(Modifier.fillMaxWidth(animatedProgress).height(6.dp).background(color, RoundedCornerShape(4.dp))) }
    }
}

@Composable
fun OfflineScreen(onBrowseFiles: () -> Unit, onReadCached: () -> Unit, onReconnect: () -> Unit, focusedIndex: Int = 0) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0C0C0F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.background(AuroraColors.neutral900, RoundedCornerShape(24.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(24.dp)).padding(32.dp)) {
            Box(Modifier.size(64.dp).background(AuroraColors.auroraAmber.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.auroraAmber.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Wifi, null, Modifier.size(32.dp), AuroraColors.auroraAmber) }
            Text("Connection Lost • Offline Assistant", color = AuroraColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Wi-Fi is currently disconnected. However, you can still view completed downloads, explore bookmarks, or access cached local resources.", color = AuroraColors.white40, fontSize = 10.sp, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.background(if (focusedIndex == 0) AuroraColors.auroraBlue.copy(alpha = 0.35f) else AuroraColors.neutral800, RoundedCornerShape(12.dp)).border(if (focusedIndex == 0) 1.5.dp else 0.dp, AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 10.dp).clickable { onBrowseFiles() }) { Text("Browse Files", color = if (focusedIndex == 0) AuroraColors.auroraBlue else AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Box(Modifier.background(if (focusedIndex == 1) AuroraColors.auroraBlue.copy(alpha = 0.35f) else AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).border(if (focusedIndex == 1) 1.5.dp else 0.dp, AuroraColors.white70, RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 10.dp).clickable { onReadCached() }) { Text("Read Cached Wiki", color = if (focusedIndex == 1) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
            Text("Force Reconnect Try", color = if (focusedIndex == 2) AuroraColors.auroraEmerald else AuroraColors.auroraEmerald, fontSize = 10.sp, modifier = Modifier.clickable { onReconnect() })
        }
    }
}

@Composable
fun CrashRecoveryScreen(onRestore: () -> Unit, onHome: () -> Unit, focusedIndex: Int = 0) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0E0B0D)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.background(AuroraColors.neutral900, RoundedCornerShape(24.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(24.dp)).padding(32.dp)) {
            Box(Modifier.size(64.dp).background(AuroraColors.auroraRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.auroraRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, null, Modifier.size(32.dp), AuroraColors.auroraRed) }
            Text("Renderer Process Crushed!", color = AuroraColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("The virtual tab rendering process stopped responding unexpectedly. Other tabs are isolated and healthy.", color = AuroraColors.white40, fontSize = 10.sp, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.background(if (focusedIndex == 0) AuroraColors.auroraBlue.copy(alpha = 0.35f) else AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).border(if (focusedIndex == 0) 1.5.dp else 0.dp, AuroraColors.white70, RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 10.dp).clickable { onRestore() }) { Text("Reload & Restore State", color = if (focusedIndex == 0) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Box(Modifier.background(if (focusedIndex == 1) AuroraColors.auroraBlue.copy(alpha = 0.35f) else AuroraColors.neutral800, RoundedCornerShape(12.dp)).border(if (focusedIndex == 1) 1.5.dp else 0.dp, AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 10.dp).clickable { onHome() }) { Text("Return Home", color = if (focusedIndex == 1) AuroraColors.auroraBlue else AuroraColors.white, fontSize = 11.sp) } }
        }
    }
}

@Composable
fun PerformanceCenterScreen(processes: List<AuroraProcess>, timeline: List<AuroraLogEvent>, onKillProcess: (Int) -> Unit, onRunDiagnostics: () -> Unit, onBack: () -> Unit, onClearTimeline: () -> Unit = {}, modifier: Modifier = Modifier) {
    val totalMem = processes.sumOf { it.memory }.coerceAtLeast(1)
    val avgCpu = if (processes.isNotEmpty()) (processes.sumOf { it.cpu } / processes.size) else 0
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0B0D12)).padding(24.dp)) {
        Row(Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.size(40.dp).background(Brush.linearGradient(listOf(AuroraColors.auroraBlue, AuroraColors.auroraPurple)), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Terminal, null, Modifier.size(22.dp), Color.White) }; Column { Text("AURORA TELEMETRY & PERFORMANCE CENTER", color = AuroraColors.white, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Real-time system health, process scheduling, and diagnostic data", color = AuroraColors.white40, fontSize = 9.sp) } }
            Box(Modifier.background(AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp).clickable { onBack() }) { Text("Back to Dashboard", color = AuroraColors.white, fontSize = 11.sp) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TelemetryCard("Total Memory", "$totalMem MB", "${processes.size} processes active", AuroraColors.auroraBlue, (totalMem.toFloat() / 2048f).coerceIn(0.01f, 1f), Modifier.weight(1f))
            TelemetryCard("CPU Activity", "$avgCpu%", "Average across ${processes.size} tasks", AuroraColors.auroraPurple, (avgCpu.toFloat() / 100f).coerceIn(0.01f, 1f), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(AuroraColors.auroraBlue.copy(alpha = 0.1f), AuroraColors.neutral900.copy(alpha = 0.4f), AuroraColors.auroraPurple.copy(alpha = 0.1f))), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { val sr = rememberSparkleRotation(); Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp).graphicsLayer { rotationZ = sr }, AuroraColors.auroraBlue); Text("Automated Hardware & Memory Optimization Engine", color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Text("Simulate kernel diagnostics self-test, flush garbage collection allocations, and verify thread integrity.", color = AuroraColors.white40, fontSize = 9.sp) }
                Box(Modifier.background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp).clickable { onRunDiagnostics() }) { Text("Run System Self-Test", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(0.58f).background(AuroraColors.neutral900.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Active Processes Manager", color = AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("${processes.size} tasks running", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
                Spacer(Modifier.height(8.dp)); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) { processes.forEach { p -> Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Text("#${p.pid}", color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.2f)); Text(p.name, color = AuroraColors.white90, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.5f)); Text("${p.cpu}%", color = AuroraColors.auroraBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.15f)); if (p.type == "Renderer") Box(Modifier.background(AuroraColors.auroraRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).border(1.dp, AuroraColors.auroraRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp).clickable { onKillProcess(p.pid) }) { Text("Kill Process", color = AuroraColors.auroraRed, fontSize = 7.sp, fontWeight = FontWeight.Bold) } else Text("Protected", color = AuroraColors.white20, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f)) } } }
            }
            Column(Modifier.weight(0.42f).background(AuroraColors.neutral900.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Telemetry Event Logs", color = AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold); Box(Modifier.background(AuroraColors.neutral850, RoundedCornerShape(4.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp).clickable { onClearTimeline() }) { Text("Clear", color = AuroraColors.white40, fontSize = 8.sp) } }
                Spacer(Modifier.height(8.dp))
                if (timeline.isEmpty()) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No logged telemetry events.", color = AuroraColors.white30, fontSize = 9.sp, fontFamily = FontFamily.Monospace) }
                else Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { timeline.take(15).forEach { event -> Column(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.3f), RoundedCornerShape(6.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(6.dp)).padding(8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("[]", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace); Text(event.category, color = AuroraColors.auroraBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }; Text(event.message, color = AuroraColors.white80, fontSize = 8.sp, fontFamily = FontFamily.Monospace) } } }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    currentScreen: Screen, focusedZone: String, focusedItemIndex: Int, setFocusedZone: (String) -> Unit, setFocusedItemIndex: (Int) -> Unit,
    onNavigate: (String) -> Unit, onOpenKeyboard: () -> Unit, isDark: Boolean, downloads: List<Download>, activeAccent: String,
    currentProfile: Profile, developerMode: Boolean, setCurrentScreen: (Screen) -> Unit, setLibraryActiveFilter: (String) -> Unit,
    onQuickAction: (Int) -> Unit,
    searchQuery: String = "",
    activeTheme: String = "Aurora Dark", setActiveTheme: (String) -> Unit = {}, activeSettingsCategory: String = "Appearance",
    setActiveSettingsCategory: (String) -> Unit = {}, largerUI: Boolean = false, setLargerUI: (Boolean) -> Unit = {},
    activeAccentColor: String = "#4DA3FF", setActiveAccent: (String) -> Unit = {}, searchEngine: String = "Google",
    setSearchEngine: (String) -> Unit = {}, isPerfOverlayEnabled: Boolean = false, setIsPerfOverlayEnabled: (Boolean) -> Unit = {},
    isRemoteVisible: Boolean = false, setIsRemoteVisible: (Boolean) -> Unit = {}, isFpsCounterEnabled: Boolean = true,
    setIsFpsCounterEnabled: (Boolean) -> Unit = {}, isMemoryUsageEnabled: Boolean = true, setIsMemoryUsageEnabled: (Boolean) -> Unit = {},
    animationSpeedMultiplier: Float = 1f, setAnimationSpeedMultiplier: (Float) -> Unit = {}, devMode: Boolean = false,
    setBookmarks: (List<Bookmark>) -> Unit = {}, triggerToast: (String) -> Unit = {},
    handleHomePress: () -> Unit = {}, onNavigatePerformanceCenter: () -> Unit = {}, bookmarks: List<Bookmark> = emptyList(),
    benchmarkMode: Boolean = false, handleBenchmarkToggle: (Boolean) -> Unit = {},
    sessionManager: SessionManager? = null,
    focusEngine: FocusEngine? = null,
    showNetworkPanel: Boolean = false,
    isOffline: Boolean = false,
    onDismissNetworkPanel: () -> Unit = {},
    activeTab: TabSession? = null
) {
    val homeScope = rememberCoroutineScope()
    val scrollState = rememberScrollState(); val greeting = getGreeting(); val isSettings = currentScreen == Screen.Settings
    val settingsAlpha = if (isSettings) rememberSettingsAlpha() else Pair(1f, 0f)
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (isSettings) Modifier.graphicsLayer { scaleX = 0.97f; scaleY = 0.97f; alpha = 0.35f } else Modifier)) {
            Box(Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val (dx1, dy1) = rememberAmbientPulseOffset(0); val (dx2, dy2) = rememberAmbientPulseOffset(1)
                    Box(Modifier.fillMaxWidth().height(0.dp)) {
                        Box(Modifier.size(320.dp).graphicsLayer { alpha = 0.6f; translationX = dx1; translationY = dy1 }.background(AuroraColors.auroraBlue.copy(alpha = 0.15f), CircleShape))
                        Box(Modifier.size(384.dp).graphicsLayer { alpha = 0.5f; translationX = dx2; translationY = dy2 }.background(AuroraColors.auroraPurple.copy(alpha = 0.1f), CircleShape))
                    }
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, AuroraColors.white10, Color.Transparent))).graphicsLayer { alpha = 0.6f })
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, AuroraColors.white5, Color.Transparent))).graphicsLayer { alpha = 0.4f })
                    val s0 = rememberStaggerAlpha(StaggerDelay.Stagger0)
                    Column(Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 12.dp).graphicsLayer { alpha = s0 }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { val ds = rememberBrandDotPulse(); Box(Modifier.size(6.dp).graphicsLayer(scaleX = ds, scaleY = ds).background(AuroraColors.auroraBlue, CircleShape)); Text("Aurora Operating Core", color = AuroraColors.white40, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column { Text("$greeting, ${currentProfile.name}", color = AuroraColors.white, fontSize = 28.sp, fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AutoAwesome, null, Modifier.size(14.dp), AuroraColors.auroraPurple); Text("Living Glass interface is fully optimized for 3-meter living room viewing.", color = AuroraColors.white45, fontSize = 11.sp) } }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(AuroraColors.white5, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white10, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Icon(Icons.Default.Book, null, Modifier.size(14.dp), AuroraColors.auroraBlue); Text("LIVING GLASS V2.0", color = AuroraColors.white50, fontSize = 9.sp, fontFamily = FontFamily.Monospace) }
                        }
                    }
                    val s1 = rememberStaggerAlpha(StaggerDelay.Stagger1); val cb = rememberCursorBlink()
                    FocusBinding(id = "search_bar", focusEngine = focusEngine!!, group = "search", order = 0, onClick = { onOpenKeyboard() }) { isFocused ->
                    Box(Modifier.widthIn(max = 620.dp).fillMaxWidth().align(Alignment.CenterHorizontally).graphicsLayer { alpha = s1 }.focusScale(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral950 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 18.dp)) {
                        if (isFocused) Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(AuroraColors.auroraBlue, AuroraColors.auroraEmerald, AuroraColors.auroraPurple)), RoundedCornerShape(24.dp)).padding(1.5.dp)) { Box(Modifier.fillMaxSize().background(AuroraColors.neutral950, RoundedCornerShape(22.dp))) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, Modifier.size(20.dp), if (isFocused) AuroraColors.auroraBlue else AuroraColors.white40)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(if (searchQuery.isNotEmpty()) searchQuery else if (isFocused) "D-pad OK to search or voice command..." else "Search web, enter address, or ask Aurora AI...", color = if (isFocused || searchQuery.isNotEmpty()) AuroraColors.white else AuroraColors.white45, fontSize = 12.sp)
                                    if (isFocused) Box(Modifier.size(2.dp, 14.dp).background(if (cb) AuroraColors.auroraBlue else Color.Transparent))
        }
    }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(AuroraColors.white5, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white10, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.Mic, null, Modifier.size(14.dp), if (isFocused) AuroraColors.auroraBlue else AuroraColors.white50)
                                val sr2 = rememberSparkleRotation(); Icon(Icons.Default.AutoAwesome, null, Modifier.size(14.dp).graphicsLayer { rotationZ = if (isFocused) sr2 else 0f }, AuroraColors.auroraPurple)
                            }
                        }
                    }
                    }
                    ContinueBrowsingSection(focusEngine = focusEngine!!, onNavigate = onNavigate)
                    StreamingHubSection(focusEngine = focusEngine!!, onNavigate = onNavigate)
                    SpeedDialSection(focusEngine = focusEngine!!, onNavigate = onNavigate)
                    TrendingSection(focusEngine = focusEngine!!, onNavigate = onNavigate, downloads = downloads, setCurrentScreen = { setCurrentScreen(it) }, triggerToast = triggerToast)
                    QuickActionsSection(developerMode = developerMode, focusEngine = focusEngine, onQuickAction = onQuickAction)
                }
            }
        }
        if (showNetworkPanel) {
            NetworkInfoPanel(isOffline = isOffline, onClose = onDismissNetworkPanel)
        }
if (isSettings) {
            val (sA, sTY) = settingsAlpha
            SettingsOverlayContent(sA, sTY, activeSettingsCategory, { setActiveSettingsCategory(it) }, handleHomePress, devMode = developerMode, activeTheme = activeTheme, setActiveTheme = { setActiveTheme(it) }, triggerToast = triggerToast, activeAccentColor = activeAccentColor, setActiveAccent = { setActiveAccent(it) }, largerUI = largerUI, setLargerUI = { setLargerUI(it) }, searchEngine = searchEngine, setSearchEngine = { setSearchEngine(it) }, isPerfOverlayEnabled = isPerfOverlayEnabled, setIsPerfOverlayEnabled = { setIsPerfOverlayEnabled(it) }, isRemoteVisible = isRemoteVisible, setIsRemoteVisible = { setIsRemoteVisible(it) }, isFpsCounterEnabled = isFpsCounterEnabled, setIsFpsCounterEnabled = { setIsFpsCounterEnabled(it) }, isMemoryUsageEnabled = isMemoryUsageEnabled, setIsMemoryUsageEnabled = { setIsMemoryUsageEnabled(it) }, animationSpeedMultiplier = animationSpeedMultiplier, setAnimationSpeedMultiplier = { setAnimationSpeedMultiplier(it) }, onNavigatePerformanceCenter, clearHistory = { homeScope.launch { withContext(Dispatchers.IO) { com.aurora.data.DataService.history.clear() } }; triggerToast("Browsing history cleared") }, setBookmarks,
                benchmarkMode = benchmarkMode, onBenchmarkToggle = { handleBenchmarkToggle(it) },
                sessionManager = sessionManager, focusEngine = focusEngine
            )
        }
    }
}

@Composable
fun SettingsOverlayContent(
    sA: Float, sTY: Float,
    activeSettingsCategory: String, setActiveSettingsCategory: (String) -> Unit,
    handleHomePress: () -> Unit, devMode: Boolean,
    activeTheme: String, setActiveTheme: (String) -> Unit,
    triggerToast: (String) -> Unit,
    activeAccentColor: String, setActiveAccent: (String) -> Unit,
    largerUI: Boolean, setLargerUI: (Boolean) -> Unit,
    searchEngine: String, setSearchEngine: (String) -> Unit,
    isPerfOverlayEnabled: Boolean, setIsPerfOverlayEnabled: (Boolean) -> Unit,
    isRemoteVisible: Boolean, setIsRemoteVisible: (Boolean) -> Unit,
    isFpsCounterEnabled: Boolean, setIsFpsCounterEnabled: (Boolean) -> Unit,
    isMemoryUsageEnabled: Boolean, setIsMemoryUsageEnabled: (Boolean) -> Unit,
    animationSpeedMultiplier: Float, setAnimationSpeedMultiplier: (Float) -> Unit,
    onNavigatePerformanceCenter: () -> Unit,
    clearHistory: () -> Unit, setBookmarks: (List<Bookmark>) -> Unit,
    benchmarkMode: Boolean = false, onBenchmarkToggle: (Boolean) -> Unit = {},
sessionManager: SessionManager? = null,
    focusEngine: FocusEngine? = null
) {
    val persistScope = rememberCoroutineScope()
    val browserSettings = sessionManager?.browserSettings
    var enginePrefs by remember(browserSettings) { mutableStateOf(EnginePrefs(
        allowJavaScript = browserSettings?.allowJavaScript ?: true,
        allowCookies = browserSettings?.allowCookies ?: true,
        allowPopups = browserSettings?.allowPopups ?: false,
        allowLocation = browserSettings?.allowLocation ?: true,
        allowNotifications = browserSettings?.allowNotifications ?: true,
        allowThirdPartyCookies = browserSettings?.allowThirdPartyCookies ?: true,
        adBlockingEnabled = browserSettings?.adBlockingEnabled ?: false,
        textZoom = browserSettings?.textZoom ?: 100
    )) }
    val persist: (suspend () -> Unit) -> Unit = remember(persistScope) { { b -> persistScope.launch(Dispatchers.IO) { b() } } }
    val onTglJs = remember(browserSettings) { { val v = !enginePrefs.allowJavaScript; enginePrefs = enginePrefs.copy(allowJavaScript = v); persist { com.aurora.data.DataService.sessions.setAllowJavaScript(v) }; browserSettings?.allowJavaScript = v; triggerToast(if (v) "JavaScript Enabled" else "JavaScript Disabled") } }
    val onTglCk = remember(browserSettings) { { val v = !enginePrefs.allowCookies; enginePrefs = enginePrefs.copy(allowCookies = v); persist { com.aurora.data.DataService.sessions.setAllowCookies(v) }; browserSettings?.allowCookies = v; triggerToast(if (v) "Cookies Enabled (restart to apply)" else "Cookies Disabled (restart to apply)") } }
    val onTglPp = remember(browserSettings) { { val v = !enginePrefs.allowPopups; enginePrefs = enginePrefs.copy(allowPopups = v); persist { com.aurora.data.DataService.sessions.setAllowPopups(v) }; browserSettings?.allowPopups = v; triggerToast(if (v) "Pop-ups Allowed" else "Pop-ups Blocked") } }
    val onTglLc = remember(browserSettings) { { val v = !enginePrefs.allowLocation; enginePrefs = enginePrefs.copy(allowLocation = v); persist { com.aurora.data.DataService.sessions.setAllowLocation(v) }; browserSettings?.allowLocation = v; triggerToast(if (v) "Location Access Enabled" else "Location Access Disabled") } }
    val onTglNt = remember(browserSettings) { { val v = !enginePrefs.allowNotifications; enginePrefs = enginePrefs.copy(allowNotifications = v); persist { com.aurora.data.DataService.sessions.setAllowNotifications(v) }; browserSettings?.allowNotifications = v; triggerToast(if (v) "Notifications Enabled" else "Notifications Disabled") } }
    val onTgl3pc = remember(browserSettings) { { val v = !enginePrefs.allowThirdPartyCookies; enginePrefs = enginePrefs.copy(allowThirdPartyCookies = v); browserSettings?.allowThirdPartyCookies = v; triggerToast(if (v) "3rd-Party Cookies Allowed" else "3rd-Party Cookies Blocked") } }
    val onTglAdBlock = remember(browserSettings) { { val v = !enginePrefs.adBlockingEnabled; enginePrefs = enginePrefs.copy(adBlockingEnabled = v); browserSettings?.adBlockingEnabled = v; triggerToast(if (v) "Ad-Blocking Enabled" else "Ad-Blocking Disabled") } }
    val onZoomIn = remember(browserSettings) { { val v = (enginePrefs.textZoom + 10).coerceAtMost(200); enginePrefs = enginePrefs.copy(textZoom = v); browserSettings?.textZoom = v; sessionManager?.state?.value?.activeTab?.session?.setTextZoom(v); triggerToast("Text zoom: ${v}%") } }
    val onZoomOut = remember(browserSettings) { { val v = (enginePrefs.textZoom - 10).coerceAtLeast(50); enginePrefs = enginePrefs.copy(textZoom = v); browserSettings?.textZoom = v; sessionManager?.state?.value?.activeTab?.session?.setTextZoom(v); triggerToast("Text zoom: ${v}%") } }
    Box(Modifier.fillMaxSize().graphicsLayer { alpha = sA; translationY = sTY }.verticalScroll(rememberScrollState()).padding(24.dp)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, Modifier.size(24.dp), AuroraColors.auroraEmerald); Column { Text("AURORA CONTROL CENTER", color = AuroraColors.white, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text("Adjust parameters of your Living Glass environment", color = AuroraColors.white40, fontSize = 9.sp) } }
                FocusBinding(id = "settings_back", focusEngine = focusEngine!!, group = "settings_sidebar", order = 10, onClick = { handleHomePress() }) { isFocused -> Box(Modifier.background(if (isFocused) AuroraColors.auroraEmerald.copy(alpha = 0.2f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) AuroraColors.auroraEmerald else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("Back to Dashboard", color = AuroraColors.white, fontSize = 10.sp) } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(0.33f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    buildList { add("Appearance"); add("Privacy"); add("Search Engine"); add("Performance"); if (devMode) add("Developer"); add("About") }.forEach { cat -> FocusBinding(id = "settings_cat_$cat", focusEngine = focusEngine!!, group = "settings_sidebar", order = 0, onClick = { setActiveSettingsCategory(cat) }) { isFocused -> Box(Modifier.fillMaxWidth().background(if (isFocused || activeSettingsCategory == cat) AuroraColors.auroraEmerald.copy(alpha = 0.15f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused || activeSettingsCategory == cat) AuroraColors.auroraEmerald else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(cat, color = if (isFocused || activeSettingsCategory == cat) AuroraColors.auroraEmerald else AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold); Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), AuroraColors.white30) } } } }
                }
                Column(Modifier.weight(0.67f).background(AuroraColors.neutral900.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(24.dp)) {
                    Text("$activeSettingsCategory OPTIONS", color = AuroraColors.white, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(Modifier.height(16.dp))
                    when (activeSettingsCategory) {
                        "Appearance" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Select Theme Palette", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Medium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Aurora Dark", "Midnight Blue", "Graphite Slate").forEach { t -> val ita = activeTheme == t; Box(Modifier.background(if (ita) AuroraColors.auroraEmerald.copy(alpha = 0.2f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (ita) AuroraColors.auroraEmerald else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp).clickable { setActiveTheme(t); triggerToast("Theme: $t") }) { Text(t, color = if (ita) AuroraColors.auroraEmerald else AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } } }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Select Brand Accent Color", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Medium); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf("Blue" to "#4DA3FF", "Emerald" to "#34D399", "Purple" to "#A78BFA", "Orange" to "#F97316").forEach { (n, c) -> val iac = activeAccentColor == c; Box(Modifier.size(32.dp).background(Color(android.graphics.Color.parseColor(c)), CircleShape).border(1.dp, AuroraColors.white20, CircleShape).clickable { setActiveAccent(c); triggerToast("Accent: $n") }, contentAlignment = Alignment.Center) { if (iac) Icon(Icons.Default.Check, null, Modifier.size(16.dp), Color.Black) } } } }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("UI Scaling preferences", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Medium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (largerUI) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (largerUI) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)).clickable { setLargerUI(!largerUI) }, contentAlignment = Alignment.Center) { if (largerUI) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Text("Enable Larger UI mode (+4sp comfort boost)", color = AuroraColors.white80, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                            }
                        }
                        "Privacy" -> { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Aurora manages all database keys with AES-256 local envelopes. Guest sessions never write sync data.", color = AuroraColors.white50, fontSize = 10.sp)
                            FocusBinding(id = "settings_js", focusEngine = focusEngine!!, group = "settings_panel", order = 7, onClick = { onTglJs() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.allowJavaScript) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.allowJavaScript) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.allowJavaScript) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Enable JavaScript", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Web interactivity and dynamic content", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_cookies", focusEngine = focusEngine!!, group = "settings_panel", order = 8, onClick = { onTglCk() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.allowCookies) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.allowCookies) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.allowCookies) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Accept Cookies", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Session and persistent storage (applies on restart)", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_popups", focusEngine = focusEngine!!, group = "settings_panel", order = 9, onClick = { onTglPp() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (!enginePrefs.allowPopups) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (!enginePrefs.allowPopups) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (!enginePrefs.allowPopups) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Block Pop-ups", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Prevent sites from opening new windows", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_location", focusEngine = focusEngine!!, group = "settings_panel", order = 10, onClick = { onTglLc() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.allowLocation) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.allowLocation) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.allowLocation) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Location Access", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Allow sites to request device location", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_notifications", focusEngine = focusEngine!!, group = "settings_panel", order = 11, onClick = { onTglNt() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.allowNotifications) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.allowNotifications) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.allowNotifications) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Notifications", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Allow sites to send push notifications", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_3pc", focusEngine = focusEngine!!, group = "settings_panel", order = 12, onClick = { onTgl3pc() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.allowThirdPartyCookies) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.allowThirdPartyCookies) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.allowThirdPartyCookies) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Third-Party Cookies", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Embedded sign-in widgets and oAuth iframes", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            FocusBinding(id = "settings_adblock", focusEngine = focusEngine!!, group = "settings_panel", order = 13, onClick = { onTglAdBlock() }) { isFocused ->Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (enginePrefs.adBlockingEnabled) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (enginePrefs.adBlockingEnabled) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { if (enginePrefs.adBlockingEnabled) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black) }; Column { Text("Ad-Blocking (Experimental)", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("Blocks known ad & tracking domains", color = AuroraColors.white40, fontSize = 7.sp) } } }
                            Spacer(Modifier.height(4.dp))
                            FocusBinding(id = "settings_flush", focusEngine = focusEngine!!, group = "settings_panel", order = 5, onClick = { clearHistory() }) { isFocused ->Box(Modifier.fillMaxWidth().background(if (isFocused) AuroraColors.auroraRed.copy(alpha = 0.15f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) AuroraColors.auroraRed else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Text("Clear Browsing History", color = AuroraColors.auroraRed, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                            FocusBinding(id = "settings_reset_bookmarks", focusEngine = focusEngine!!, group = "settings_panel", order = 6, onClick = { setBookmarks(listOf(Bookmark("b-1", "https://youtube.com", "YouTube", "Streaming"))); triggerToast("Bookmarks Reset") }) { isFocused ->Box(Modifier.fillMaxWidth().background(if (isFocused) AuroraColors.auroraEmerald.copy(alpha = 0.15f) else AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, if (isFocused) AuroraColors.auroraEmerald else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Text("Reset Bookmarks Metadata", color = AuroraColors.white80, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                        } }
                        "Search Engine" -> { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Configure Default Search Protocol", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Medium); SearchEngineRegistry.all.map { it.name }.forEach { e -> val ie = searchEngine == e; Row(Modifier.fillMaxWidth().background(AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp).clickable { setSearchEngine(e); val engineId = SearchEngineRegistry.all.find { it.name == e }?.id ?: e.lowercase(); persist { com.aurora.data.DataService.sessions.setDefaultSearchEngine(engineId) }; triggerToast("Search: $e") }, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(16.dp).background(if (ie) AuroraColors.auroraEmerald else Color.Transparent, CircleShape).border(2.dp, if (ie) AuroraColors.auroraEmerald else AuroraColors.white30, CircleShape), contentAlignment = Alignment.Center) { if (ie) Icon(Icons.Default.Check, null, Modifier.size(10.dp), Color.Black) }; Text("$e Engine", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } } }
                        "Performance" -> { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Your active television utilizes TV Class-2 memory budget allocations.", color = AuroraColors.white50, fontSize = 10.sp); Box(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Renderer Compressor:", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace); Text("ACTIVE", color = AuroraColors.auroraEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) } }; Box(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tab Sleep Threshold:", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace); Text("After 30 minutes", color = AuroraColors.white70, fontSize = 10.sp, fontFamily = FontFamily.Monospace) } }; Box(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Text Zoom:", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace); Text("${enginePrefs.textZoom}%", color = AuroraColors.auroraBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) } } } }
                        "Developer" -> { DeveloperSettingsContent(devMode, isPerfOverlayEnabled, isRemoteVisible, isFpsCounterEnabled, isMemoryUsageEnabled, animationSpeedMultiplier, setIsPerfOverlayEnabled, setIsRemoteVisible, setIsFpsCounterEnabled, setIsMemoryUsageEnabled, setAnimationSpeedMultiplier, onNavigatePerformanceCenter, benchmarkMode, onBenchmarkToggle, context = LocalContext.current, focusEngine = focusEngine) }
                        "About" -> { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("Application:", color = AuroraColors.auroraBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold); Text("Aurora Premium Browser", color = AuroraColors.white80, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }; Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("Version:", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold); Text("v2.0.0 Stable Build", color = AuroraColors.white80, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }; Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("Compositor Engine:", color = AuroraColors.white, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold); Text(sessionManager?.engineName ?: AuroraEngineConfig.displayName, color = AuroraColors.white80, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }; Text("Designed & engineered by Aurora Labs. Living Glass TV v2.0.", color = AuroraColors.white60, fontSize = 10.sp, fontFamily = FontFamily.Monospace) } }
                    }
                    Spacer(Modifier.weight(1f)); Text("State: Local Sync Engaged", color = AuroraColors.white30, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun DeveloperSettingsContent(
    devMode: Boolean, isPerfOverlayEnabled: Boolean, isRemoteVisible: Boolean,
    isFpsCounterEnabled: Boolean, isMemoryUsageEnabled: Boolean, animationSpeedMultiplier: Float,
    setIsPerfOverlayEnabled: (Boolean) -> Unit, setIsRemoteVisible: (Boolean) -> Unit,
    setIsFpsCounterEnabled: (Boolean) -> Unit, setIsMemoryUsageEnabled: (Boolean) -> Unit,
    setAnimationSpeedMultiplier: (Float) -> Unit, onNavigatePerformanceCenter: () -> Unit,
    benchmarkMode: Boolean = false, onBenchmarkToggle: (Boolean) -> Unit = {},
    context: android.content.Context? = null,
    focusEngine: FocusEngine? = null
) {
    if (devMode) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(AuroraColors.auroraBlue.copy(alpha = 0.15f), Color.Transparent)), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp)).padding(12.dp)) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, null, Modifier.size(14.dp), AuroraColors.auroraBlue)
                    Text("Kernel Diagnostic Suite", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("Real-time television telemetry pipeline controls", color = AuroraColors.white50, fontSize = 9.sp)
            }
        }
        Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp).clickable { setIsPerfOverlayEnabled(!isPerfOverlayEnabled) }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(if (isPerfOverlayEnabled) AuroraColors.auroraBlue else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (isPerfOverlayEnabled) AuroraColors.auroraBlue else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                if (isPerfOverlayEnabled) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black)
            }
            Column {
                Text("Enable Diagnostics HUD Overlay", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Float FPS, memory, render times on top right", color = AuroraColors.white40, fontSize = 8.sp)
            }
        }
        Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp).clickable { setIsRemoteVisible(!isRemoteVisible) }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(if (isRemoteVisible) AuroraColors.auroraBlue else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (isRemoteVisible) AuroraColors.auroraBlue else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                if (isRemoteVisible) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black)
            }
            Column {
                Text("Show Virtual Simulation Remote Control (F1 shortcut)", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Keep on screen for mouse navigation inputs", color = AuroraColors.white40, fontSize = 8.sp)
            }
        }
        if (isPerfOverlayEnabled) Column(Modifier.padding(start = 24.dp).border(1.dp, AuroraColors.white10, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { setIsFpsCounterEnabled(!isFpsCounterEnabled) }) {
                Box(Modifier.size(14.dp).background(if (isFpsCounterEnabled) AuroraColors.auroraBlue else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (isFpsCounterEnabled) AuroraColors.auroraBlue else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    if (isFpsCounterEnabled) Icon(Icons.Default.Check, null, Modifier.size(10.dp), Color.Black)
                }
                Text("Render Frame Counter (FPS)", color = AuroraColors.white80, fontSize = 9.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { setIsMemoryUsageEnabled(!isMemoryUsageEnabled) }) {
                Box(Modifier.size(14.dp).background(if (isMemoryUsageEnabled) AuroraColors.auroraBlue else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (isMemoryUsageEnabled) AuroraColors.auroraBlue else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    if (isMemoryUsageEnabled) Icon(Icons.Default.Check, null, Modifier.size(10.dp), Color.Black)
                }
                Text("Resident RAM Allocations", color = AuroraColors.white80, fontSize = 9.sp)
            }
        }
        Column(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gecko CSS Animation Scale", color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("${animationSpeedMultiplier}x", color = AuroraColors.auroraBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).background(AuroraColors.neutral800, RoundedCornerShape(2.dp)).drawWithContent { drawContent(); drawRect(AuroraColors.auroraBlue.copy(alpha = 0.5f), size = Size(size.width * (animationSpeedMultiplier / 2f), size.height)) })
            Text("Set to 0x to clear composite layout processing threads", color = AuroraColors.white30, fontSize = 7.sp)
        }
        Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(8.dp).clickable { onBenchmarkToggle(!benchmarkMode) }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(if (benchmarkMode) AuroraColors.auroraEmerald else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (benchmarkMode) AuroraColors.auroraEmerald else AuroraColors.white30, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                if (benchmarkMode) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black)
            }
            Column {
                Text("Benchmark Mode (Requires Restart)", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Disables Spectre mitigations, tracking, safe browsing. Speedometer/JetStream boost.", color = AuroraColors.white40, fontSize = 8.sp)
            }
        }
        context?.let { ctx ->
            val prefs = remember(ctx) { com.aurora.data.preferences.SessionPreferences(ctx) }
            var activePath by remember { mutableStateOf(prefs.activeDownloadPath()) }
            var volumes by remember { mutableStateOf(com.aurora.data.storage.StorageScanner.getAvailableStorageVolumes(ctx)) }
            Column(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Download Storage Volume", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                volumes.forEach { vol ->
                    val selected = activePath == vol.downloadPath.absolutePath
                    var rowFocused by remember(vol.downloadPath.absolutePath) { mutableStateOf(false) }
                    Row(Modifier.fillMaxWidth().background(if (selected) AuroraColors.auroraEmerald.copy(alpha = 0.12f) else AuroraColors.neutral950, RoundedCornerShape(8.dp)).border(1.dp, if (selected) AuroraColors.auroraEmerald.copy(alpha = 0.3f) else AuroraColors.white5, RoundedCornerShape(8.dp)).padding(10.dp).clickable { prefs.setDownloadPath(vol.downloadPath.absolutePath); activePath = vol.downloadPath.absolutePath }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).background(if (selected) AuroraColors.auroraEmerald else Color.Transparent, CircleShape).border(1.5.dp, if (selected) AuroraColors.auroraEmerald else AuroraColors.white30, CircleShape), contentAlignment = Alignment.Center) {
                            if (selected) Icon(Icons.Default.Check, null, Modifier.size(9.dp), Color.Black)
                        }
                        Column {
                            Text(vol.displayName, color = if (selected) AuroraColors.auroraEmerald else AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${com.aurora.data.storage.StorageScanner.formatSize(vol.availableBytes)} free${if (vol.isRemovable) " • Removable" else ""}", color = AuroraColors.white40, fontSize = 7.sp)
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(vertical = 10.dp).clickable { onNavigatePerformanceCenter() }, contentAlignment = Alignment.Center) {
            Text("Launch Full Telemetry Performance Center Screen", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BrowserModeContent(
    sessionManager: SessionManager,
    sessionState: com.aurora.ui.viewmodel.SessionManagerState,
    activeTab: TabSession?,
    activeTabId: String,
    toolbarVisible: Boolean,
    recentlyClosed: List<HistoryEntry>,
    bookmarks: List<Bookmark>,
    omniboxInput: String,
    isOmniboxFocused: Boolean,
    remoteClicked: Boolean,
    context: Context,
    triggerToast: (String) -> Unit,
    handleHomePress: () -> Unit,
    handleCreateNewTab: (Boolean) -> Unit,
    handleCloseTab: (String) -> Unit,
    handleWebNavigation: (String) -> Unit,
    handleReopenClosed: (HistoryEntry) -> Unit,
    handleToggleBookmark: () -> Unit,
    switchSession: (String) -> Unit,
    setCurrentScreen: (Screen) -> Unit,
    setOmniboxInput: (String) -> Unit,
    setIsOmniboxFocused: (Boolean) -> Unit,
    setIsKeyboardOpen: (Boolean) -> Unit,
    onCursorMove: (Float, Float) -> Unit = { _, _ -> },
    scrollDelta: Pair<Float, Float> = Pair(0f, 0f),
    scrollTick: Long = 0L,
    clickCoordsProvider: () -> Pair<Float, Float> = { Pair(0f, 0f) },
    onDpadPress: (String) -> Unit = {},
    isTabWorkspaceOpen: Boolean = false,
    onTabWorkspaceOpenChange: (Boolean) -> Unit = {},
    tabWorkspaceVisible: Boolean = false,
    onBridgeSet: (InputBridge) -> Unit = {},
    onToolbarHeightChanged: (Float) -> Unit = {},
    onRefreshDownloads: suspend () -> Unit = {},
    downloadManager: com.aurora.browser.service.DownloadManager? = null
) {
    Log.d("AuroraBrowser", "BrowserModeContent entered composition activeTab=${activeTab?.id} geckoSession=${System.identityHashCode(activeTab?.session)}")
    DisposableEffect(Unit) {
        onDispose {
            Log.d("AuroraBrowser", "BrowserModeContent left composition")
        }
    }
    val browserBs = activeTab?.let { it.controller.state }?.collectAsState()?.value ?: BrowserState()
                LaunchedEffect(browserBs.downloadUrl) {
        val dlUrl = browserBs.downloadUrl ?: return@LaunchedEffect
        val fileName = browserBs.downloadFileName ?: "download"
        val mimeType = browserBs.downloadMimeType ?: ""
        val savedPath = browserBs.savedDownloadFilePath
        val dl = downloadManager
        if (dl != null) {
            if (savedPath != null && File(savedPath).exists()) {
                dl.registerCompletedDownload(dlUrl, fileName, mimeType, savedPath)
                triggerToast("Download complete: $fileName")
            } else {
                dl.startDownload(dlUrl, fileName, mimeType)
                triggerToast("Download started: $fileName")
            }
            onRefreshDownloads()
        }
        activeTab?.controller?.clearDownloadRequest()
    }
    val browserUiState = BrowserMapper.toUiState(
        tabs = sessionState.tabs, activeTabId = activeTabId, browserState = browserBs,
        isBookmarked = activeTab?.let { tab -> bookmarks.any { it.url == tab.controller.state.value.currentUrl } } ?: false,
        toolbarVisible = toolbarVisible, recentlyClosed = recentlyClosed,
        isDesktopMode = activeTab?.controller?.isDesktopMode() ?: false,
        onToggleDesktop = { activeTab?.controller?.setDesktopMode(!(activeTab?.controller?.isDesktopMode() ?: false)) }
    )
    val session = activeTab?.session
    BrowserScreen(
        uiState = browserUiState, session = session,
        onBackPress = { activeTab?.controller?.goBack() },
        onForwardPress = { activeTab?.controller?.goForward() },
        onReload = { activeTab?.controller?.reload() },
        onStop = { activeTab?.controller?.stop() },
        onHomePress = handleHomePress,
        onOpenLibrary = { setCurrentScreen(Screen.Library) },
        onToggleBookmark = { handleToggleBookmark() },
        onNewTab = { isPrivate -> handleCreateNewTab(isPrivate) },
        onCloseTab = { id -> handleCloseTab(id) },
        onTabSelect = { id -> switchSession(id) },
        onNavigate = { url -> handleWebNavigation(url) },
        onReopenClosed = { url -> val entry = recentlyClosed.find { it.url == url }; if (entry != null) handleReopenClosed(entry) },
        onRetry = { activeTab?.controller?.reload() },
        remoteClicked = remoteClicked,
        editQuery = omniboxInput, isEditing = isOmniboxFocused,
        onUrlEdit = { url -> setOmniboxInput(url); setIsOmniboxFocused(true); setIsKeyboardOpen(true) },
        onCursorMove = onCursorMove,
        scrollDelta = scrollDelta, scrollTick = scrollTick,
        clickCoordsProvider = clickCoordsProvider,
        onDpadPress = onDpadPress,
        onBridgeSet = onBridgeSet,
        isTabWorkspaceOpen = isTabWorkspaceOpen,
        onTabWorkspaceOpenChange = onTabWorkspaceOpenChange,
        tabWorkspaceVisible = tabWorkspaceVisible,
        onToolbarHeightChanged = onToolbarHeightChanged
    )
}

@Composable
fun SpeedDialSection(
    focusEngine: FocusEngine,
    onNavigate: (String) -> Unit
) {
    var sites by remember { mutableStateOf(emptyList<MockData.PopularSite>()) }
    LaunchedEffect(Unit) {
        val assets = withContext(Dispatchers.IO) {
            com.aurora.data.DataService.browserAssets.getRecentAssets(limit = 8, withFavicon = false, withThumbnail = false)
        }
        sites = assets.take(6).map { a ->
            MockData.PopularSite(name = a.title.ifEmpty { a.domain }, url = a.url, icon = "BookOpen", color = a.dominantColor.toLong() and 0xFFFFFFFFL)
        }
    }
    val displaySites = sites.ifEmpty { MockData.popularSites }
    val s3 = rememberStaggerAlpha(StaggerDelay.Stagger3)
    Column(Modifier.graphicsLayer { alpha = s3 }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraPurple, CircleShape)); Text("SPEED DIAL FAVORITES", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("SPEED DIAL • REFLEXIVE METADATA", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            displaySites.forEachIndexed { index, site ->
                FocusBinding(id = "fav_$index", focusEngine = focusEngine, group = "favorites", order = index, onClick = { onNavigate(site.url) }) { isFocused -> val sc = Color(site.color)
                Column(Modifier.weight(1f).height(120.dp).cardLift(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).border(1.dp, if (isFocused) AuroraColors.auroraPurple.copy(alpha = 0.6f) else AuroraColors.white5, RoundedCornerShape(24.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(40.dp).background(sc.copy(alpha = 0.08f), RoundedCornerShape(16.dp)).border(1.dp, sc.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).graphicsLayer { if (isFocused) { scaleX = 1.1f; scaleY = 1.1f } }, contentAlignment = Alignment.Center) { val siteLogo = MockData.logoResFor(site); if (siteLogo != 0) Image(painter = painterResource(siteLogo), contentDescription = null, modifier = Modifier.size(26.dp)) else getIconComponent(site.icon, sc, 22.dp) }
                    Spacer(Modifier.height(8.dp)); Text(site.name, color = if (isFocused) AuroraColors.white else AuroraColors.white70, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.width(24.dp).height(3.dp).background(sc.copy(alpha = if (isFocused) 1f else 0.4f), RoundedCornerShape(4.dp)))
                }
            }
            }
        }
    }
}

@Composable
fun StreamingHubSection(
    focusEngine: FocusEngine,
    onNavigate: (String) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraPurple, CircleShape)); Text("STREAMING & MOVIE HUB", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("GLOBAL PICKS • ${MockData.streamingSites.size} SITES", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp));
        MockData.featuredStreamingSites.chunked(MockData.STREAMING_COLUMNS).forEachIndexed { fRowIndex, fRowSites ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fRowSites.forEachIndexed { colIndex, site ->
                    FocusBinding(id = "stream_f_${fRowIndex}_$colIndex", focusEngine = focusEngine, group = "streaming_featured_r$fRowIndex", order = colIndex, onClick = { onNavigate(site.url) }) { isFocused -> val sc = Color(site.color)
                    Column(Modifier.weight(1f).height(120.dp).cardLift(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).border(1.dp, if (isFocused) AuroraColors.auroraPurple.copy(alpha = 0.6f) else AuroraColors.white5, RoundedCornerShape(24.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(40.dp).background(sc.copy(alpha = 0.08f), RoundedCornerShape(16.dp)).border(1.dp, sc.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).graphicsLayer { if (isFocused) { scaleX = 1.1f; scaleY = 1.1f } }, contentAlignment = Alignment.Center) { getIconComponent(site.icon, sc, 22.dp) }
                        Spacer(Modifier.height(8.dp)); Text(site.name, color = if (isFocused) AuroraColors.white else AuroraColors.white70, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Box(Modifier.width(24.dp).height(3.dp).background(sc.copy(alpha = if (isFocused) 1f else 0.4f), RoundedCornerShape(4.dp)))
                    }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(0.dp));
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraEmerald, CircleShape)); Text("MOST POPULAR WORLDWIDE", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("SCROLL WITH D-PAD • MOVIES • DRAMA • ANIME • LIVE TV", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp));
        MockData.streamingSites.chunked(6).forEachIndexed { rowIndex, rowSites ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowSites.forEachIndexed { colIndex, site ->
                    FocusBinding(id = "stream_${rowIndex}_$colIndex", focusEngine = focusEngine, group = MockData.streamingRowGroupName(rowIndex), order = colIndex, onClick = { onNavigate(site.url) }) { isFocused -> val sc = Color(site.color)
                    Column(Modifier.weight(1f).height(120.dp).cardLift(isFocused).lightSweep(isFocused).background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp)).border(1.dp, if (isFocused) AuroraColors.auroraEmerald.copy(alpha = 0.6f) else AuroraColors.white5, RoundedCornerShape(24.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(40.dp).background(sc.copy(alpha = 0.08f), RoundedCornerShape(16.dp)).border(1.dp, sc.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).graphicsLayer { if (isFocused) { scaleX = 1.1f; scaleY = 1.1f } }, contentAlignment = Alignment.Center) { getIconComponent(site.icon, sc, 22.dp) }
                        Spacer(Modifier.height(8.dp)); Text(site.name, color = if (isFocused) AuroraColors.white else AuroraColors.white70, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Box(Modifier.width(24.dp).height(3.dp).background(sc.copy(alpha = if (isFocused) 1f else 0.4f), RoundedCornerShape(4.dp)))
                    }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ContinueBrowsingSection(
    focusEngine: FocusEngine,
    onNavigate: (String) -> Unit
) {
    var items by remember { mutableStateOf(emptyList<ContinueBrowsingUiModel>()) }
    LaunchedEffect(Unit) {
        val assets = withContext(Dispatchers.IO) {
            com.aurora.data.DataService.browserAssets.getRecentAssets(limit = 8)
        }
        items = assets.map { a ->
            ContinueBrowsingUiModel(
                id = a.url, name = a.title.ifEmpty { a.domain }, title = a.title.ifEmpty { a.domain },
                timeText = formatRelativeTime(a.lastVisited), url = a.url, domain = a.domain,
                accentColor = Color(a.dominantColor), faviconBitmap = a.favicon, thumbnail = a.thumbnail
            )
        }
    }
    val displayItems = items.ifEmpty {
        listOf(
            ContinueBrowsingUiModel("cb-1", "YouTube", "YouTube Feed", "Active - 2 hours ago", "https://youtube.com", "youtube.com", Color(0xFFFF0000)),
            ContinueBrowsingUiModel("cb-2", "GitHub", "Aurora OS Repo", "Last visited 5h ago", "https://github.com", "github.com", Color(0xFFFFFFFF)),
            ContinueBrowsingUiModel("cb-3", "Wikipedia", "Living Glass Spec", "Active Session", "https://wikipedia.org/wiki/living-glass", "wikipedia.org", Color(0xFF4285F4)),
            ContinueBrowsingUiModel("cb-4", "Reddit", "r/AndroidTV Devs", "Last visited yesterday", "https://reddit.com", "reddit.com", Color(0xFFFF4500))
        )
    }
    val s2 = rememberStaggerAlpha(StaggerDelay.Stagger2)
    Column(Modifier.graphicsLayer { alpha = s2 }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraBlue, CircleShape)); Text("CONTINUE BROWSING", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("RECENT SESSIONS • CARDS", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            displayItems.take(4).forEachIndexed { index, item ->
                FocusBinding(id = "continue_$index", focusEngine = focusEngine, group = "continue_browsing", order = index, onClick = { onNavigate(item.url) }) { isFocused ->
                Box(
                    Modifier
                        .weight(1f)
                        .cardLift(isFocused)
                        .lightSweep(isFocused)
                        .background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                        .border(1.dp, if (isFocused) AuroraColors.auroraBlue else AuroraColors.white5, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(28.dp).background(item.accentColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(item.name.take(1).uppercase(), color = item.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(Modifier.background(AuroraColors.white5, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(item.timeText, color = AuroraColors.white40, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text(item.title, color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.domain, color = AuroraColors.white40, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            }
        }
    }
}

@Composable
fun TrendingSection(
    focusEngine: FocusEngine,
    onNavigate: (String) -> Unit, downloads: List<Download>,
    setCurrentScreen: (Screen) -> Unit, triggerToast: (String) -> Unit
) {
    val s4 = rememberStaggerAlpha(StaggerDelay.Stagger4)
    Column(Modifier.graphicsLayer { alpha = s4 }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(AuroraColors.auroraAmber, CircleShape)); Text("TRENDING TODAY", color = AuroraColors.white45, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }; Text("CONTENT DISCOVERY • D-PAD INTERACTIVE", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace) }
        Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            com.aurora.ui.screens.TRENDING_ITEMS.forEachIndexed { index, item ->
                FocusBinding(id = "trending_$index", focusEngine = focusEngine, group = "trending", order = index, onClick = { onNavigate("https://google.com/search?q=${java.net.URLEncoder.encode(item.title, "UTF-8")}") }) { isFocused ->
                Box(
                    Modifier
                        .weight(1f)
                        .cardLift(isFocused)
                        .lightSweep(isFocused)
                        .background(if (isFocused) AuroraColors.neutral900 else AuroraColors.neutral950.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                        .border(1.dp, if (isFocused) AuroraColors.auroraAmber else AuroraColors.white5, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.background(AuroraColors.auroraAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(item.category, color = AuroraColors.auroraAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(item.domain, color = AuroraColors.white30, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(item.title, color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    bookmarks: List<Bookmark>, setBookmarks: (List<Bookmark>) -> Unit,
    downloads: List<Download>, setDownloads: (List<Download>) -> Unit,
    onNavigate: (String) -> Unit, onOpenViewer: (Download) -> Unit,
    librarySearchQuery: String, setLibrarySearchQuery: (String) -> Unit,
    libraryActiveFilter: String, setLibraryActiveFilter: (String) -> Unit,
    onHome: () -> Unit, triggerToast: (String) -> Unit, addLog: (String, String, String) -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFF0E0F12)).padding(24.dp)) {
        Row(Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Book, null, Modifier.size(24.dp), AuroraColors.auroraPurple); Column { Text("AURORA UNIFIED LIBRARY", color = AuroraColors.white, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Bookmarks, Downloads, Reading List, and History", color = AuroraColors.white40, fontSize = 9.sp) } }; Box(Modifier.background(AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { onHome() }) { Text("Back to Dashboard", color = AuroraColors.white, fontSize = 11.sp) } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Videos", "PDFs", "Images", "Articles").forEach { f -> val ia = libraryActiveFilter == f; Box(Modifier.background(if (ia) AuroraColors.auroraBlue.copy(alpha = 0.15f) else AuroraColors.neutral900, RoundedCornerShape(8.dp)).border(1.dp, if (ia) AuroraColors.auroraBlue else AuroraColors.white5, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { setLibraryActiveFilter(f) }) { Text(f, color = if (ia) AuroraColors.auroraBlue else AuroraColors.white50, fontSize = 11.sp, fontWeight = FontWeight.Bold) } } }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Box(Modifier.fillMaxWidth().background(AuroraColors.neutral900, RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, Modifier.size(14.dp), AuroraColors.white30); Text(if (librarySearchQuery.isEmpty()) "Search Bookmarks or History..." else librarySearchQuery, color = if (librarySearchQuery.isEmpty()) AuroraColors.white40 else AuroraColors.white, fontSize = 11.sp) } } }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            if (libraryActiveFilter == "All" || libraryActiveFilter == "Articles") {
                Column { Text("Bookmarks", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { bookmarks.filter { it.title.lowercase().contains(librarySearchQuery.lowercase()) }.forEach { b -> Box(Modifier.weight(1f).height(96.dp).background(AuroraColors.neutral900.copy(alpha = 0.6f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).clickable { onNavigate(b.url) }.padding(12.dp)) { Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.background(AuroraColors.neutral800, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(b.category, color = AuroraColors.white50, fontSize = 7.sp) }; Icon(Icons.Default.Delete, null, Modifier.size(12.dp).clickable { setBookmarks(bookmarks.filter { it.id != b.id }); triggerToast("Removed Bookmark") }, AuroraColors.white30) }; Column { Text(b.title, color = AuroraColors.white90, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(b.url, color = AuroraColors.white40, fontSize = 8.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis) } } } } } }
                Column { Text("Reading List (Offline Available)", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(top = 16.dp)); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { Box(Modifier.weight(1f).padding(4.dp).background(AuroraColors.neutral900.copy(alpha = 0.6f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).clickable { onNavigate("https://wikipedia.org/wiki/living-glass") }.padding(16.dp)) { Column { Text("Design Spec", color = AuroraColors.auroraPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text("Living Glass Design — Wikipedia", color = AuroraColors.white90, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Offline Available • 6 min read", color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace) } }; Box(Modifier.weight(1f).padding(4.dp).background(AuroraColors.neutral900.copy(alpha = 0.6f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).clickable { onNavigate("https://google.com/search?q=performance+architecture") }.padding(16.dp)) { Column { Text("Performance Spec", color = AuroraColors.auroraPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text("Low-Memory Solutions — Wikipedia", color = AuroraColors.white90, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Offline Available • 8 min read", color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace) } } } }
            }
            if (libraryActiveFilter == "All" || libraryActiveFilter == "Videos" || libraryActiveFilter == "PDFs" || libraryActiveFilter == "Images") {
                Column { Text("Downloaded Files Sandbox", color = AuroraColors.white40, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(top = 16.dp)); Spacer(Modifier.height(8.dp)); if (downloads.isEmpty()) Text("No downloads yet. Try downloading files inside GitHub or Drive simulator.", color = AuroraColors.white30, fontSize = 10.sp) else { downloads.filter { d -> when (libraryActiveFilter) { "Videos" -> d.mimeType == "video/mp4"; "PDFs" -> d.mimeType == "application/pdf"; "Images" -> d.mimeType == "image/png"; else -> true } }.forEach { d -> val isActive = d.status != DownloadStatus.Completed && d.status != DownloadStatus.Failed; Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).background(AuroraColors.neutral900.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).border(1.dp, if (isActive) AuroraColors.auroraBlue.copy(alpha = 0.3f) else AuroraColors.white5, RoundedCornerShape(12.dp)).padding(12.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(d.fileName, color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(d.mimeType, color = AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace); Text("•", color = AuroraColors.white20, fontSize = 9.sp); if (isActive && d.speed != null) { Text(d.speed, color = AuroraColors.auroraEmerald, fontSize = 9.sp, fontFamily = FontFamily.Monospace); Text("•", color = AuroraColors.white20, fontSize = 9.sp) }; Text(if (isActive) "${d.progress}%" else d.totalSize, color = if (isActive) AuroraColors.auroraBlue else AuroraColors.white40, fontSize = 9.sp, fontFamily = FontFamily.Monospace) } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (d.status == DownloadStatus.Completed) Box(Modifier.background(AuroraColors.auroraBlue, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { onOpenViewer(d) }) { Text("Open Built-In", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold) } else if (d.status == DownloadStatus.Failed) Box(Modifier.background(AuroraColors.auroraRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).border(1.dp, AuroraColors.auroraRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("FAILED", color = AuroraColors.auroraRed, fontSize = 9.sp, fontWeight = FontWeight.Bold) } else Box(Modifier.background(AuroraColors.auroraBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("${d.progress}%", color = AuroraColors.auroraBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold) }; Icon(Icons.Default.Delete, null, Modifier.size(14.dp).clickable { setDownloads(downloads.filter { it.id != d.id }) }, AuroraColors.white30) } }; if (isActive) { Spacer(Modifier.height(8.dp)); Box(Modifier.fillMaxWidth().height(4.dp).background(AuroraColors.white5, RoundedCornerShape(2.dp))) { Box(Modifier.fillMaxWidth((d.progress / 100f).coerceIn(0.01f, 1f)).height(4.dp).background(AuroraColors.auroraBlue, RoundedCornerShape(2.dp))) } } } } } }
            }
        }
    }
}
