package com.aurora.ui.viewmodel

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.aurora.browser.controller.BrowserController
import com.aurora.data.DataService
import com.aurora.data.model.TabInfo
import com.aurora.engine.BrowserEngine
import com.aurora.engine.BrowserSession
import com.aurora.engine.BrowserSettings
import com.aurora.engine.LoginStorage
import com.aurora.ui.EngineFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Stable
data class TabSession(
    val id: String,
    val session: BrowserSession,
    val controller: BrowserController
)

@Stable
data class SessionManagerState(
    val tabs: List<TabSession> = emptyList(),
    val activeTabId: String? = null,
    val isRestored: Boolean = false
) {
    val activeTab: TabSession? get() = tabs.find { it.id == activeTabId }
}

enum class TabLifecycle { Active, Background, Sleeping, Discarded }

class SessionManager(
    private val engine: BrowserEngine,
    private val maxTabs: Int = 5,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val _state = MutableStateFlow(SessionManagerState())
    val state: StateFlow<SessionManagerState> = _state.asStateFlow()

    private val tabCollectorJobs = mutableMapOf<String, kotlinx.coroutines.Job>()
    private val tabThumbnailServices = mutableMapOf<String, com.aurora.data.service.ThumbnailService>()
    private val tabLifecycle = mutableMapOf<String, TabLifecycle>()
    private val tabBackgroundSince = mutableMapOf<String, Long>()
    private var isForeground = true
    private var pendingSave: kotlinx.coroutines.Job? = null
    private var lifecycleJob: kotlinx.coroutines.Job? = null

    val engineName: String
        get() = engine.getEngineName()

    val runtimeReady: StateFlow<Boolean>
        get() = engine.runtimeReady()

    val browserSettings: BrowserSettings
        get() = engine.getSettings()

    val loginStorage: LoginStorage
        get() = engine.getLoginStorage()

    fun createSession(isPrivate: Boolean = false): TabSession? {
        if (_state.value.tabs.size >= maxTabs) {
            Log.w("AuroraSession", "Tab limit reached maxTabs=$maxTabs")
            return null
        }
        val id = "tab-${UUID.randomUUID().toString().take(8)}"
        val session = engine.createSession(isPrivate)
        session.onNewSessionRequest = { url -> handleRequestWindow(url, isPrivate) }
        val metadataService = session.createMetadataService(
            metadataCache = DataService.metadataCache,
            thumbnailCache = DataService.thumbnailCache
        )
        val thumbnailService = session.createThumbnailService(DataService.thumbnailCache)
        val controller = BrowserController(
            session = session,
            historyRepo = DataService.history,
            sessionRepo = DataService.sessions,
            metadataService = metadataService,
            thumbnailService = thumbnailService
        )
        val tabSession = TabSession(id, session, controller)
        _state.update {
            it.copy(tabs = it.tabs + tabSession, activeTabId = id)
        }

        val th = thumbnailService
        tabThumbnailServices[id] = th
        tabLifecycle[id] = TabLifecycle.Active
        tabBackgroundSince[id] = System.currentTimeMillis()
        if (lifecycleJob == null) startLifecycleMonitor()
        val collectorJob = scope.launch {
            controller.state.collect {
                saveCurrentTabsState()
            }
        }
        tabCollectorJobs[id] = collectorJob

        updateSessionActivity()
        saveCurrentTabsState()
        return tabSession
    }

    fun closeSession(tabId: String) {
        tabCollectorJobs[tabId]?.cancel()
        tabCollectorJobs.remove(tabId)
        tabThumbnailServices[tabId]?.cancel()
        tabThumbnailServices.remove(tabId)
        tabLifecycle[tabId] = TabLifecycle.Discarded
        tabBackgroundSince.remove(tabId)
        _state.update { state ->
            val tab = state.tabs.find { it.id == tabId } ?: return@update state
            tab.controller.close()
            tab.session.close()
            val remaining = state.tabs.filter { it.id != tabId }
            state.copy(
                tabs = remaining,
                activeTabId = when {
                    remaining.isEmpty() -> null
                    state.activeTabId == tabId -> remaining.last().id
                    else -> state.activeTabId
                }
            )
        }
        updateSessionActivity()
        saveCurrentTabsState()
    }

    fun switchSession(tabId: String) {
        val oldActiveId = _state.value.activeTabId
        _state.update { it.copy(activeTabId = tabId) }
        if (oldActiveId != null && oldActiveId != tabId) {
            tabLifecycle[oldActiveId] = TabLifecycle.Background
            tabBackgroundSince[oldActiveId] = System.currentTimeMillis()
        }
        tabLifecycle[tabId] = TabLifecycle.Active
        updateSessionActivity()
        saveCurrentTabsState()
    }

    private fun handleRequestWindow(url: String, isPrivate: Boolean): BrowserSession? {
        val tab = createSession(isPrivate) ?: return null
        if (url.isNotBlank()) tab.session.loadUrl(url)
        return tab.session
    }

    fun closeAll() {
        tabCollectorJobs.values.forEach { it.cancel() }
        tabCollectorJobs.clear()
        tabThumbnailServices.values.forEach { it.cancel() }
        tabThumbnailServices.clear()
        tabLifecycle.clear()
        tabBackgroundSince.clear()
        _state.value.tabs.forEach {
            it.controller.close()
            it.session.close()
        }
        _state.update { SessionManagerState(isRestored = true) }
        saveCurrentTabsState()
    }

    fun setAppForeground(foreground: Boolean) {
        isForeground = foreground
        updateSessionActivity()
    }

    fun onTrimMemory(level: Int) {
        Log.w("AuroraSession", "onTrimMemory level=$level tabs=${_state.value.tabs.size}")
        saveCurrentTabsState()
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> setAppForeground(false)
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> closeInactiveTabs(keepCount = 1)
            else -> if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
                closeInactiveTabs(keepCount = 1)
            }
        }
    }

    private fun startLifecycleMonitor() {
        lifecycleJob?.cancel()
        lifecycleJob = scope.launch {
            while (isActive) {
                delay(30_000)
                runLifecycleCheck()
            }
        }
    }

    private fun runLifecycleCheck() {
        val now = System.currentTimeMillis()
        val idleThreshold = 120_000L
        val sleepThreshold = 300_000L
        val activeId = _state.value.activeTabId
        for ((id, state) in tabLifecycle.toMap()) {
            if (id == activeId) continue
            val since = tabBackgroundSince[id] ?: now
            when (state) {
                TabLifecycle.Background -> {
                    if (now - since > sleepThreshold) {
                        tabLifecycle[id] = TabLifecycle.Sleeping
                        _state.value.tabs.find { it.id == id }?.session?.setActive(false)
                        Log.d("AuroraSession", "Tab $id → Sleeping (inactive ${(now - since) / 1000}s)")
                    }
                }
                TabLifecycle.Sleeping -> {
                    if (_state.value.tabs.size > 2 && now - since > sleepThreshold * 2) {
                        tabLifecycle[id] = TabLifecycle.Discarded
                        tabCollectorJobs[id]?.cancel()
                        _state.value.tabs.find { it.id == id }?.session?.close()
                        Log.d("AuroraSession", "Tab $id → Discarded")
                    }
                }
                else -> {}
            }
        }
    }

    private fun saveCurrentTabsState() {
        if (!_state.value.isRestored) return
        pendingSave?.cancel()

        val tabsToSave = _state.value.tabs
            .filter { !it.session.isPrivate }
            .mapIndexed { index, tabSession ->
            TabInfo(
                id = tabSession.id,
                url = tabSession.controller.state.value.currentUrl,
                title = tabSession.controller.state.value.pageTitle,
                isPrivate = false,
                tabOrder = index,
                isPinned = false,
                createdAt = System.currentTimeMillis(),
                lastVisited = System.currentTimeMillis(),
                scrollPosition = 0,
                zoomLevel = 1.0f,
                readerModeEnabled = false
            )
        }

        pendingSave = scope.launch {
            delay(500)
            scope.launch(Dispatchers.IO) {
                try {
                    DataService.sessions.saveTabs(tabsToSave)
                    DataService.sessions.setLastActiveSessionId(_state.value.activeTabId)
                } catch (e: Exception) {
                    Log.e("AuroraSession", "Failed to persist tab state", e)
                }
            }
        }
    }

    fun restoreSessions() {
        scope.launch {
            try {
                val savedTabs = withContext(Dispatchers.IO) {
                    DataService.sessions.restoreTabs().filter { !it.isPrivate }
                }
                val lastActiveId = withContext(Dispatchers.IO) {
                    DataService.sessions.lastActiveSessionId.firstOrNull()
                }

                if (savedTabs.isNotEmpty()) {
                    val cappedTabs = savedTabs.take(maxTabs)
                    val activeId = if (cappedTabs.any { it.id == lastActiveId }) lastActiveId else cappedTabs.firstOrNull()?.id
                    val restored = mutableListOf<TabSession>()
                    for (tabInfo in cappedTabs) {
                        val session = engine.createSession(tabInfo.isPrivate)
                        session.onNewSessionRequest = { url -> handleRequestWindow(url, tabInfo.isPrivate) }
                        val metadataService = session.createMetadataService(
                            metadataCache = DataService.metadataCache,
                            thumbnailCache = DataService.thumbnailCache
                        )
                        val thumbnailService = session.createThumbnailService(DataService.thumbnailCache)
                        val controller = BrowserController(
                            session = session,
                            historyRepo = DataService.history,
                            sessionRepo = DataService.sessions,
                            metadataService = metadataService,
                            thumbnailService = thumbnailService
                        )
                        controller.prefillState(tabInfo.url, tabInfo.title)

                        if (tabInfo.url.isNotBlank() && tabInfo.id == activeId) {
                            session.loadUrl(tabInfo.url)
                        }

                        val ts = TabSession(tabInfo.id, session, controller)
                        restored.add(ts)
                        tabThumbnailServices[tabInfo.id] = thumbnailService
                    }

                    _state.update {
                        it.copy(
                            tabs = restored,
                            activeTabId = activeId,
                            isRestored = true
                        )
                    }
                    updateSessionActivity()

                    // Setup auto-save collection for all restored tabs
                    restored.forEach { tabSession ->
                        val job = scope.launch {
                            tabSession.controller.state.collect {
                                saveCurrentTabsState()
                            }
                        }
                        tabCollectorJobs[tabSession.id] = job
                    }
                } else {
                    _state.update { it.copy(isRestored = true) }
                    createSession(isPrivate = false)
                }
            } catch (e: Exception) {
                Log.e("AuroraSession", "Failed to restore saved tabs", e)
                _state.update { it.copy(isRestored = true) }
                if (_state.value.tabs.isEmpty()) createSession(isPrivate = false)
            }
        }
    }

    private fun updateSessionActivity() {
        val snapshot = _state.value
        snapshot.tabs.forEach { tab ->
            val active = isForeground && tab.id == snapshot.activeTabId
            tab.session.setActive(active)
            tab.session.setFocused(active)
        }
    }

    fun closeInactiveTabs(keepCount: Int) {
        val snapshot = _state.value
        val inactiveTabs = snapshot.tabs.filter { it.id != snapshot.activeTabId }
        val closeCount = (snapshot.tabs.size - keepCount).coerceAtLeast(0).coerceAtMost(inactiveTabs.size)
        inactiveTabs.take(closeCount).forEach { tab ->
            Log.w("AuroraSession", "Closing inactive tab ${tab.id} under memory pressure")
            closeSession(tab.id)
        }
    }
}

@Composable
fun rememberSessionManager(): SessionManager {
    val context = LocalContext.current.applicationContext
    val maxTabs = remember(context) { maxTabsForDevice(context) }
    val engine = remember { EngineFactory.create(context) }
    val manager = remember(engine, maxTabs) { SessionManager(engine, maxTabs = maxTabs) }
    return manager
}

private fun maxTabsForDevice(context: Context): Int {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 5
    val info = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(info)
    return if (info.totalMem >= 4L * 1024L * 1024L * 1024L) 10 else 5
}