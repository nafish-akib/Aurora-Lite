package com.aurora.ui

import android.content.Context
import com.aurora.engine.BrowserEngine

/**
 * WebView-backed engine selection, compiled into the module graph only when
 * the `:engine:webview` module is included (aurora.engine=webview).
 * The single/backed engine-selection point for the lite variant.
 */
object EngineFactoryProvider {
    fun create(context: Context): BrowserEngine =
        com.aurora.engine.webview.WebViewBrowserEngine(context.applicationContext)
}