package com.aurora.browser.state

data class BrowserState(
    val currentUrl: String = "",
    val pageTitle: String = "",
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val errorMessage: String? = null,
    val isSecure: Boolean = false,
    val isFullScreen: Boolean = false,
    val securityInfo: String = "",
    val errorState: ErrorState = ErrorState.None,
    val lastFailedUrl: String = "",
    val downloadUrl: String? = null,
    val downloadFileName: String? = null,
    val downloadMimeType: String? = null,
    val savedDownloadFilePath: String? = null,
    val faviconVersion: Long = 0L
)
