package com.aurora.browser.controller

import com.aurora.browser.state.BrowserState
import com.aurora.browser.state.ErrorState
import com.aurora.data.repository.HistoryRepository
import com.aurora.data.repository.SessionRepository
import com.aurora.data.service.ThumbnailService
import com.aurora.data.service.WebsiteMetadataService
import com.aurora.engine.BrowserCallbacks
import com.aurora.engine.BrowserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserController(
    private val session: BrowserSession,
    private val historyRepo: HistoryRepository? = null,
    private val sessionRepo: SessionRepository? = null,
    private val metadataService: WebsiteMetadataService? = null,
    private val thumbnailService: ThumbnailService? = null
) : BrowserCallbacks {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    init {
        session.setCallbacks(this)
    }

    fun loadUrl(input: String) {
        val url = input.trim()
        if (url.isBlank()) return
        _state.update {
            it.copy(currentUrl = url, isLoading = true, loadingProgress = 0, errorMessage = null)
        }
        session.loadUrl(url)
    }

    fun prefillState(url: String, title: String) {
        _state.update {
            it.copy(currentUrl = url, pageTitle = title)
        }
    }

    fun goBack() { session.goBack() }
    fun goForward() { session.goForward() }
    fun reload() { session.reload() }
    fun stop() { session.stop() }
    fun setDesktopMode(enabled: Boolean) { session.setDesktopMode(enabled) }
    fun isDesktopMode(): Boolean = session.isDesktopMode()

    override fun onPageStart(url: String) {
        _state.update { it.copy(isLoading = true, loadingProgress = 0, errorMessage = null, errorState = ErrorState.None) }
        if (!session.isPrivate) metadataService?.onPageStart(url)
    }

    override fun onPageFinish(url: String, success: Boolean) {
        val title = _state.value.pageTitle
        _state.update { it.copy(isLoading = false, loadingProgress = 100) }
        if (session.isPrivate || !success) return
        metadataService?.onPageFinished(url, true)
        scope.launch { metadataService?.collectAfterLoad(url) }
        thumbnailService?.scheduleCapture(url)
        scope.launch {
            historyRepo?.addEntry(url, title)
            sessionRepo?.addSession(url, title)
        }
    }

    override fun onProgressChange(progress: Int) {
        _state.update { it.copy(loadingProgress = progress) }
    }

    override fun onTitleChange(title: String) {
        _state.update { it.copy(pageTitle = title) }
        metadataService?.onTitleChanged(_state.value.currentUrl, title)
    }

    override fun onUrlChange(url: String) {
        _state.update { it.copy(currentUrl = url) }
    }

    override fun onCanGoBackChanged(canGoBack: Boolean) {
        _state.update { it.copy(canGoBack = canGoBack) }
    }

    override fun onCanGoForwardChanged(canGoForward: Boolean) {
        _state.update { it.copy(canGoForward = canGoForward) }
    }

    override fun onError(error: String) {
        _state.update { it.copy(isLoading = false, errorMessage = error) }
        metadataService?.onPageFinished(_state.value.currentUrl, false)
    }

    override fun onSecurityChange(isSecure: Boolean, securityInfo: String) {
        _state.update { it.copy(isSecure = isSecure, securityInfo = securityInfo) }
    }

    override fun onFullScreenChange(isFullScreen: Boolean) {
        _state.update { it.copy(isFullScreen = isFullScreen) }
    }

    override fun onLoadError(url: String, errorCode: Int, description: String) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = "Error $errorCode: $description",
                errorState = ErrorState.fromEngineError(errorCode, description, url),
                lastFailedUrl = url
            )
        }
    }

    override fun onDownloadRequest(url: String, fileName: String, mimeType: String, contentLength: Long, savedFilePath: String?) {
        _state.update {
            it.copy(
                isLoading = false,
                downloadUrl = url,
                downloadFileName = fileName,
                downloadMimeType = mimeType,
                savedDownloadFilePath = savedFilePath
            )
        }
    }

    override fun onFaviconChange(url: String) {
        _state.update { it.copy(faviconVersion = System.currentTimeMillis()) }
    }

    fun clearDownloadRequest() {
        _state.update { it.copy(downloadUrl = null, downloadFileName = null, downloadMimeType = null, savedDownloadFilePath = null) }
    }

    fun close() {
        scope.cancel()
        metadataService?.close()
        thumbnailService?.cancel()
    }
}
