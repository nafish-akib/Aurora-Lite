package com.aurora.data.repository

import com.aurora.data.model.BrowserSession
import com.aurora.data.model.HistoryEntry
import com.aurora.data.model.TabInfo
import com.aurora.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.Flow

class PersistentSessionRepository(
    private val preferences: SessionPreferences
) : SessionRepository {

    override suspend fun addSession(url: String, title: String) {
        preferences.addSession(url, title)
    }

    override suspend fun getRecentSessions(limit: Int): List<BrowserSession> {
        return preferences.getRecentSessions(limit)
    }

    override suspend fun removeSession(id: Long) {
        preferences.removeSession(id)
    }

    override suspend fun clear() {
        preferences.clearSessions()
    }

    override suspend fun saveTabs(tabs: List<TabInfo>) {
        preferences.saveTabs(tabs)
    }

    override suspend fun restoreTabs(): List<TabInfo> {
        return preferences.restoreTabs()
    }

    override suspend fun clearOpenTabs() {
        preferences.clearOpenTabs()
    }

    override suspend fun saveRecentlyClosed(entries: List<HistoryEntry>) {
        preferences.saveRecentlyClosed(entries)
    }

    override suspend fun getRecentlyClosed(limit: Int): List<HistoryEntry> {
        return preferences.getRecentlyClosed(limit)
    }

    override val lastActiveSessionId: Flow<String?> = preferences.lastActiveSessionId

    override suspend fun setLastActiveSessionId(id: String?) {
        preferences.setLastActiveSessionId(id)
    }

    override val startupBehavior: Flow<String> = preferences.startupBehavior

    override suspend fun setStartupBehavior(behavior: String) {
        preferences.setStartupBehavior(behavior)
    }

    override val homepageUrl: Flow<String> = preferences.homepageUrl

    override suspend fun setHomepageUrl(url: String) {
        preferences.setHomepageUrl(url)
    }

    override val userAgent: Flow<String?> = preferences.userAgent

    override suspend fun setUserAgent(ua: String?) {
        preferences.setUserAgent(ua)
    }

    override val allowJavaScript: Flow<Boolean> = preferences.allowJavaScript

    override suspend fun setAllowJavaScript(allow: Boolean) {
        preferences.setAllowJavaScript(allow)
    }

    override val allowCookies: Flow<Boolean> = preferences.allowCookies

    override suspend fun setAllowCookies(allow: Boolean) {
        preferences.setAllowCookies(allow)
    }

    override val allowPopups: Flow<Boolean> = preferences.allowPopups

    override suspend fun setAllowPopups(allow: Boolean) {
        preferences.setAllowPopups(allow)
    }

    override val allowLocation: Flow<Boolean> = preferences.allowLocation

    override suspend fun setAllowLocation(allow: Boolean) {
        preferences.setAllowLocation(allow)
    }

    override val allowNotifications: Flow<Boolean> = preferences.allowNotifications

    override suspend fun setAllowNotifications(allow: Boolean) {
        preferences.setAllowNotifications(allow)
    }

    override val defaultSearchEngine: Flow<String> = preferences.defaultSearchEngine

    override suspend fun setDefaultSearchEngine(engine: String) {
        preferences.setDefaultSearchEngine(engine)
    }
}
