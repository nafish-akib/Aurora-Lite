package com.aurora.data.repository

import com.aurora.data.model.BrowserSession
import com.aurora.data.model.HistoryEntry
import com.aurora.data.model.TabInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemorySessionRepository : SessionRepository {

    private val sessions = mutableListOf<BrowserSession>()
    private var nextId = 1L
    private val mutex = Mutex()

    private val openTabs = mutableListOf<TabInfo>()
    private val recentlyClosed = mutableListOf<HistoryEntry>()
    private val _lastActiveSessionId = MutableStateFlow<String?>(null)
    private val _startupBehavior = MutableStateFlow("RESTORE_LAST_SESSION")
    private val _homepageUrl = MutableStateFlow("https://wikipedia.org/wiki/living-glass")
    private val _userAgent = MutableStateFlow<String?>(null)
    private val _allowJavaScript = MutableStateFlow(true)
    private val _allowCookies = MutableStateFlow(true)
    private val _allowPopups = MutableStateFlow(false)
    private val _allowLocation = MutableStateFlow(true)
    private val _allowNotifications = MutableStateFlow(true)
    private val _defaultSearchEngine = MutableStateFlow("Google")

    override suspend fun addSession(url: String, title: String) {
        mutex.withLock {
            sessions.add(BrowserSession(id = nextId++, url = url, title = title))
            if (sessions.size > 50) {
                sessions.removeAt(0)
            }
        }
    }

    override suspend fun getRecentSessions(limit: Int): List<BrowserSession> {
        mutex.withLock {
            return sessions.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override suspend fun removeSession(id: Long) {
        mutex.withLock { sessions.removeAll { it.id == id } }
    }

    override suspend fun clear() {
        mutex.withLock { sessions.clear() }
    }

    override suspend fun saveTabs(tabs: List<TabInfo>) {
        mutex.withLock {
            openTabs.clear()
            openTabs.addAll(tabs)
        }
    }

    override suspend fun restoreTabs(): List<TabInfo> {
        mutex.withLock {
            return openTabs.toList()
        }
    }

    override suspend fun clearOpenTabs() {
        mutex.withLock {
            openTabs.clear()
        }
    }

    override suspend fun saveRecentlyClosed(entries: List<HistoryEntry>) {
        mutex.withLock {
            recentlyClosed.clear()
            recentlyClosed.addAll(entries)
        }
    }

    override suspend fun getRecentlyClosed(limit: Int): List<HistoryEntry> {
        mutex.withLock {
            return recentlyClosed.take(limit)
        }
    }

    override val lastActiveSessionId: Flow<String?> = _lastActiveSessionId.asStateFlow()

    override suspend fun setLastActiveSessionId(id: String?) {
        _lastActiveSessionId.value = id
    }

    override val startupBehavior: Flow<String> = _startupBehavior.asStateFlow()

    override suspend fun setStartupBehavior(behavior: String) {
        _startupBehavior.value = behavior
    }

    override val homepageUrl: Flow<String> = _homepageUrl.asStateFlow()

    override suspend fun setHomepageUrl(url: String) {
        _homepageUrl.value = url
    }

    override val userAgent: Flow<String?> = _userAgent.asStateFlow()

    override suspend fun setUserAgent(ua: String?) {
        _userAgent.value = ua
    }

    override val allowJavaScript: Flow<Boolean> = _allowJavaScript.asStateFlow()

    override suspend fun setAllowJavaScript(allow: Boolean) {
        _allowJavaScript.value = allow
    }

    override val allowCookies: Flow<Boolean> = _allowCookies.asStateFlow()

    override suspend fun setAllowCookies(allow: Boolean) {
        _allowCookies.value = allow
    }

    override val allowPopups: Flow<Boolean> = _allowPopups.asStateFlow()

    override suspend fun setAllowPopups(allow: Boolean) {
        _allowPopups.value = allow
    }

    override val allowLocation: Flow<Boolean> = _allowLocation.asStateFlow()

    override suspend fun setAllowLocation(allow: Boolean) {
        _allowLocation.value = allow
    }

    override val allowNotifications: Flow<Boolean> = _allowNotifications.asStateFlow()

    override suspend fun setAllowNotifications(allow: Boolean) {
        _allowNotifications.value = allow
    }

    override val defaultSearchEngine: Flow<String> = _defaultSearchEngine.asStateFlow()

    override suspend fun setDefaultSearchEngine(engine: String) {
        _defaultSearchEngine.value = engine
    }
}
