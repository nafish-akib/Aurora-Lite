package com.aurora.engine

import android.content.Context
import android.view.View
import com.aurora.data.cache.MetadataCache
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.service.ThumbnailService
import com.aurora.data.service.WebsiteMetadataService
import kotlinx.coroutines.flow.SharedFlow

interface BrowserSession {
    val isOpen: Boolean
    val isPrivate: Boolean

    fun loadUrl(url: String)
    fun goBack()
    fun goForward()
    fun reload()
    fun stop()

    fun setDesktopMode(enabled: Boolean)
    fun isDesktopMode(): Boolean

    fun setCallbacks(callbacks: BrowserCallbacks)
    fun close()

    // --- lifecycle hints (tab manager / view attach) ---

    fun setActive(active: Boolean)
    fun setFocused(focused: Boolean)

    // --- find-in-page ---

    fun findInPage(query: String)
    fun findNextInPage(forward: Boolean)
    fun clearFind()

    // --- capability streams consumed by the host UI ---

    val permissionRequests: SharedFlow<PermissionRequest>
    val filePickerRequests: SharedFlow<FilePickerRequest>
    val linkContextRequests: SharedFlow<String>

    /** Persistent per-site permission store shared between engine callbacks and the UI. */
    fun getPermissionsService(): SitePermissionsService

    /**
     * Popup / window-open contract. The host (tab manager) assigns a factory;
     * the engine invokes it when a new window session is required and uses the
     * returned [BrowserSession] as the popup target. Null return = popup rejected.
     */
    var onNewSessionRequest: ((url: String) -> BrowserSession?)?

    /** Password vault used by this engine's capture mechanism. */
    fun getLoginStorage(): LoginStorage?

    /** View host: the engine supplies the Android view; the UI hosts it in a single Composable. */
    fun createView(context: Context): View

    /** Input bridge for TV remote / cursor injection on the view returned by [createView]. */
    fun createInputBridge(): InputBridge?

    // --- services bound to :data caches ---

    fun createMetadataService(
        metadataCache: MetadataCache,
        thumbnailCache: ThumbnailCache
    ): WebsiteMetadataService

    fun createThumbnailService(thumbnailCache: ThumbnailCache): ThumbnailService

    fun openExternally(url: String) = Unit
    fun setTextZoom(zoom: Int) = Unit
}
