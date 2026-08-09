package com.aurora.engine

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface BrowserEngine {
    /** Optional engine bootstrap (e.g. Gecko runtime pre-init). Default no-op. */
    fun initialize(context: Context) = Unit

    /** Emits true once the engine can serve sessions (Gecko runtime warm-up). */
    fun runtimeReady(): StateFlow<Boolean>

    fun createSession(): BrowserSession
    fun createSession(isPrivate: Boolean): BrowserSession
    fun getSettings(): BrowserSettings

    /** Shared password vault; sessions capture into it. */
    fun getLoginStorage(): LoginStorage

    /** Display name used by the About/Compositor UI. */
    fun getEngineName(): String
}