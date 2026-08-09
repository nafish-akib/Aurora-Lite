package com.aurora.data.repository

import com.aurora.data.model.BrowserSession
import com.aurora.data.model.HistoryEntry
import com.aurora.data.model.TabInfo
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun addSession(url: String, title: String)
    suspend fun getRecentSessions(limit: Int = 10): List<BrowserSession>
    suspend fun removeSession(id: Long)
    suspend fun clear()

    // Persistent open tabs
    suspend fun saveTabs(tabs: List<TabInfo>)
    suspend fun restoreTabs(): List<TabInfo>
    suspend fun clearOpenTabs()

    // Persistent recently closed tabs
    suspend fun saveRecentlyClosed(entries: List<HistoryEntry>)
    suspend fun getRecentlyClosed(limit: Int = 10): List<HistoryEntry>

    // Preferences
    val lastActiveSessionId: Flow<String?>
    suspend fun setLastActiveSessionId(id: String?)
    val startupBehavior: Flow<String>
    suspend fun setStartupBehavior(behavior: String)
    val homepageUrl: Flow<String>
    suspend fun setHomepageUrl(url: String)
    val userAgent: Flow<String?>
    suspend fun setUserAgent(ua: String?)
    val allowJavaScript: Flow<Boolean>
    suspend fun setAllowJavaScript(allow: Boolean)
    val allowCookies: Flow<Boolean>
    suspend fun setAllowCookies(allow: Boolean)
    val allowPopups: Flow<Boolean>
    suspend fun setAllowPopups(allow: Boolean)
    val allowLocation: Flow<Boolean>
    suspend fun setAllowLocation(allow: Boolean)
    val allowNotifications: Flow<Boolean>
    suspend fun setAllowNotifications(allow: Boolean)
    val defaultSearchEngine: Flow<String>
    suspend fun setDefaultSearchEngine(engine: String)
}
