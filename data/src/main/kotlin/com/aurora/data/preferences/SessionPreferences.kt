package com.aurora.data.preferences

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aurora.data.model.BrowserSession
import com.aurora.data.model.HistoryEntry
import com.aurora.data.model.TabInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aurora_preferences")

class SessionPreferences(private val context: Context) {

    companion object {
        private val KEY_LAST_ACTIVE_SESSION_ID = stringPreferencesKey("last_active_session_id")
        private val KEY_STARTUP_BEHAVIOR = stringPreferencesKey("startup_behavior")
        private val KEY_HOMEPAGE_URL = stringPreferencesKey("homepage_url")
        private val KEY_USER_AGENT = stringPreferencesKey("user_agent")
        private val KEY_ALLOW_JAVASCRIPT = booleanPreferencesKey("allow_javascript")
        private val KEY_ALLOW_COOKIES = booleanPreferencesKey("allow_cookies")
        private val KEY_DEFAULT_SEARCH_ENGINE = stringPreferencesKey("default_search_engine")
        private val KEY_ALLOW_POPUPS = booleanPreferencesKey("allow_popups")
        private val KEY_ALLOW_LOCATION = booleanPreferencesKey("allow_location")
        private val KEY_ALLOW_NOTIFICATIONS = booleanPreferencesKey("allow_notifications")
        private val KEY_OPEN_TABS_JSON = stringPreferencesKey("open_tabs_json")
        private val KEY_RECENT_SESSIONS_JSON = stringPreferencesKey("recent_sessions_json")
        private val KEY_RECENTLY_CLOSED_JSON = stringPreferencesKey("recently_closed_json")
        private val KEY_BENCHMARK_MODE = booleanPreferencesKey("benchmark_mode")
        private val KEY_DOWNLOAD_PATH = stringPreferencesKey("active_download_path")
        private val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
        private val KEY_THEME_NAME = stringPreferencesKey("theme_name")
        private val KEY_PROFILE_NAME = stringPreferencesKey("profile_name")
    }

    val accentColor: Flow<String> = context.dataStore.data.map { it[KEY_ACCENT_COLOR] ?: "#4DA3FF" }
    val themeName: Flow<String> = context.dataStore.data.map { it[KEY_THEME_NAME] ?: "Aurora Dark" }

    suspend fun setAccentColor(color: String) { context.dataStore.edit { it[KEY_ACCENT_COLOR] = color } }
    suspend fun setThemeName(name: String) { context.dataStore.edit { it[KEY_THEME_NAME] = name } }

    val profileName: Flow<String> = context.dataStore.data.map { it[KEY_PROFILE_NAME] ?: "" }
    suspend fun setProfileName(name: String) { context.dataStore.edit { it[KEY_PROFILE_NAME] = name } }

    // --- Preferences ---

    val benchmarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_BENCHMARK_MODE] ?: false }

    suspend fun setBenchmarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BENCHMARK_MODE] = enabled }
    }

    private val _cachedDownloadPath = java.util.concurrent.atomic.AtomicReference<String?>(null)

    init {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val saved = context.dataStore.data.firstOrNull()?.get(KEY_DOWNLOAD_PATH)
            if (saved != null && File(saved).exists()) {
                _cachedDownloadPath.set(saved)
            }
        }
    }

    fun internalDownloadPath(): String {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val dir = if (externalDir != null) {
            File(externalDir, "Aurora")
        } else {
            File(context.filesDir, "Downloads/Aurora")
        }
        if (!dir.exists()) {
            val created = dir.mkdirs()
            android.util.Log.d("AuroraDL", "Download dir created=$created path=${dir.absolutePath}")
        }
        return dir.absolutePath
    }

    fun activeDownloadPath(): String {
        return _cachedDownloadPath.get() ?: internalDownloadPath()
    }

    fun setDownloadPath(absolutePath: String) {
        _cachedDownloadPath.set(absolutePath)
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            context.dataStore.edit { it[KEY_DOWNLOAD_PATH] = absolutePath }
        }
    }

    val lastActiveSessionId: Flow<String?> = context.dataStore.data.map { it[KEY_LAST_ACTIVE_SESSION_ID] }

    suspend fun setLastActiveSessionId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[KEY_LAST_ACTIVE_SESSION_ID] = id
            else prefs.remove(KEY_LAST_ACTIVE_SESSION_ID)
        }
    }

    val startupBehavior: Flow<String> = context.dataStore.data.map { it[KEY_STARTUP_BEHAVIOR] ?: "RESTORE_LAST_SESSION" }

    suspend fun setStartupBehavior(behavior: String) {
        context.dataStore.edit { it[KEY_STARTUP_BEHAVIOR] = behavior }
    }

    val homepageUrl: Flow<String> = context.dataStore.data.map { it[KEY_HOMEPAGE_URL] ?: "https://wikipedia.org/wiki/living-glass" }

    suspend fun setHomepageUrl(url: String) {
        context.dataStore.edit { it[KEY_HOMEPAGE_URL] = url }
    }

    val userAgent: Flow<String?> = context.dataStore.data.map { it[KEY_USER_AGENT] }

    suspend fun setUserAgent(ua: String?) {
        context.dataStore.edit { prefs ->
            if (ua != null) prefs[KEY_USER_AGENT] = ua
            else prefs.remove(KEY_USER_AGENT)
        }
    }

    val allowJavaScript: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_JAVASCRIPT] ?: true }

    suspend fun setAllowJavaScript(allow: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_JAVASCRIPT] = allow }
    }

    val allowCookies: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_COOKIES] ?: true }

    suspend fun setAllowCookies(allow: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_COOKIES] = allow }
    }

    val allowPopups: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_POPUPS] ?: true }

    suspend fun setAllowPopups(allow: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_POPUPS] = allow }
    }

    val allowLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_LOCATION] ?: true }

    suspend fun setAllowLocation(allow: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_LOCATION] = allow }
    }

    val allowNotifications: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_NOTIFICATIONS] ?: true }

    suspend fun setAllowNotifications(allow: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_NOTIFICATIONS] = allow }
    }

    val defaultSearchEngine: Flow<String> = context.dataStore.data.map { it[KEY_DEFAULT_SEARCH_ENGINE] ?: "Google" }

    suspend fun setDefaultSearchEngine(engine: String) {
        context.dataStore.edit { it[KEY_DEFAULT_SEARCH_ENGINE] = engine }
    }

    // --- Tab Persistence (JSON serialized) ---

    suspend fun saveTabs(tabs: List<TabInfo>) {
        val json = JSONArray()
        tabs.forEach { tab ->
            json.put(JSONObject().apply {
                put("id", tab.id)
                put("url", tab.url)
                put("title", tab.title)
                put("isPrivate", tab.isPrivate)
                put("isPinned", tab.isPinned)
                put("createdAt", tab.createdAt)
                put("lastVisited", tab.lastVisited)
                put("scrollPosition", tab.scrollPosition)
                put("zoomLevel", tab.zoomLevel.toDouble())
                put("readerModeEnabled", tab.readerModeEnabled)
                put("tabOrder", tab.tabOrder)
            })
        }
        context.dataStore.edit { it[KEY_OPEN_TABS_JSON] = json.toString() }
    }

    suspend fun restoreTabs(): List<TabInfo> {
        return try {
            val jsonStr = context.dataStore.data.firstOrNull()?.get(KEY_OPEN_TABS_JSON) ?: return emptyList()
            if (jsonStr.isBlank()) return emptyList()
            val json = JSONArray(jsonStr)
            val tabs = mutableListOf<TabInfo>()
            for (i in 0 until json.length()) {
                try {
                    val obj = json.getJSONObject(i)
                    tabs.add(TabInfo(
                        id = obj.getString("id"),
                        url = obj.optString("url", ""),
                        title = obj.optString("title", ""),
                        isPrivate = obj.optBoolean("isPrivate", false),
                        isPinned = obj.optBoolean("isPinned", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastVisited = obj.optLong("lastVisited", System.currentTimeMillis()),
                        scrollPosition = obj.optInt("scrollPosition", 0),
                        zoomLevel = obj.optDouble("zoomLevel", 1.0).toFloat(),
                        readerModeEnabled = obj.optBoolean("readerModeEnabled", false),
                        tabOrder = obj.optInt("tabOrder", i)
                    ))
                } catch (_: Exception) {
                    // Skip malformed tab entry
                }
            }
            tabs
        } catch (e: Exception) {
            android.util.Log.e("AuroraSession", "Failed to restore tabs, clearing corrupted data", e)
            try { clearOpenTabs() } catch (_: Exception) {}
            emptyList()
        }
    }

    suspend fun clearOpenTabs() {
        context.dataStore.edit { it.remove(KEY_OPEN_TABS_JSON) }
    }

    // --- Session (history) Persistence (JSON serialized) ---

    suspend fun addSession(url: String, title: String) {
        val existing = getRecentSessionsInternal()
        val newList = listOf(BrowserSession(url = url, title = title)) + existing
        saveSessionsInternal(newList.take(50))
    }

    suspend fun getRecentSessions(limit: Int = 10): List<BrowserSession> {
        return getRecentSessionsInternal().take(limit)
    }

    suspend fun removeSession(id: Long) {
        val existing = getRecentSessionsInternal()
        saveSessionsInternal(existing.filter { it.id != id })
    }

    suspend fun clearSessions() {
        context.dataStore.edit { it.remove(KEY_RECENT_SESSIONS_JSON) }
    }

    private suspend fun getRecentSessionsInternal(): List<BrowserSession> {
        return try {
            val jsonStr = context.dataStore.data.firstOrNull()?.get(KEY_RECENT_SESSIONS_JSON) ?: return emptyList()
            if (jsonStr.isBlank()) return emptyList()
            val json = JSONArray(jsonStr)
            val sessions = mutableListOf<BrowserSession>()
            for (i in 0 until json.length()) {
                try {
                    val obj = json.getJSONObject(i)
                    sessions.add(BrowserSession(
                        id = obj.optLong("id", 0L),
                        url = obj.optString("url", ""),
                        title = obj.optString("title", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    ))
                } catch (_: Exception) {
                    // Skip malformed session entry
                }
            }
            sessions
        } catch (e: Exception) {
            android.util.Log.e("AuroraSession", "Failed to load sessions, clearing corrupted data", e)
            try { clearSessions() } catch (_: Exception) {}
            emptyList()
        }
    }

    private suspend fun saveSessionsInternal(sessions: List<BrowserSession>) {
        val json = JSONArray()
        var nextId = 1L
        sessions.forEach { s ->
            json.put(JSONObject().apply {
                put("id", if (s.id != 0L) s.id else nextId++)
                put("url", s.url)
                put("title", s.title)
                put("timestamp", s.timestamp)
            })
        }
        context.dataStore.edit { it[KEY_RECENT_SESSIONS_JSON] = json.toString() }
    }

    // --- Recently Closed Persistence ---

    suspend fun saveRecentlyClosed(entries: List<HistoryEntry>) {
        val json = JSONArray()
        entries.take(20).forEach { entry ->
            json.put(JSONObject().apply {
                put("id", entry.id)
                put("url", entry.url)
                put("title", entry.title)
                put("timestamp", entry.timestamp)
                put("favicon", entry.favicon)
                put("sessionId", entry.sessionId ?: "")
            })
        }
        context.dataStore.edit { it[KEY_RECENTLY_CLOSED_JSON] = json.toString() }
    }

    suspend fun getRecentlyClosed(limit: Int = 10): List<HistoryEntry> {
        return try {
            val jsonStr = context.dataStore.data.firstOrNull()?.get(KEY_RECENTLY_CLOSED_JSON) ?: return emptyList()
            if (jsonStr.isBlank()) return emptyList()
            val json = JSONArray(jsonStr)
            val list = mutableListOf<HistoryEntry>()
            for (i in 0 until json.length()) {
                try {
                    val obj = json.getJSONObject(i)
                    list.add(HistoryEntry(
                        id = obj.optLong("id", 0L),
                        url = obj.optString("url", ""),
                        title = obj.optString("title", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        favicon = obj.optString("favicon", ""),
                        sessionId = obj.optString("sessionId", "").ifEmpty { null }
                    ))
                } catch (_: Exception) {}
            }
            list.take(limit)
        } catch (e: Exception) {
            android.util.Log.e("AuroraSession", "Failed to load recently closed tabs", e)
            emptyList()
        }
    }
}
