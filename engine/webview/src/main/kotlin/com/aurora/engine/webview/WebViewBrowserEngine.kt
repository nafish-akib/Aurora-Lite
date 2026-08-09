package com.aurora.engine.webview

import android.content.Context
import android.util.Log
import com.aurora.data.preferences.SessionPreferences
import com.aurora.engine.BrowserEngine
import com.aurora.engine.BrowserSession
import com.aurora.engine.BrowserSettings
import com.aurora.engine.LoginStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Lite engine backed by the OS-provided Android WebView (Chromium).
 * Crash isolation via WebViewClient.onRenderProcessGone — no separate process.
 */
class WebViewBrowserEngine(context: Context) : BrowserEngine {

    private val appContext = context.applicationContext
    private val webViewSettings = WebViewBrowserSettings()

    private val _runtimeReady = MutableStateFlow(true)
    private val loginVault = WebViewLoginStorage(appContext)

    init {
        if (0 != (appContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(true)
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val prefs = SessionPreferences(appContext)
                prefs.userAgent.firstOrNull()?.let { webViewSettings.userAgent = it }
                prefs.allowPopups.firstOrNull()?.let { webViewSettings.allowPopups = it }
                prefs.allowCookies.firstOrNull()?.let { webViewSettings.allowCookies = it }
                prefs.allowLocation.firstOrNull()?.let { webViewSettings.allowLocation = it }
                prefs.allowNotifications.firstOrNull()?.let { webViewSettings.allowNotifications = it }
            } catch (e: Exception) {
                Log.w("AuroraWebView", "Preferences load failed", e)
            }
        }
    }

    override fun createSession(): BrowserSession {
        return createSession(isPrivate = false)
    }

    override fun createSession(isPrivate: Boolean): BrowserSession {
        return WebViewBrowserSession(
            appContext = appContext,
            settings = webViewSettings,
            loginVault = loginVault.takeUnless { isPrivate },
            isPrivate = isPrivate
        )
    }

    override fun getSettings(): BrowserSettings = webViewSettings

    override fun getLoginStorage(): LoginStorage = loginVault

    override fun getEngineName(): String = "Android System WebView ${WebViewBrowserSession.webViewVersion}"

    override fun runtimeReady(): StateFlow<Boolean> = _runtimeReady
}