package com.aurora.engine

interface BrowserCallbacks {
    fun onPageStart(url: String)
    fun onPageFinish(url: String, success: Boolean = true)
    fun onProgressChange(progress: Int)
    fun onTitleChange(title: String)
    fun onUrlChange(url: String)
    fun onCanGoBackChanged(canGoBack: Boolean)
    fun onCanGoForwardChanged(canGoForward: Boolean)
    fun onError(error: String)
    fun onSecurityChange(isSecure: Boolean, securityInfo: String = "")
    fun onFullScreenChange(isFullScreen: Boolean)
    fun onNewSession(url: String): Boolean = false
    fun onLoadError(url: String, errorCode: Int, description: String)
    fun onDownloadRequest(url: String, fileName: String, mimeType: String, contentLength: Long, savedFilePath: String? = null) = Unit
    fun onFaviconChange(url: String) = Unit
    fun onFatalError(message: String) = Unit
}
