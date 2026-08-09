package com.aurora.ui

import android.content.Context
import com.aurora.engine.BrowserEngine

/**
 * Engine metadata for the WebView-only Lite build.
 */
object AuroraEngineConfig {
    val isWebView: Boolean get() = true
    const val displayName = "Android System WebView"
}

object EngineFactory {
    fun create(context: Context): BrowserEngine = EngineFactoryProvider.create(context)
}