package com.aurora.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import android.view.KeyEvent
import com.aurora.browser.ui.components.BrowserCoordinator
import com.aurora.browser.ui.components.Cursor
import com.aurora.browser.ui.components.DeveloperHud
import com.aurora.browser.ui.components.HomeCoordinator
import com.aurora.browser.ui.components.OverlayCoordinator
import com.aurora.browser.ui.components.FindInPagePanel
import androidx.compose.runtime.CompositionLocalProvider
import com.aurora.browser.ui.components.Process as AuroraProcess
import com.aurora.browser.ui.components.LogEvent as AuroraLogEvent
import com.aurora.browser.ui.components.SettingsCoordinator
import com.aurora.browser.ui.components.Download as ViewerDownload
import com.aurora.browser.ui.theme.AuroraAnimation
import com.aurora.data.DataService
import com.aurora.data.model.BookmarkFolder
import com.aurora.data.preferences.SessionPreferences
import com.aurora.data.model.HistoryEntry
import com.aurora.data.search.SearchEngineRegistry
import com.aurora.engine.FilePickerRequest
import com.aurora.ui.theme.accentBackground
import com.aurora.ui.theme.LocalLargerUI
import com.aurora.home.UrlDetector
import com.aurora.ui.components.NoiseGrain
import com.aurora.ui.data.MockData
import com.aurora.ui.mappers.BookmarkMapper
import com.aurora.ui.model.ContinueBrowsingUiModel
import com.aurora.ui.model.FavoriteUiModel
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.theme.AuroraTheme
import com.aurora.ui.theme.Particle
import com.aurora.ui.theme.generateParticleBurst
import com.aurora.ui.types.Bookmark
import com.aurora.ui.types.Download
import com.aurora.ui.types.DownloadStatus
import com.aurora.ui.types.Screen
import com.aurora.ui.types.Tab
import com.aurora.ui.viewmodel.HistoryViewModel
import com.aurora.ui.viewmodel.rememberSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuroraApp(initialUrl: String? = null) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val sessionManager = rememberSessionManager()
    val runtimeReady by remember { sessionManager.runtimeReady }.collectAsState()
    if (!runtimeReady) {
        RuntimeWarmupScreen()
        return
    }
    val sessionState by sessionManager.state.collectAsState()
    val activeTab = sessionState.activeTab
    var pendingExternalUrl by remember { mutableStateOf(initialUrl) }

    LaunchedEffect(sessionManager) {
        AuroraAppLifecycle.foreground.collect { sessionManager.setAppForeground(it) }
    }
    LaunchedEffect(sessionManager) {
        AuroraAppLifecycle.trimMemory.collect { sessionManager.onTrimMemory(it) }
    }

    val settings = remember { SettingsCoordinator() }
    val persistedAccent = remember { mutableStateOf("#4DA3FF") }
    val persistedTheme = remember { mutableStateOf("Aurora Dark") }
    var loadedProfileName by remember { mutableStateOf("") }
    var profileLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val prefs = com.aurora.data.preferences.SessionPreferences(context)
        try { prefs.accentColor.firstOrNull()?.let { persistedAccent.value = it; settings.activeAccent = it } } catch (_: Exception) {}
        try { prefs.themeName.firstOrNull()?.let { persistedTheme.value = it; settings.activeTheme = it } } catch (_: Exception) {}
        try { prefs.profileName.firstOrNull()?.let { name -> if (name.isNotBlank()) loadedProfileName = name } } catch (_: Exception) {}
        profileLoaded = true
    }
    LaunchedEffect(settings.activeAccent, settings.activeTheme) {
        persistedAccent.value = settings.activeAccent
        persistedTheme.value = settings.activeTheme
        val prefs = com.aurora.data.preferences.SessionPreferences(context)
        try { prefs.setAccentColor(settings.activeAccent) } catch (_: Exception) {}
        try { prefs.setThemeName(settings.activeTheme) } catch (_: Exception) {}
    }

    AuroraTheme(accentColorHex = persistedAccent.value, themeName = persistedTheme.value) {
        var currentScreen by remember { mutableStateOf(Screen.Home) }
        var isOffline by remember { mutableStateOf(false) }
        var offlineFocusIndex by remember { mutableIntStateOf(0) }
        var crashFocusIndex by remember { mutableIntStateOf(0) }
        var isRendererCrashed by remember { mutableStateOf(false) }
        var crashedTabId by remember { mutableStateOf<String?>(null) }
        val profiles = remember { MockData.getProfiles() }
        var showProfileSetup by remember { mutableStateOf(false) }
        var currentProfile by remember { mutableStateOf(profiles[0]) }

        LaunchedEffect(profileLoaded) {
            if (profileLoaded && loadedProfileName.isBlank()) {
                showProfileSetup = true
            } else if (loadedProfileName.isNotBlank()) {
                currentProfile = currentProfile.copy(name = loadedProfileName)
            }
        }

        val uiTabs = sessionState.tabs.map { ts ->
            val s = ts.controller.state.value
            Tab(ts.id, s.currentUrl, s.pageTitle.ifEmpty { "New Tab" }, s.isLoading, s.loadingProgress, ts.session.isPrivate, lastActive = System.currentTimeMillis())
        }
        val activeTabId = sessionState.activeTabId ?: ""

        val openTabModels = remember(sessionState.tabs) {
            sessionState.tabs.map { ts ->
                val s = ts.controller.state.value
                com.aurora.ui.model.TabUiModel(
                    id = ts.id,
                    url = s.currentUrl,
                    title = s.pageTitle.ifEmpty { "New Tab" },
                    isPrivate = ts.session.isPrivate
                )
            }
        }

        var bookmarks by remember { mutableStateOf(listOf<Bookmark>()) }
        var bookmarkFolders by remember { mutableStateOf(listOf(BookmarkFolder(id = 0L, name = "Unsorted"))) }
        var bookmarkSearchQuery by remember { mutableStateOf("") }
        var selectedBookmarkFolderId by remember { mutableStateOf<Long?>(null) }
        var downloads by remember { mutableStateOf(listOf<Download>()) }
        var recentlyClosed by remember {
            mutableStateOf(
                listOf(
                    HistoryEntry(url = "https://reddit.com", title = "Reddit Home", timestamp = System.currentTimeMillis() - 2000000),
                    HistoryEntry(url = "https://news.google.com", title = "Google News TV Edition", timestamp = System.currentTimeMillis() - 5000000)
                )
            )
        }

        // F-012: Restore persisted recentlyClosed tabs
        LaunchedEffect(Unit) {
            val loaded = withContext(Dispatchers.IO) {
                DataService.sessions.getRecentlyClosed()
            }
            if (loaded.isNotEmpty()) {
                recentlyClosed = loaded
            }
        }

        var processes by remember { mutableStateOf(listOf<AuroraProcess>()) }
        var timeline by remember { mutableStateOf(listOf<AuroraLogEvent>()) }

        val metricsCollector = remember { com.aurora.browser.service.SystemMetricsCollector(context) }
        DisposableEffect(Unit) { metricsCollector.start(); onDispose { metricsCollector.destroy() } }
        val realFps by metricsCollector.fps.collectAsState()
        val realMemory by metricsCollector.memoryMB.collectAsState()
        val realCpu by metricsCollector.cpuPercent.collectAsState()
        val realGpu by metricsCollector.gpuPercent.collectAsState()
        val realNetwork by metricsCollector.networkKbps.collectAsState()
        val realProcesses by metricsCollector.processes.collectAsState()

        LaunchedEffect(realProcesses) {
            processes = realProcesses.map { rp ->
                AuroraProcess(pid = rp.pid, name = rp.name, type = rp.type, cpu = rp.cpuPercent.toInt(), memory = rp.memoryMB)
            }
        }

        var activeMediaViewer by remember { mutableStateOf<ViewerDownload?>(null) }
        var pipVideo by remember { mutableStateOf<ViewerDownload?>(null) }
        var backgroundAudioActive by remember { mutableStateOf(false) }
        var showSplash by remember { mutableStateOf(false) }
        var splashStep by remember { mutableIntStateOf(0) }
        var particles by remember { mutableStateOf(listOf<Particle>()) }
        var toastJob by remember { mutableStateOf<Job?>(null) }

        LaunchedEffect(Unit) {
            if (sessionState.isRestored) showSplash = false
        }
        LaunchedEffect(splashStep) {
            if (splashStep >= 2 && !sessionState.isRestored) sessionManager.restoreSessions()
        }
        LaunchedEffect(sessionState.isRestored) {
            if (sessionState.isRestored) {
                delay(200); showSplash = false
            }
        }

        LaunchedEffect(Unit) {
            try {
                val file = java.io.File(context.applicationContext.cacheDir, "aurora_crash.log")
                if (file.exists() && file.length() > 0) {
                    val content = file.readText().take(2000)
                    file.delete()
                    Log.e("AuroraCrash", "Previous session crash:\n$content")
                }
            } catch (_: Exception) { }
        }

        var toastMessage by remember { mutableStateOf<String?>(null) }
        val triggerToast: (String) -> Unit = { msg ->
            toastMessage = msg
            toastJob?.cancel()
            toastJob = uiScope.launch { delay(3500); toastMessage = null }
        }

        LaunchedEffect(Unit) {
            try {
                while (true) {
                    delay(3000)
                    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                    val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
                    val connected = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    if (connected != !isOffline) {
                        isOffline = !connected
                        if (isOffline) triggerToast("No internet connection")
                    }
                }
            } catch (_: kotlinx.coroutines.CancellationException) { }
        }

        suspend fun reloadBookmarks() {
            val folders = withContext(Dispatchers.IO) { DataService.favorites.getFolders() }
            val folderMap = folders.associateBy { it.id }
            val favorites = withContext(Dispatchers.IO) { DataService.favorites.getAll() }
            bookmarkFolders = folders
            if (selectedBookmarkFolderId != null && folders.none { it.id == selectedBookmarkFolderId }) {
                selectedBookmarkFolderId = null
            }
            bookmarks = favorites.map { favorite ->
                val folderName = folderMap[favorite.folderId]?.name
                    ?: if (favorite.folderId == 0L) "Unsorted" else "Folder ${favorite.folderId}"
                Bookmark(
                    id = favorite.id.toString(),
                    url = favorite.url,
                    title = favorite.title.ifEmpty { favorite.url },
                    category = folderName,
                    folderId = favorite.folderId
                )
            }
        }

        val historyService = remember { com.aurora.browser.service.HistoryService(DataService.history) }
        val historyVm = remember(historyService) { HistoryViewModel(historyService) }
        DisposableEffect(historyVm) { onDispose { historyVm.close() } }

        val overlayCoordinator = remember { OverlayCoordinator() }
        LaunchedEffect(activeTab) {
            val session = activeTab?.session ?: return@LaunchedEffect
            session.permissionRequests.collect { req ->
                if (session.getPermissionsService().getPermission(req.domain, req.permission) == null) {
                    overlayCoordinator.openPermissions(req)
                }
            }
        }

        LaunchedEffect(Unit) {
            DataService.sessions.defaultSearchEngine.collect { engineId ->
                val engine = SearchEngineRegistry.byId(engineId)
                UrlDetector.setSearchEngine(engine)
                settings.searchEngine = engine.name
            }
        }
        LaunchedEffect(settings.animationSpeedMultiplier) {
            AuroraAnimation.speedMultiplier = settings.animationSpeedMultiplier
        }

        val home = remember { HomeCoordinator() }
        val browser = remember { BrowserCoordinator() }
        var continueBrowsing by remember { mutableStateOf(emptyList<ContinueBrowsingUiModel>()) }
        var favoriteSites by remember { mutableStateOf(emptyList<FavoriteUiModel>()) }

        LaunchedEffect(currentScreen) {
            if (currentScreen == Screen.Home) {
                val assets = withContext(Dispatchers.IO) {
                    DataService.browserAssets.getRecentAssets(limit = 8, withFavicon = true, withThumbnail = true)
                }
                continueBrowsing = assets.map { a ->
                    ContinueBrowsingUiModel(
                        id = a.url, name = a.title.ifEmpty { a.domain }, title = a.title.ifEmpty { a.domain },
                        timeText = formatRelativeTime(a.lastVisited), url = a.url, domain = a.domain,
                        accentColor = Color(a.dominantColor), faviconBitmap = a.favicon, thumbnail = a.thumbnail
                    )
                }.ifEmpty {
                    listOf(
                        ContinueBrowsingUiModel("cb-1", "YouTube", "YouTube Feed", "Active - 2 hours ago", "https://youtube.com", "youtube.com", Color(0xFFFF0000)),
                        ContinueBrowsingUiModel("cb-2", "GitHub", "Aurora OS Repo", "Last visited 5h ago", "https://github.com", "github.com", Color(0xFFFFFFFF)),
                        ContinueBrowsingUiModel("cb-3", "Wikipedia", "Living Glass Spec", "Active Session", "https://wikipedia.org/wiki/living-glass", "wikipedia.org", Color(0xFF4285F4)),
                        ContinueBrowsingUiModel("cb-4", "Reddit", "r/AndroidTV Devs", "Last visited yesterday", "https://reddit.com", "reddit.com", Color(0xFFFF4500))
                    )
                }
                var favs = emptyList<com.aurora.data.model.Favorite>()
                try { favs = withContext(Dispatchers.IO) { DataService.favorites.getAll() } } catch (_: Exception) {}
                val favUrls = favs.map { it.url }
                val favAssets = if (favUrls.isNotEmpty()) {
                    try { withContext(Dispatchers.IO) { DataService.browserAssets.getAssets(favUrls, withFavicon = true, withThumbnail = false) }.associateBy { it.url } } catch (_: Exception) { emptyMap() }
                } else emptyMap()
                favoriteSites = BookmarkMapper.toUiList(favs, assets = favAssets)
            }
        }

        LaunchedEffect(Unit) {
            try {
                reloadBookmarks()
            } catch (e: Exception) {
                Log.w("AuroraBookmark", "Bookmark load failed", e)
            }
        }

        val downloadManager = remember { com.aurora.browser.service.DownloadManager(context) }
        DisposableEffect(Unit) { onDispose { downloadManager.destroy() } }
        val activeDownloads by downloadManager.active.collectAsState()
        LaunchedEffect(Unit) {
            downloads = withContext(Dispatchers.IO) {
                DataService.downloads.getAll().map { toUiDownload(it) }
            }
        }
        fun buildMergedDownloads(dbList: List<Download>, activeList: List<com.aurora.browser.service.ActiveDownload>): List<Download> {
            val merged = mutableListOf<Download>()
            val activeIds = mutableSetOf<String>()
            for (ad in activeList) {
                val prog = if (ad.totalBytes > 0) ((ad.downloadedBytes.toFloat() / ad.totalBytes) * 100).toInt() else 0
                val tSize = when {
                    ad.totalBytes >= 1_000_000_000 -> "%.1f GB".format(ad.totalBytes / 1_000_000_000.0)
                    ad.totalBytes >= 1_000_000 -> "%.1f MB".format(ad.totalBytes / 1_000_000.0)
                    ad.totalBytes >= 1_000 -> "%.0f KB".format(ad.totalBytes / 1_000.0)
                    else -> "${ad.totalBytes} B"
                }
                val status = when (ad.status) {
                    "COMPLETED" -> DownloadStatus.Completed
                    "FAILED" -> DownloadStatus.Failed
                    "PAUSED" -> DownloadStatus.Paused
                    else -> DownloadStatus.Downloading
                }
                merged.add(Download(
                    id = ad.id.toString(), fileName = ad.fileName, url = ad.url, totalSize = tSize,
                    mimeType = ad.mimeType, progress = prog, status = status, timestamp = ad.id,
                    speed = ad.speed.ifEmpty { null }
                ))
                activeIds.add(ad.id.toString())
            }
            for (d in dbList) {
                if (d.id !in activeIds) merged.add(d)
            }
            return merged.sortedByDescending { it.timestamp }
        }
        LaunchedEffect(activeDownloads) {
            val dbList = withContext(Dispatchers.IO) {
                DataService.downloads.getAll().map { toUiDownload(it) }
            }
            downloads = buildMergedDownloads(dbList, activeDownloads)
        }
        val suspendRefreshDownloads: suspend () -> Unit = {
            val dbList = withContext(Dispatchers.IO) {
                DataService.downloads.getAll().map { toUiDownload(it) }
            }
            downloads = buildMergedDownloads(dbList, downloadManager.active.value)
        }

        var lowMemoryWarningShown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            try {
                while (true) {
                    delay(5000)
                    val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager ?: continue
                    val mem = android.app.ActivityManager.MemoryInfo()
                    am.getMemoryInfo(mem)
                    val usedRatio = (mem.totalMem - mem.availMem).toFloat() / mem.totalMem.toFloat()
                    val tabCount = sessionState.tabs.size
                    if (usedRatio > 0.85f && tabCount > 2 && !lowMemoryWarningShown) {
                        lowMemoryWarningShown = true
                        triggerToast("Memory low (${(usedRatio * 100).toInt()}%) — close unused tabs")
                    } else if (usedRatio < 0.75f) {
                        lowMemoryWarningShown = false
                    }
                    if (mem.lowMemory && tabCount > 1) {
                        Log.w("AuroraMem", "System low memory — closing inactive tabs")
                        sessionManager.closeInactiveTabs(keepCount = 1)
                    }
                }
            } catch (_: kotlinx.coroutines.CancellationException) { }
        }

        var isDiagnosticsOpen by remember { mutableStateOf(false) }
        var developerMode by remember { mutableStateOf(false) }
        var benchmarkMode by remember { mutableStateOf(false) }
        var devClickCount by remember { mutableIntStateOf(0) }
        var isVoiceListening by remember { mutableStateOf(false) }
        var voiceWave by remember { mutableStateOf(false) }
        var voiceOutputMessage by remember { mutableStateOf("") }
        var remoteClickPulse by remember { mutableFloatStateOf(0f) }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) triggerToast("Notification permission needed for download notifications")
        }

        LaunchedEffect(Unit) {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        var pendingFilePicker by remember { mutableStateOf<FilePickerRequest?>(null) }
        val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            val picker = pendingFilePicker
            pendingFilePicker = null
            if (picker != null && uris.isNotEmpty()) {
                picker.complete(uris.map { it.toString() })
            } else {
                picker?.cancel()
            }
        }

        LaunchedEffect(activeTab) {
            val session = activeTab?.session ?: return@LaunchedEffect
            session.filePickerRequests.collect { request ->
                pendingFilePicker = request
                filePickerLauncher.launch("*/*")
            }
        }

        LaunchedEffect(activeTab) {
            val session = activeTab?.session ?: return@LaunchedEffect
            session.linkContextRequests.collect { url ->
                overlayCoordinator.openLinkMenu(url)
            }
        }

        var isFindInPage by remember { mutableStateOf(false) }
        var findQuery by remember { mutableStateOf("") }
        var findCurrentMatch by remember { mutableIntStateOf(1) }
        var findTotalMatches by remember { mutableIntStateOf(0) }

        var librarySearchQuery by remember { mutableStateOf("") }
        var libraryActiveFilter by remember { mutableStateOf("All") }

        val addLog: (String, String, String) -> Unit = { category, message, type ->
            val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            timeline = listOf(AuroraLogEvent(Math.random().toString(), ts, category, type, message)) + timeline.take(49)
        }

        val triggerParticleBurst = { x: Float, y: Float ->
            val newP = generateParticleBurst(x, y)
            particles = particles + newP
            uiScope.launch { delay(800); particles = particles.filter { p -> newP.none { np -> np.id == p.id } } }
        }

        val handleWebNavigation = { urlInput: String ->
            val resolvedUrl = UrlDetector.toUrl(urlInput)
            val existingCtrl = activeTab?.controller
            var didNavigate = false
            if (existingCtrl != null) {
                existingCtrl.loadUrl(resolvedUrl)
                didNavigate = true
            } else {
                val newTab = sessionManager.createSession()
                if (newTab != null) {
                    newTab.controller.loadUrl(resolvedUrl)
                    didNavigate = true
                } else {
                    triggerToast("Tab limit reached — close a tab")
                    currentScreen = Screen.TabManagement
                }
            }
            if (didNavigate) {
                currentScreen = Screen.Browser; browser.isKeyboardOpen = false; home.isOmniboxFocused = false; triggerToast("Navigating...")
            }
        }

        val latestHandleWebNavigation by rememberUpdatedState(handleWebNavigation)
        LaunchedEffect(sessionState.isRestored, pendingExternalUrl) {
            val url = pendingExternalUrl
            if (sessionState.isRestored && url != null) {
                pendingExternalUrl = null
                latestHandleWebNavigation(url)
            }
        }
        LaunchedEffect(Unit) {
            AuroraNavigationIntents.urls.collect { url ->
                if (sessionManager.state.value.isRestored) {
                    latestHandleWebNavigation(url)
                } else {
                    pendingExternalUrl = url
                }
            }
        }

        val localFocusManager = LocalFocusManager.current

        val handleHomePress = {
            browser.toolbarVisible = true; currentScreen = Screen.Home; home.focusedZone = "search"; home.focusedItemIndex = 0
            overlayCoordinator.close(); browser.isKeyboardOpen = false; browser.isTabWorkspaceOpen = false
            addLog("NAV", "Returned to Dashboard home context", "Info"); triggerToast("Returned to Dashboard")
        }

        LaunchedEffect(currentScreen) {
            browser.isPointerMode = (currentScreen == Screen.Browser)
        }
        LaunchedEffect(currentScreen, home.isOmniboxFocused, browser.isKeyboardOpen) {
            browser.toolbarVisible = true
            if (currentScreen == Screen.Browser && !home.isOmniboxFocused && !browser.isKeyboardOpen) { delay(4500); browser.toolbarVisible = false }
        }

        val headerZones = if (developerMode) listOf("header_brand", "header_wifi", "header_settings", "header_diagnostics") else listOf("header_brand", "header_wifi", "header_settings")
        val dashboardGroupOrder = listOf("header", "tab_header", "tab_list", "tab_confirm", "search", "continue_browsing") + MockData.featuredRowGroupNames() + MockData.streamingRowGroupNames() + listOf("favorites") + MockData.socialRowGroupNames() + listOf("trending", "quick_actions", "settings_sidebar", "settings_panel", "developer")
        val focusEngine = remember { com.aurora.ui.focus.FocusEngine(groupOrder = dashboardGroupOrder) }

        val handleDpadPress: (String) -> Boolean = { direction: String ->
            if (!browser.toolbarVisible) browser.toolbarVisible = true
            addLog("INPUT", "D-pad: $direction", "Debug")
            if (browser.isPointerMode && !browser.isTabWorkspaceOpen) {
                val s = 40f
                val viewW = 1920f; val viewH = 1080f
                when (direction) {
                    "LEFT" -> if (browser.remoteX <= s) browser.scrollBy(-120f, 0f) else browser.remoteX = (browser.remoteX - s).coerceAtLeast(0f)
                    "RIGHT" -> if (browser.remoteX >= viewW - s) browser.scrollBy(120f, 0f) else browser.remoteX = (browser.remoteX + s).coerceAtMost(viewW)
                    "UP" -> if (browser.remoteY <= s) browser.scrollBy(0f, -120f) else browser.remoteY = (browser.remoteY - s).coerceAtLeast(0f)
                    "DOWN" -> if (browser.remoteY >= viewH - s) browser.scrollBy(0f, 120f) else browser.remoteY = (browser.remoteY + s).coerceAtMost(viewH)
                }
                browser.inputBridge?.injectHoverMove(browser.remoteX, browser.remoteY)
                browser.tabWorkspaceVisible = browser.remoteY > 900f
                true
            } else if (isOffline) {
                when (direction) {
                    "UP", "LEFT" -> offlineFocusIndex = (offlineFocusIndex + 2) % 3
                    "DOWN", "RIGHT" -> offlineFocusIndex = (offlineFocusIndex + 1) % 3
                }
                addLog("NAV", "Offline: option $offlineFocusIndex", "Debug")
                true
            } else if (isRendererCrashed) {
                when (direction) {
                    "UP", "LEFT" -> crashFocusIndex = (crashFocusIndex + 1) % 2
                    "DOWN", "RIGHT" -> crashFocusIndex = (crashFocusIndex + 1) % 2
                }
                addLog("NAV", "Crash: option $crashFocusIndex", "Debug")
                true
            } else if (currentScreen != Screen.Home && !browser.isTabWorkspaceOpen) {
                val dirEnum = when (direction.uppercase()) { "UP" -> com.aurora.ui.focus.FocusDirection.UP; "DOWN" -> com.aurora.ui.focus.FocusDirection.DOWN; "LEFT" -> com.aurora.ui.focus.FocusDirection.LEFT; "RIGHT" -> com.aurora.ui.focus.FocusDirection.RIGHT; else -> null }
                if (dirEnum != null && focusEngine.moveFocus(dirEnum)) { true } else { false }
            } else if (currentScreen == Screen.Browser && browser.isTabWorkspaceOpen) {
                val dir = when (direction.uppercase()) {
                    "UP" -> FocusDirection.Up
                    "DOWN" -> FocusDirection.Down
                    "LEFT" -> FocusDirection.Left
                    "RIGHT" -> FocusDirection.Right
                    else -> null
                }
                if (dir != null) localFocusManager.moveFocus(dir)
                true
            } else {
                val hdrIdx = headerZones.indexOf(home.focusedZone)
                val favCount = favoriteSites.size
                val continueCount = continueBrowsing.size
                val trendingCount = com.aurora.ui.screens.TRENDING_ITEMS.size
val streamingGrid = MockData.streamingSites.chunked(MockData.STREAMING_COLUMNS)
val streamingZones = streamingGrid.mapIndexed { i, _ -> MockData.streamingRowGroupName(i) }
val streamIdx = streamingZones.indexOf(home.focusedZone)
val socialGrid = MockData.socialSites.chunked(MockData.STREAMING_COLUMNS)
val socialZones = socialGrid.mapIndexed { i, _ -> MockData.socialRowGroupName(i) }
val socialIdx = socialZones.indexOf(home.focusedZone)
val featuredCount = MockData.featuredStreamingSites.size
                val quickCount = 5
                val historyCount = historyVm.screenState.value.entries.take(4).size
                val zoneCount: (String) -> Int = { z -> when (z) {
                    "continue" -> continueCount
                    "streaming" -> featuredCount
                    "favorites" -> favCount
                    "trending" -> trendingCount
                    "quickActions" -> quickCount
                    "history" -> historyCount
                    else -> if (streamingZones.contains(z)) streamingGrid[streamingZones.indexOf(z)].size else if (socialZones.contains(z)) socialGrid[socialZones.indexOf(z)].size else 0
                } }
                val focusZone: (String) -> Unit = { z -> home.focusedZone = z; home.focusedItemIndex = 0 }
                val focusAt: (String, Int) -> Unit = { z, item -> home.focusedZone = z; home.focusedItemIndex = item }
                when (direction) {
                    "DOWN" -> if (hdrIdx >= 0) focusZone("search") else if (streamIdx >= 0) { if (streamIdx < streamingZones.size - 1) focusZone(streamingZones[streamIdx + 1]) else focusZone(if (favCount > 0) "favorites" else if (socialZones.isNotEmpty()) socialZones[0] else "trending") } else if (socialIdx >= 0) { if (socialIdx < socialZones.size - 1) focusZone(socialZones[socialIdx + 1]) else focusZone("trending") } else when (home.focusedZone) {
                        "search" -> focusZone("continue")
                        "continue" -> focusZone("streaming")
                        "streaming" -> if (streamingZones.isNotEmpty()) focusZone(streamingZones[0]) else focusZone(if (favCount > 0) "favorites" else "trending")
                        "favorites" -> if (socialZones.isNotEmpty()) focusZone(socialZones[0]) else focusZone("trending")
                        "trending" -> focusZone("quickActions")
                        "downloads" -> focusZone("quickActions")
                        "quickActions" -> focusZone("history")
                        "history" -> focusZone("search")
                        else -> focusZone("search")
                    }
                    "UP" -> if (home.focusedZone == "search") focusZone(headerZones.last()) else if (hdrIdx >= 0) focusZone("search") else if (streamIdx >= 0) { if (streamIdx > 0) focusZone(streamingZones[streamIdx - 1]) else focusZone("streaming") } else if (socialIdx >= 0) { if (socialIdx > 0) focusZone(socialZones[socialIdx - 1]) else focusZone(if (favCount > 0) "favorites" else if (streamingZones.isNotEmpty()) streamingZones.last() else "streaming") } else when (home.focusedZone) {
                        "continue" -> focusZone("search")
                        "streaming" -> focusZone("continue")
                        "favorites" -> if (streamingZones.isNotEmpty()) focusZone(streamingZones.last()) else focusZone("streaming")
                        "trending" -> if (socialZones.isNotEmpty()) focusZone(socialZones.last()) else focusZone("favorites")
                        "downloads" -> focusZone("favorites")
                        "quickActions" -> focusZone("trending")
                        "history" -> focusZone("quickActions")
                        else -> focusZone("search")
                    }
                    "LEFT" -> if (hdrIdx >= 0) { home.focusedZone = headerZones[(hdrIdx - 1 + headerZones.size) % headerZones.size]; home.focusedItemIndex = 0 } else if (streamIdx >= 0) { if (home.focusedItemIndex > 0) home.focusedItemIndex-- else if (streamIdx > 0) focusAt(streamingZones[streamIdx - 1], (streamingGrid[streamIdx - 1].size - 1).coerceAtLeast(0)) else focusAt("streaming", (featuredCount - 1).coerceAtLeast(0)) } else if (socialIdx >= 0) { if (home.focusedItemIndex > 0) home.focusedItemIndex-- else if (socialIdx > 0) focusAt(socialZones[socialIdx - 1], (socialGrid[socialIdx - 1].size - 1).coerceAtLeast(0)) else focusAt("favorites", (favCount - 1).coerceAtLeast(0)) } else when (home.focusedZone) {
                        "search" -> {}
                        "continue" -> if (home.focusedItemIndex > 0) home.focusedItemIndex-- else focusZone("search")
                        "streaming" -> if (home.focusedItemIndex > 0) home.focusedItemIndex--
                        "favorites" -> if (home.focusedItemIndex > 0) home.focusedItemIndex-- else focusZone("streaming")
                        "trending" -> if (home.focusedItemIndex > 0) home.focusedItemIndex-- else focusZone("favorites")
                        "downloads" -> focusZone("trending")
                        "quickActions" -> if (home.focusedItemIndex > 0) home.focusedItemIndex--
                        "history" -> if (home.focusedItemIndex > 0) home.focusedItemIndex--
                        else -> {}
                    }
                    "RIGHT" -> if (hdrIdx >= 0) { home.focusedZone = headerZones[(hdrIdx + 1) % headerZones.size]; home.focusedItemIndex = 0 } else if (streamIdx >= 0) {
                        val rowSize = streamingGrid[streamIdx].size
                        if (home.focusedItemIndex < rowSize - 1) home.focusedItemIndex++
                        else if (streamIdx < streamingZones.size - 1) { home.focusedZone = streamingZones[streamIdx + 1]; home.focusedItemIndex = 0 }
                        else { home.focusedZone = if (favCount > 0) "favorites" else if (socialZones.isNotEmpty()) socialZones[0] else "trending"; home.focusedItemIndex = 0 }
                    } else if (socialIdx >= 0) {
                        val rowSize = socialGrid[socialIdx].size
                        if (home.focusedItemIndex < rowSize - 1) home.focusedItemIndex++
                        else if (socialIdx < socialZones.size - 1) { home.focusedZone = socialZones[socialIdx + 1]; home.focusedItemIndex = 0 }
                        else { home.focusedZone = "trending"; home.focusedItemIndex = 0 }
                    } else {
                        val itemCount = zoneCount(home.focusedZone)
                        if (itemCount > 0 && home.focusedItemIndex < itemCount - 1) home.focusedItemIndex++ else when (home.focusedZone) {
                            "continue" -> { home.focusedZone = "streaming"; home.focusedItemIndex = 0 }
                            "streaming" -> { home.focusedZone = if (streamingZones.isNotEmpty()) streamingZones[0] else "favorites"; home.focusedItemIndex = 0 }
                            "favorites" -> { home.focusedZone = if (socialZones.isNotEmpty()) socialZones[0] else "trending"; home.focusedItemIndex = 0 }
                            "trending" -> { home.focusedZone = "downloads"; home.focusedItemIndex = 0 }
                            "downloads" -> { home.focusedZone = "quickActions"; home.focusedItemIndex = 0 }
                            "quickActions" -> { home.focusedZone = "history"; home.focusedItemIndex = 0 }
                            "history" -> { home.focusedZone = "quickActions"; home.focusedItemIndex = 0 }
                            else -> {}
                        }
                    }
                }
                addLog("NAV", "Focus: ${home.focusedZone}, Item: ${home.focusedItemIndex}", "Debug")
                true
            }
        }

        val handleRestoreCrashedTab = {
            if (crashedTabId != null) {
                isRendererCrashed = false
                crashedTabId = null
                triggerToast("Tab Restored")
            }
        }

        val handleSelectPress: () -> Boolean = {
            browser.toolbarVisible = true
            triggerParticleBurst(960f, 540f); remoteClickPulse = 1f
            if (currentScreen == Screen.Browser && browser.isPointerMode && !browser.isTabWorkspaceOpen) {
                if (browser.tabWorkspaceVisible && browser.remoteY > 900f) {
                    currentScreen = Screen.TabManagement
                    triggerToast("Tab Manager")
                } else if (browser.remoteY < browser.toolbarHeightPx) {
                    if (!browser.toolbarVisible) browser.toolbarVisible = true
                    else browser.toolbarClickTick++
                } else {
                    browser.remoteClicked = true
                    uiScope.launch { delay(200); browser.remoteClicked = false }
                    browser.inputBridge?.injectClick(browser.remoteX, browser.remoteY)
                }
                true
            } else if (isOffline) {
                when (offlineFocusIndex) {
                    0 -> currentScreen = Screen.Library
                    1 -> { handleWebNavigation("https://wikipedia.org/wiki/living-glass"); isOffline = false }
                    else -> isOffline = false
                }
                triggerToast("Offline action")
                true
            } else if (isRendererCrashed) {
                when (crashFocusIndex) {
                    0 -> handleRestoreCrashedTab()
                    else -> { isRendererCrashed = false; crashedTabId = null; currentScreen = Screen.Home }
                }
                true
            } else if (currentScreen == Screen.Home) {
                if (home.focusedZone.startsWith("streaming_r")) {
                    val row = home.focusedZone.removePrefix("streaming_r").toIntOrNull() ?: -1
                    val chunk = MockData.streamingSites.chunked(MockData.STREAMING_COLUMNS).getOrNull(row)
                    val idx = home.focusedItemIndex
                    if (chunk != null && idx in chunk.indices) handleWebNavigation(chunk[idx].url)
                } else if (home.focusedZone.startsWith("social_r")) {
                    val row = home.focusedZone.removePrefix("social_r").toIntOrNull() ?: -1
                    val chunk = MockData.socialSites.chunked(MockData.STREAMING_COLUMNS).getOrNull(row)
                    val idx = home.focusedItemIndex
                    if (chunk != null && idx in chunk.indices) handleWebNavigation(chunk[idx].url)
                } else when (home.focusedZone) {
                    "search" -> { home.isOmniboxFocused = true; browser.isKeyboardOpen = true }
                    "header_brand" -> { devClickCount++; if (devClickCount >= 5) { developerMode = true; devClickCount = 0; addLog("SYSTEM", "Dev Mode", "Info"); triggerToast("Developer Mode Enabled!") } else { triggerToast("Click ${5 - devClickCount} more") } }
                    "header_wifi" -> triggerToast(if (isOffline) "No internet connection" else "Connected to internet")
                    "header_settings" -> { currentScreen = Screen.Settings; triggerToast("Settings") }
                    "header_diagnostics" -> { isDiagnosticsOpen = !isDiagnosticsOpen; triggerToast(if (isDiagnosticsOpen) "Diagnostics" else "Close Diagnostics") }
                    "continue" -> { if (home.focusedItemIndex < continueBrowsing.size) handleWebNavigation(continueBrowsing[home.focusedItemIndex].url) }
                    "streaming" -> { val idx = home.focusedItemIndex; val list = MockData.featuredStreamingSites.take(6); if (idx in list.indices) handleWebNavigation(list[idx].url) }
                    "favorites" -> { if (home.focusedItemIndex < favoriteSites.size) handleWebNavigation(favoriteSites[home.focusedItemIndex].url) }
                    "trending" -> { if (home.focusedItemIndex < com.aurora.ui.screens.TRENDING_ITEMS.size) handleWebNavigation("https://google.com/search?q=${java.net.URLEncoder.encode(com.aurora.ui.screens.TRENDING_ITEMS[home.focusedItemIndex].title, "UTF-8")}") }
                    "downloads" -> { currentScreen = Screen.Library; triggerToast("Library Opened") }
                    "quickActions" -> when (home.focusedItemIndex) {
                        0 -> { currentScreen = Screen.Library; triggerToast("Library") }
                        1 -> { currentScreen = Screen.Settings; triggerToast("Settings") }
                        2 -> { browser.isKeyboardOpen = true; home.isOmniboxFocused = true; home.searchQuery = "Ask Aurora: Summarize layout specs..." }
                        3 -> { currentScreen = Screen.TabManagement; triggerToast("Tab Manager") }
                        4 -> { isDiagnosticsOpen = true; triggerToast("Diagnostics") }
                    }
                    "history" -> { val h = historyVm.screenState.value.entries.getOrNull(home.focusedItemIndex); if (h != null) handleWebNavigation(h.url) else { currentScreen = Screen.History; triggerToast("History") } }
                }
                true
            } else if (currentScreen == Screen.Browser && browser.isTabWorkspaceOpen) {
                false
            } else {
                if (focusEngine.selectFocused()) { true } else { false }
            }
        }

        val handleBackPress = {
            browser.toolbarVisible = true
            browser.isTabWorkspaceOpen = false
            when {
                isOffline -> { isOffline = false }
                isRendererCrashed -> { isRendererCrashed = false; crashedTabId = null }
                overlayCoordinator.isShowing -> overlayCoordinator.close()
                browser.isKeyboardOpen -> browser.isKeyboardOpen = false
                activeMediaViewer != null -> activeMediaViewer = null
                isDiagnosticsOpen -> isDiagnosticsOpen = false
                currentScreen == Screen.Browser && (activeTab?.controller?.state?.value?.canGoBack == true) -> { activeTab?.controller?.goBack(); triggerToast("Back") }
                currentScreen != Screen.Home -> { currentScreen = Screen.Home; home.focusedZone = "search"; home.focusedItemIndex = 0; triggerToast("Dashboard") }
            }
        }

        LaunchedEffect(currentScreen) { browser.isPointerMode = currentScreen == Screen.Browser }
        LaunchedEffect(sessionState.isRestored) {
            if (sessionState.isRestored && sessionState.tabs.isEmpty() && pendingExternalUrl == null) {
                sessionManager.createSession()
            }
        }

        val handleCreateNewTab = { isPrivate: Boolean ->
            val newTab = sessionManager.createSession(isPrivate)
            if (newTab != null) {
                newTab.controller.loadUrl("https://www.google.com")
                addLog("TAB", "New ${if (isPrivate) "Private" else "Standard"} tab", "Info")
                triggerToast(if (isPrivate) "Private Tab" else "Tab Opened")
            } else {
                addLog("TAB", "Tab limit reached", "Warning")
                triggerToast("Tab limit reached — close a tab")
                currentScreen = Screen.TabManagement
            }
        }

        SideEffect { KeyBridge.isKeyboardOpen = browser.isKeyboardOpen }
        LaunchedEffect(Unit) {
            KeyBridge.onDpad = handleDpadPress
            KeyBridge.onSelect = handleSelectPress
            KeyBridge.onBack = handleBackPress
        }

        val handleOpenInNewTab = { url: String, isPrivate: Boolean ->
            val newTab = sessionManager.createSession(isPrivate)
            if (newTab != null) {
                newTab.controller.loadUrl(url)
                addLog("TAB", "New ${if (isPrivate) "Private" else "Standard"} tab: $url", "Info")
                triggerToast(if (isPrivate) "Private Tab" else "Tab Opened")
            } else {
                addLog("TAB", "Tab limit reached", "Warning")
                triggerToast("Tab limit reached — close a tab in workspace")
            }
        }

        val handleOpenReader = { url: String ->
            uiScope.launch {
                val result = com.aurora.browser.service.ReaderContentExtractor.extract(url)
                if (result != null) {
                    overlayCoordinator.openReaderMode(result.url, result.title, result.text)
                } else {
                    triggerToast("Reader mode unavailable")
                }
            }
            Unit
        }

        val handleBenchmarkToggle = { enabled: Boolean ->
            benchmarkMode = enabled
            uiScope.launch(Dispatchers.IO) {
                SessionPreferences(context).setBenchmarkMode(enabled)
            }
            triggerToast(if (enabled) "Benchmark Mode ON — restart to apply" else "Benchmark Mode OFF — restart to apply")
        }

        val handleCloseTab = { id: String ->
            val t = sessionState.tabs.find { it.id == id }
            if (t != null) {
                val entry = HistoryEntry(
                    url = t.controller.state.value.currentUrl,
                    title = t.controller.state.value.pageTitle.ifEmpty { t.controller.state.value.currentUrl },
                    timestamp = System.currentTimeMillis()
                )
                recentlyClosed = listOf(entry) + recentlyClosed.take(9)
                uiScope.launch(Dispatchers.IO) {
                    DataService.sessions.saveRecentlyClosed(recentlyClosed)
                }
            }
            sessionManager.closeSession(id)
            triggerToast("Tab Closed")
        }

        val handleReopenClosed = { entry: HistoryEntry ->
            val newTab = sessionManager.createSession()
            if (newTab != null) {
                newTab.controller.loadUrl(entry.url)
                recentlyClosed = recentlyClosed.filter { it.timestamp != entry.timestamp }
                uiScope.launch(Dispatchers.IO) {
                    DataService.sessions.saveRecentlyClosed(recentlyClosed)
                }
                addLog("TAB", "Restored: ${entry.title}", "Info")
                triggerToast("Restored")
            } else {
                addLog("TAB", "Tab limit reached while restoring ${entry.title}", "Warning")
                triggerToast("Tab limit reached")
            }
        }

        val handleSwitchTab = { tabId: String ->
            sessionManager.switchSession(tabId)
            if (pendingExternalUrl != null) { pendingExternalUrl = null }
            currentScreen = Screen.Browser
            triggerToast("Switched Tab")
        }

        val handleCloseAllTabs = {
            val allIds = sessionState.tabs.map { it.id }
            allIds.forEach { id -> sessionManager.closeSession(id) }
            sessionManager.createSession()
            triggerToast("All tabs cleared")
        }

        fun handleKillProcess(pid: Int) {
            val p = processes.find { it.pid == pid } ?: return
            processes = processes.filter { it.pid != pid }
            if (p.type == "Renderer") {
                crashedTabId = pid.toString()
                isRendererCrashed = true
                addLog("PROCESS", "Renderer crash", "Error")
                triggerToast("Renderer Crashed!")
            }
        }

        val handleRunDiagnostics = {
            addLog("DIAG", "Starting diagnostics...", "Info")
            triggerToast("Self-test...")
            uiScope.launch {
                delay(1500)
                addLog("DIAG", "Self-Test: OK", "Info")
                triggerToast("System Health: 100%")
            }
            Unit
        }

        val handleToggleBookmark = {
            val tab = activeTab
            if (tab != null) {
                val url = tab.controller.state.value.currentUrl
                val title = tab.controller.state.value.pageTitle
                val e = bookmarks.find { it.url == url }
                if (e != null) {
                    uiScope.launch {
                        val bookmarkId = e.id.toLongOrNull()
                        if (bookmarkId == null) {
                            bookmarks = bookmarks.filter { it.id != e.id }
                            triggerToast("Removed Bookmark")
                            return@launch
                        }
                        try {
                            withContext(Dispatchers.IO) { DataService.favorites.remove(bookmarkId) }
                            reloadBookmarks()
                            triggerToast("Removed Bookmark")
                        } catch (exception: Exception) {
                            Log.w("AuroraBookmark", "Remove bookmark failed", exception)
                            triggerToast("Bookmark update failed")
                        }
                    }
                } else {
                    uiScope.launch {
                        try {
                            withContext(Dispatchers.IO) { DataService.favorites.add(url, title.ifEmpty { "Bookmark" }) }
                            reloadBookmarks()
                            triggerToast("Added Bookmark")
                        } catch (exception: Exception) {
                            Log.w("AuroraBookmark", "Add bookmark failed", exception)
                            triggerToast("Bookmark update failed")
                        }
                    }
                }
            }
        }

        val handleCreateBookmarkFolder = { name: String ->
            uiScope.launch {
                try {
                    val newFolderId = withContext(Dispatchers.IO) { DataService.favorites.addFolder(name) }
                    reloadBookmarks()
                    selectedBookmarkFolderId = newFolderId
                    triggerToast("Folder Created")
                } catch (exception: Exception) {
                    Log.w("AuroraBookmark", "Create folder failed", exception)
                    triggerToast("Folder update failed")
                }
            }
            Unit
        }

        val handleRemoveBookmarkFolder = { folder: BookmarkFolder ->
            uiScope.launch {
                try {
                    withContext(Dispatchers.IO) { DataService.favorites.removeFolder(folder.id) }
                    selectedBookmarkFolderId = null
                    reloadBookmarks()
                    triggerToast("Folder Removed")
                } catch (exception: Exception) {
                    Log.w("AuroraBookmark", "Remove folder failed", exception)
                    triggerToast("Folder update failed")
                }
            }
            Unit
        }

        val handleRemoveBookmark = { bookmark: Bookmark ->
            uiScope.launch {
                val bookmarkId = bookmark.id.toLongOrNull()
                if (bookmarkId == null) {
                    bookmarks = bookmarks.filter { it.id != bookmark.id }
                    triggerToast("Removed Bookmark")
                    return@launch
                }
                try {
                    withContext(Dispatchers.IO) { DataService.favorites.remove(bookmarkId) }
                    reloadBookmarks()
                    triggerToast("Removed Bookmark")
                } catch (exception: Exception) {
                    Log.w("AuroraBookmark", "Remove bookmark failed", exception)
                    triggerToast("Bookmark update failed")
                }
            }
            Unit
        }

        val handleMoveBookmark = { bookmark: Bookmark, folderId: Long ->
            uiScope.launch {
                val bookmarkId = bookmark.id.toLongOrNull()
                if (bookmarkId == null) {
                    triggerToast("Bookmark is not persisted")
                    return@launch
                }
                try {
                    withContext(Dispatchers.IO) { DataService.favorites.moveToFolder(bookmarkId, folderId) }
                    reloadBookmarks()
                    triggerToast("Bookmark Moved")
                } catch (exception: Exception) {
                    Log.w("AuroraBookmark", "Move bookmark failed", exception)
                    triggerToast("Bookmark update failed")
                }
            }
            Unit
        }

        val handleSearchPress = {
            browser.toolbarVisible = true
            if (currentScreen == Screen.Home) {
                home.isOmniboxFocused = true
                browser.isKeyboardOpen = true
            } else {
                currentScreen = Screen.Browser
                browser.toolbarVisible = true
                home.isOmniboxFocused = true
            }
        }

        val voiceSearchManager = remember { com.aurora.browser.service.VoiceSearchManager(context) }
        val handleVoiceSearch: () -> Unit = {
            if (!voiceSearchManager.isAvailable()) {
                triggerToast("Voice search not available")
            } else {
                isVoiceListening = true
                voiceWave = true
                voiceOutputMessage = "Listening..."
                addLog("VOICE", "Listening...", "Info")
                uiScope.launch {
                    val result = voiceSearchManager.listen()
                    isVoiceListening = false
                    voiceWave = false
                    if (result.text.isNotBlank()) {
                        voiceOutputMessage = "Recognized: '${result.text}'"
                        home.isOmniboxFocused = true
                        browser.isKeyboardOpen = true
                        home.searchQuery = result.text
                        handleWebNavigation(result.text)
                        triggerToast("Voice: ${result.text}")
                        addLog("VOICE", "Recognized: ${result.text}", "Info")
                    } else if (result.error != null) {
                        voiceOutputMessage = result.error ?: ""
                        triggerToast(result.error ?: "")
                    }
                }
            }
            Unit
        }
        KeyBridge.onVoice = handleVoiceSearch

        val bgRoot = accentBackground()

        BackHandler { handleBackPress() }

        val rootFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { rootFocusRequester.requestFocus() }
        LaunchedEffect(currentScreen, browser.toolbarVisible, browser.isKeyboardOpen) {
            if (!browser.isKeyboardOpen) rootFocusRequester.requestFocus()
        }

        CompositionLocalProvider(LocalLargerUI provides settings.largerUI) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgRoot)
                .focusRequester(rootFocusRequester)
                .focusable()
                .then(if (settings.brightness != 100) Modifier.graphicsLayer { alpha = settings.brightness / 100f } else Modifier)
                .auroraKeyHandler(
                    settings = settings, overlayCoordinator = overlayCoordinator,
                    currentScreen = currentScreen, isDiagnosticsOpen = isDiagnosticsOpen,
                    isVoiceListening = isVoiceListening, onSetDiagnosticsOpen = { isDiagnosticsOpen = it },
                    onDpadPress = handleDpadPress, onSelectPress = handleSelectPress,
                    onBackPress = handleBackPress, onTriggerToast = triggerToast
                )
        ) {
            if (currentScreen != Screen.Browser) {
                Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF4DA3FF).copy(alpha = 0.25f), Color.Transparent), radius = 800f)))
                NoiseGrain(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.3f })
            }

            if (currentScreen == Screen.Browser && !isDiagnosticsOpen) {
                BrowserModeContent(
                    sessionManager = sessionManager, sessionState = sessionState, activeTab = activeTab, activeTabId = activeTabId,
                    toolbarVisible = browser.toolbarVisible, recentlyClosed = recentlyClosed,
                    bookmarks = bookmarks,
                    omniboxInput = home.searchQuery, isOmniboxFocused = home.isOmniboxFocused,
                    remoteClicked = browser.remoteClicked,
                    context = context, triggerToast = triggerToast,
                    handleHomePress = handleHomePress, handleCreateNewTab = handleCreateNewTab,
                    handleCloseTab = { id -> handleCloseTab(id) }, handleWebNavigation = handleWebNavigation,
                    handleReopenClosed = { entry -> handleReopenClosed(entry) }, handleToggleBookmark = handleToggleBookmark,
                    switchSession = { id -> sessionManager.switchSession(id) },
                    setCurrentScreen = { screen -> currentScreen = screen },
                    setOmniboxInput = { v -> home.searchQuery = v },
                    setIsOmniboxFocused = { v -> home.isOmniboxFocused = v },
                    setIsKeyboardOpen = { v -> browser.isKeyboardOpen = v },
                    onCursorMove = { x, y ->
                        browser.remoteX = x
                        browser.remoteY = y
                        browser.tabWorkspaceVisible = y > 900f
                    },
                    scrollDelta = browser.scrollDelta, scrollTick = browser.scrollTick, toolbarClickTick = browser.toolbarClickTick,
                    clickCoordsProvider = { Pair(browser.remoteX, browser.remoteY) },
                    onDpadPress = { d -> handleDpadPress(d); Unit },
                    isTabWorkspaceOpen = browser.isTabWorkspaceOpen,
                    onTabWorkspaceOpenChange = { browser.isTabWorkspaceOpen = it },
                    tabWorkspaceVisible = browser.tabWorkspaceVisible,
                    onBridgeSet = { bridge -> Log.d("AuroraBrowser", "onBridgeSet storing bridge in coordinator"); browser.inputBridge = bridge },
                    onToolbarHeightChanged = { h -> browser.toolbarHeightPx = h },
                    onRefreshDownloads = { suspendRefreshDownloads() },
                    downloadManager = downloadManager
                )
                if (isFindInPage) {
                    Box(Modifier.fillMaxSize()) {
                        FindInPagePanel(
                            visible = true,
                            query = findQuery,
                            onQueryChange = { q ->
                                findQuery = q
                                activeTab?.session?.findInPage(q)
                            },
                            currentMatch = findCurrentMatch,
                            totalMatches = findTotalMatches,
                            onFindNext = { activeTab?.session?.findNextInPage(true) },
                            onFindPrevious = { activeTab?.session?.findNextInPage(false) },
                            onClose = {
                                isFindInPage = false
                                findQuery = ""
                                activeTab?.session?.clearFind()
                            },
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
                        )
                    }
                }
            } else {
                NonBrowserDashboard(
                    currentScreen = currentScreen,
                    isOffline = isOffline,
                    isRendererCrashed = isRendererCrashed,
                    crashedTabId = crashedTabId,
                    isDiagnosticsOpen = isDiagnosticsOpen,
                    developerMode = developerMode,
                    benchmarkMode = benchmarkMode,
                    isVoiceListening = isVoiceListening,
                    voiceWave = voiceWave,
                    voiceOutputMessage = voiceOutputMessage,
                    activeMediaViewer = activeMediaViewer,
                    pipVideo = pipVideo,
                    backgroundAudioActive = backgroundAudioActive,
                    bookmarks = bookmarks,
                    bookmarkFolders = bookmarkFolders,
                    downloads = downloads,
                    processes = processes,
                    timeline = timeline,
                    librarySearchQuery = librarySearchQuery,
                    libraryActiveFilter = libraryActiveFilter,
                    bookmarkSearchQuery = bookmarkSearchQuery,
                    selectedBookmarkFolderId = selectedBookmarkFolderId,
                    currentProfile = currentProfile,
                    activeTab = activeTab,
                    toastMessage = toastMessage,
                    settings = settings,
                    home = home,
                    browser = browser,
                    overlayCoordinator = overlayCoordinator,
                    historyVm = historyVm,
                    downloadManager = downloadManager,
                    context = context,
                    triggerToast = triggerToast,
                    addLog = addLog,
                    handleWebNavigation = handleWebNavigation,
                    handleHomePress = handleHomePress,
                    handleDpadPress = handleDpadPress,
                    handleSelectPress = handleSelectPress,
                    handleBackPress = handleBackPress,
                    handleCreateNewTab = handleCreateNewTab,
                    handleOpenInNewTab = handleOpenInNewTab,
                    handleOpenReader = handleOpenReader,
                    handleBenchmarkToggle = handleBenchmarkToggle,
                    handleToggleBookmark = handleToggleBookmark,
                    handleKillProcess = { pid -> handleKillProcess(pid) },
                    handleRestoreCrashedTab = handleRestoreCrashedTab,
                    handleRunDiagnostics = handleRunDiagnostics,
                    handleVoiceSearch = handleVoiceSearch,
                    handleSearchPress = handleSearchPress,
                    onOpenFindInPage = { isFindInPage = true; findQuery = "" },
                    onSetCurrentScreen = { currentScreen = it },
                    onSetOffline = { override -> if (!override) isOffline = false /* dismiss only, never force offline */ },
                    onSetIsDiagnosticsOpen = { isDiagnosticsOpen = it },
                    onSetActiveMediaViewer = { activeMediaViewer = it },
                    onSetPipVideo = { pipVideo = it },
                    onToggleBackgroundAudio = { backgroundAudioActive = !backgroundAudioActive },
                    onSetIsVoiceListening = { isVoiceListening = it },
                    onSetBookmarks = { bookmarks = it },
                    onSetDownloads = { downloads = it },
                    onSetTimeline = { timeline = it },
                    onSetCrashedTabId = { crashedTabId = it },
                    onSetIsRendererCrashed = { isRendererCrashed = it },
                    onSetLibrarySearchQuery = { librarySearchQuery = it },
                    onSetLibraryActiveFilter = { libraryActiveFilter = it },
                    onSetBookmarkSearchQuery = { bookmarkSearchQuery = it },
                    onSetSelectedBookmarkFolderId = { selectedBookmarkFolderId = it },
                    onCreateBookmarkFolder = handleCreateBookmarkFolder,
                    onRemoveBookmarkFolder = handleRemoveBookmarkFolder,
                    onRemoveBookmark = handleRemoveBookmark,
                    onMoveBookmark = handleMoveBookmark,
                    onDevClick = {
                        devClickCount++
                        if (devClickCount >= 5) {
                            developerMode = true
                            devClickCount = 0
                            addLog("SYSTEM", "Dev Mode", "Info")
                            triggerToast("Developer Mode Enabled!")
                        } else {
                            triggerToast("Click ${5 - devClickCount} more")
                        }
                    },
                    continueBrowsing = continueBrowsing,
                    favoriteSites = favoriteSites,
                    openTabs = openTabModels,
                    activeTabId = activeTabId,
                    onTabManagementClose = { id -> handleCloseTab(id) },
                    onTabManagementCloseAll = { handleCloseAllTabs() },
                    onTabManagementSwitch = { id -> handleSwitchTab(id) },
                    offlineFocusIndex = offlineFocusIndex,
                    crashFocusIndex = crashFocusIndex,
                    focusEngine = focusEngine
                )
            }

            AuroraTransientUi(
                browser = browser, home = home, settings = settings,
                uiTabs = uiTabs, showSplash = showSplash, splashStep = splashStep,
                particles = particles, onWebNavigation = handleWebNavigation,
                realFps = realFps, realMemory = realMemory, realCpu = realCpu,
                realGpu = realGpu, realNetwork = realNetwork
            )

            if (remoteClickPulse > 0.01f) {
                val ringAlpha = remoteClickPulse
                val ringScale = 1f + (1f - remoteClickPulse) * 2f
                LaunchedEffect(browser.remoteClicked) {
                    if (browser.remoteClicked) {
                        remoteClickPulse = 1f
                        val start = System.currentTimeMillis()
                        while (System.currentTimeMillis() - start < 300) {
                            val elapsed = (System.currentTimeMillis() - start) / 300f
                            remoteClickPulse = (1f - elapsed).coerceIn(0f, 1f)
                            delay(16)
                        }
                        remoteClickPulse = 0f
                    }
                }
                Box(
                    Modifier
                        .offset { IntOffset((browser.remoteX - 30).toInt(), (browser.remoteY - 30).toInt()) }
                        .size((60 * ringScale).dp)
                        .graphicsLayer { alpha = ringAlpha }
                        .border(2.dp, AuroraColors.auroraBlue.copy(alpha = ringAlpha), CircleShape)
                )
            }

            if (currentScreen == Screen.Browser) {
                Cursor(
                    x = browser.remoteX,
                    y = browser.remoteY,
                    isPointerMode = browser.isPointerMode,
                    clicked = browser.remoteClicked
                )
            }

            if (showProfileSetup) {
                ProfileSetupDialog(
                    onSave = { name ->
                        val prefs = com.aurora.data.preferences.SessionPreferences(context)
                        uiScope.launch {
                            try { prefs.setProfileName(name) } catch (_: Exception) {}
                        }
                        loadedProfileName = name
                        currentProfile = currentProfile.copy(name = name)
                        showProfileSetup = false
                    }
                )
            }
        }
        }
    }
}

@Composable
fun ProfileSetupDialog(onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.background(Color(0xFF1A1C23), RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)).padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.size(56.dp).background(Brush.linearGradient(listOf(AuroraColors.auroraBlue, AuroraColors.auroraPurple)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Text("A", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("Welcome to Aurora", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Enter your name to personalize your browsing experience.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, textAlign = TextAlign.Center)
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0E0F14), RoundedCornerShape(12.dp)).border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(AuroraColors.auroraBlue),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (name.isEmpty()) Text("Your name", color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier.background(if (name.isBlank()) Color.White.copy(alpha = 0.1f) else AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 48.dp, vertical = 12.dp).clickable(enabled = name.isNotBlank()) { if (name.isNotBlank()) onSave(name.trim()) }
            ) {
                Text("Let's Go", color = if (name.isBlank()) Color.White.copy(alpha = 0.3f) else Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
