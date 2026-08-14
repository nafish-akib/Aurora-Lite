package com.aurora.engine.webview

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.aurora.data.cache.MetadataCache
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.service.ThumbnailService
import com.aurora.data.service.WebsiteMetadataService
import com.aurora.engine.BrowserCallbacks
import com.aurora.engine.BrowserSession
import com.aurora.engine.FilePickerRequest
import com.aurora.engine.InputBridge
import com.aurora.engine.LoginStorage
import com.aurora.engine.PermissionRequest
import com.aurora.engine.SitePermissionsService
import com.aurora.engine.WebsiteMetadataServiceImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.ByteArrayInputStream

class WebViewBrowserSession(
    private val appContext: Context,
    private val settings: WebViewBrowserSettings,
    private val loginVault: WebViewLoginStorage?,
    override val isPrivate: Boolean
) : BrowserSession {

    override var onNewSessionRequest: ((url: String) -> BrowserSession?)? = null

    private val permissionsService = SitePermissionsService()
    private val _permissionRequests = MutableSharedFlow<PermissionRequest>(replay = 0)
    override val permissionRequests: SharedFlow<PermissionRequest> = _permissionRequests.asSharedFlow()

    private val _filePickerRequests = MutableSharedFlow<FilePickerRequest>(replay = 0)
    override val filePickerRequests: SharedFlow<FilePickerRequest> = _filePickerRequests.asSharedFlow()

    private val _linkContextRequests = MutableSharedFlow<String>(replay = 0)
    override val linkContextRequests: SharedFlow<String> = _linkContextRequests.asSharedFlow()

    private var callbacks: BrowserCallbacks? = null
    private var currentUrl: String = ""
    private var pageLoadFailed = false
    private var desktopEnabled = false
    private var uaOverrideActive = false
    private var systemUserAgent = ""
    private var desktopUserAgent = ""
    private var rendererCrashCount = 0
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private val blobBridge: WebViewBlobBridge by lazy {
        WebViewBlobBridge(appContext) { url, fileName, mimeType, savedFilePath ->
            callbacks?.onDownloadRequest(url, fileName, mimeType, 0, savedFilePath)
        }
    }

    var sslBypassHandler: ((url: String, primaryError: Int) -> Boolean)? = null

    private var webView: WebView? = null

    override val isOpen: Boolean
        get() = webView != null

    override fun setCallbacks(callbacks: BrowserCallbacks) {
        this.callbacks = callbacks
    }

    override fun close() {
        webView?.let { wv ->
            runCatching {
                if (wv.parent != null) (wv.parent as android.view.ViewGroup).removeView(wv)
                wv.destroy()
            }.onFailure { Log.w("AuroraWebView", "WebView destroy failed", it) }
        }
        webView = null
        customView?.let { customViewCallback?.onCustomViewHidden() }
        customView = null
        customViewCallback = null
    }

    override fun loadUrl(url: String) {
        val view = webView
        if (view != null) {
            applyAutoDesktopUa(url)
            applyLayerMode(url)
            view.loadUrl(url)
        } else {
            pendingUrl = url
        }
    }

    private var pendingUrl: String? = null

    override fun goBack() {
        webView?.goBack()
    }

    override fun goForward() {
        webView?.goForward()
    }

    override fun reload() {
        webView?.reload()
    }

    override fun stop() {
        webView?.stopLoading()
    }

    override fun setDesktopMode(enabled: Boolean) {
        if (!settings.userAgentValue.isNullOrEmpty()) return
        desktopEnabled = enabled
        uaOverrideActive = true
        val wv = webView ?: return
        wv.settings.userAgentString = if (enabled) desktopUserAgent else systemUserAgent
        wv.reload()
    }

    private fun applyAutoDesktopUa(url: String) {
        if (uaOverrideActive || desktopEnabled) return
        if (!settings.userAgentValue.isNullOrEmpty()) return
        val wv = webView ?: return
        val host = runCatching { Uri.parse(url).host?.lowercase() ?: "" }.getOrDefault("")
        val needsDesktop = DEFAULT_DESKTOP_UA_FOR_ALL || DESKTOP_REQUIRED_DOMAINS.any { host == it || host.endsWith(".$it") }
        wv.settings.userAgentString = if (needsDesktop) desktopUserAgent else systemUserAgent
    }

    private fun applyLayerMode(url: String) {
        val wv = webView ?: return
        val host = runCatching { Uri.parse(url).host?.lowercase() ?: "" }.getOrDefault("")
        val needsSoftware = SOFTWARE_RENDER_DOMAINS.any { host == it || host.endsWith(".$it") }
        wv.setLayerType(
            if (needsSoftware) View.LAYER_TYPE_SOFTWARE else View.LAYER_TYPE_HARDWARE,
            null
        )
    }

    override fun isDesktopMode(): Boolean = desktopEnabled

    override fun setActive(active: Boolean) {
        val wv = webView ?: return
        if (active) wv.onResume() else wv.onPause()
    }

    override fun setFocused(focused: Boolean) {
    }

    override fun findInPage(query: String) {
        webView?.findAllAsync(query)
    }

    override fun findNextInPage(forward: Boolean) {
        webView?.findNext(forward)
    }

    override fun clearFind() {
        webView?.clearMatches()
    }

    override fun openExternally(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        val resolved = appContext.packageManager.queryIntentActivities(intent, 0)
        if (resolved.isNotEmpty()) {
            appContext.startActivity(intent)
        } else {
            Log.w("AuroraWebView", "No external browser available on this TV — staying in WebView")
        }
    }

    override fun setTextZoom(zoom: Int) {
        webView?.settings?.textZoom = zoom
    }

    override fun getPermissionsService(): SitePermissionsService = permissionsService

    override fun getLoginStorage(): LoginStorage? = loginVault

    override fun createInputBridge(): InputBridge = WebViewInputBridge { webView }

    override fun createMetadataService(
        metadataCache: MetadataCache,
        thumbnailCache: ThumbnailCache
    ): WebsiteMetadataService {
        return WebsiteMetadataServiceImpl(metadataCache, thumbnailCache)
    }

    override fun createThumbnailService(thumbnailCache: ThumbnailCache): ThumbnailService {
        return WebViewThumbnailService(viewProvider = { webView }, cache = thumbnailCache)
    }

    override fun createView(context: Context): View {
        webView?.let { return it }
        val view = WebView(context)
        webView = view
        rendererCrashCount = 0
        configureView(view)
        return view
    }

    private fun configureView(view: WebView) {
        val webSettings = view.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.setSupportMultipleWindows(true)
        webSettings.setMediaPlaybackRequiresUserGesture(false)
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.loadsImagesAutomatically = true
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = false
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.setSaveFormData(false)

        webSettings.setGeolocationEnabled(true)

        systemUserAgent = WebSettings.getDefaultUserAgent(view.context)
        desktopUserAgent = settings.userAgentValue
            ?: DESKTOP_USER_AGENT
        webSettings.userAgentString = if (desktopEnabled) desktopUserAgent else systemUserAgent

        CookieManager.getInstance().setAcceptCookie(settings.cookiesAllowed)
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, settings.thirdPartyCookiesAllowed)

        webSettings.textZoom = settings.textZoom
        webSettings.setSafeBrowsingEnabled(true)

        view.overScrollMode = View.OVER_SCROLL_NEVER
        view.isHorizontalScrollBarEnabled = false
        view.isVerticalScrollBarEnabled = false
        view.requestFocus()
        Log.i("AuroraWebView", "WebView engine: $webViewVersion")

        view.webViewClient = pageClient
        view.webChromeClient = chromeClient
        view.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true)
        view.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            if (mimetype == "application/pdf") {
                runCatching {
                    view.context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                    )
                }
                return@setDownloadListener
            }
            callbacks?.onDownloadRequest(
                url,
                WebViewMappings.fileNameFrom(url, contentDisposition),
                mimetype,
                contentLength,
                null
            )
        }

        loginVault?.install(view)
        view.addJavascriptInterface(blobBridge, "AuroraBlob")
        pendingUrl?.let { applyAutoDesktopUa(it); applyLayerMode(it); view.loadUrl(it); pendingUrl = null }
    }

    private val pageClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val scheme = request.url.scheme?.lowercase() ?: ""
            if (scheme != "http" && scheme != "https" && scheme != "about" &&
                scheme != "data" && scheme != "blob" && scheme != "javascript"
            ) {
                if (request.isForMainFrame) {
                    when (scheme) {
                        "tel" -> {
                            runCatching {
                                view.context.startActivity(
                                    android.content.Intent(android.content.Intent.ACTION_DIAL, request.url)
                                )
                            }.onFailure { Log.w("AuroraWebView", "No dialer for: ${request.url}", it) }
                        }
                        else -> {
                            runCatching {
                                view.context.startActivity(
                                    android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
                                )
                            }.onFailure { Log.w("AuroraWebView", "Unhandled scheme: ${request.url}", it) }
                        }
                    }
                }
                return true
            }
            if (request.isForMainFrame) {
                applyAutoDesktopUa(request.url.toString())
                applyLayerMode(request.url.toString())
                currentUrl = request.url.toString()
                callbacks?.onUrlChange(currentUrl)
            }
            val path = request.url.path ?: ""
            val ext = path.substringAfterLast('.', "").lowercase()
            if (ext in DOWNLOADABLE_EXTENSIONS) {
                callbacks?.onDownloadRequest(
                    request.url.toString(),
                    WebViewMappings.fileNameFrom(request.url.toString(), null),
                    MIME_MAP[ext] ?: "application/octet-stream",
                    -1,
                    null
                )
                return true
            }
            return false
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            if (!settings.adBlockingEnabled) return null
            val host = request.url.host?.lowercase() ?: return null
            for (rule in settings.adBlockHosts) {
                if (host == rule || host.endsWith(".$rule")) {
                    Log.d("AuroraWebView", "Blocked: $host")
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                }
            }
            return null
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            pageLoadFailed = false
            val u = url ?: currentUrl
            currentUrl = u
            callbacks?.onPageStart(u)
            callbacks?.onSecurityChange(WebViewMappings.isSecureUrl(u))
        }

        override fun onPageFinished(view: WebView, url: String?) {
            callbacks?.onProgressChange(100)
            callbacks?.onPageFinish(currentUrl, !pageLoadFailed)
            loginVault?.let { it.lastCapturedUrl = currentUrl; it.injectCaptureScript(view) }
            view.evaluateJavascript(WebViewBlobBridge.INJECT_SCRIPT, null)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
            if (request?.isForMainFrame != true) return
            pageLoadFailed = true
            val code = error?.errorCode ?: WebViewClient.ERROR_UNKNOWN
            callbacks?.onLoadError(request.url.toString(), code, WebViewMappings.describeError(code))
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            if (!request.isForMainFrame) return
            pageLoadFailed = true
            callbacks?.onLoadError(request.url.toString(), errorResponse.statusCode, WebViewMappings.describeHttpError(errorResponse.statusCode))
        }

        override fun onReceivedSslError(view: WebView, handler: android.webkit.SslErrorHandler, error: android.net.http.SslError) {
            val url = error.url ?: currentUrl
            val bypass = sslBypassHandler?.invoke(url, error.primaryError) ?: settings.sslBypassEnabled
            if (bypass) {
                handler.proceed()
                return
            }
            pageLoadFailed = true
            handler.cancel()
            callbacks?.onSecurityChange(false)
            callbacks?.onLoadError(url, WebViewClient.ERROR_FAILED_SSL_HANDSHAKE, "SSL certificate error")
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            val message = if (detail.didCrash()) "Renderer process crashed" else "Renderer killed by system"
            Log.w("AuroraWebView", "onRenderProcessGone: $message")
            callbacks?.onError(message)
            rendererCrashCount++
            val lastUrl = currentUrl
            runCatching {
                if (view.parent != null) (view.parent as android.view.ViewGroup).removeView(view)
            }
            view.destroy()
            webView = null
            if (rendererCrashCount >= MAX_RENDERER_CRASHES) {
                Log.e("AuroraWebView", "Too many crashes ($rendererCrashCount); giving up")
                callbacks?.onFatalError("WebView renderer has crashed repeatedly")
            } else {
                pendingUrl = if (lastUrl.isNotBlank()) lastUrl else "about:blank"
            }
            return true
        }

        override fun onSafeBrowsingHit(view: WebView, request: WebResourceRequest, threatType: Int, callback: SafeBrowsingResponse) {
            Log.w("AuroraWebView", "Safe Browsing hit: type=$threatType for ${request.url}")
            callback.showInterstitial(true)
        }
    }

    private val chromeClient = object : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            callbacks?.onProgressChange(newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String?) {
            title?.let { callbacks?.onTitleChange(it) }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            customView = view
            customViewCallback = callback
            view?.keepScreenOn = true
            callbacks?.onFullScreenChange(true)
        }

        override fun onHideCustomView() {
            customView?.keepScreenOn = false
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
            callbacks?.onFullScreenChange(false)
        }

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            val url = resultMsg.data?.toString().orEmpty()
            if (!settings.popupsAllowed) {
                if (url.isNotBlank()) {
                    Log.d("AuroraWebView", "Popup blocked; routing to current session: $url")
                    view.loadUrl(url)
                }
                return false
            }
            val newSession = onNewSessionRequest?.invoke(url)
            if (newSession is WebViewBrowserSession) {
                val newWebView = newSession.createView(view.context) as WebView
                val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }
            return false
        }

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            val acceptTypes = fileChooserParams.acceptTypes?.filterNotNull()?.toList() ?: emptyList()
            val isMultiple = fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE
            _filePickerRequests.tryEmit(
                FilePickerRequest(
                    id = System.currentTimeMillis(),
                    mimeTypes = acceptTypes,
                    isMultiple = isMultiple,
                    isCapture = fileChooserParams.isCaptureEnabled,
                    onComplete = { uris ->
                        if (uris.isEmpty()) {
                            filePathCallback.onReceiveValue(null)
                        } else {
                            filePathCallback.onReceiveValue(uris.map { Uri.parse(it) }.toTypedArray())
                        }
                    },
                    onCancel = { filePathCallback.onReceiveValue(null) }
                )
            )
            return true
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            if (!settings.locationAllowed) {
                callback.invoke(origin, false, false)
                return
            }
            val domain = WebViewMappings.domainFromUrl(origin)
            val saved = permissionsService.getPermission(domain, SitePermissionsService.PERMISSION_LOCATION)
            if (saved != null) {
                callback.invoke(origin, saved, false)
                return
            }
            _permissionRequests.tryEmit(
                PermissionRequest(
                    id = System.currentTimeMillis(),
                    url = origin,
                    domain = domain,
                    permission = SitePermissionsService.PERMISSION_LOCATION,
                    description = SitePermissionsService.PERMISSION_LOCATION,
                    onGrant = {
                        permissionsService.setPermission(domain, SitePermissionsService.PERMISSION_LOCATION, true)
                        callback.invoke(origin, true, false)
                    },
                    onDeny = {
                        permissionsService.setPermission(domain, SitePermissionsService.PERMISSION_LOCATION, false)
                        callback.invoke(origin, false, false)
                    }
                )
            )
        }

        override fun onPermissionRequest(request: android.webkit.PermissionRequest) {
            val resources = request.resources.toList()
            val domain = WebViewMappings.extractDomain(request.origin.toString())
            val wantsCamera = resources.contains(android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            val wantsMic = resources.contains(android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE)

            if (!wantsCamera && !wantsMic) {
                request.grant(request.resources)
                return
            }

            val permKey = if (wantsMic) SitePermissionsService.PERMISSION_MICROPHONE else SitePermissionsService.PERMISSION_CAMERA
            val saved = permissionsService.getPermission(domain, permKey)
            if (saved != null) {
                if (saved) request.grant(request.resources) else request.deny()
                return
            }
            _permissionRequests.tryEmit(
                PermissionRequest(
                    id = System.currentTimeMillis(),
                    url = request.origin.toString(),
                    domain = domain,
                    permission = permKey,
                    description = permKey,
                    onGrant = {
                        permissionsService.setPermission(domain, permKey, true)
                        request.grant(request.resources)
                    },
                    onDeny = {
                        permissionsService.setPermission(domain, permKey, false)
                        request.deny()
                    }
                )
            )
        }

        override fun onJsAlert(view: WebView, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
            Log.d("AuroraWebView", "JS Alert [$url]: $message")
            return false
        }

        override fun onJsConfirm(view: WebView, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
            Log.d("AuroraWebView", "JS Confirm [$url]: $message")
            return false
        }

        override fun onJsPrompt(view: WebView, url: String?, message: String?, defaultValue: String?, result: android.webkit.JsPromptResult?): Boolean {
            Log.d("AuroraWebView", "JS Prompt [$url]: $message")
            return false
        }
    }

    companion object {
        private const val MAX_RENDERER_CRASHES = 3

        private const val DEFAULT_DESKTOP_UA_FOR_ALL = true

        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"

        private val SOFTWARE_RENDER_DOMAINS = setOf(
            "chatgpt.com", "openai.com", "claude.ai", "gemini.google.com",
            "copilot.microsoft.com", "perplexity.ai", "deepseek.com",
            "grok.com", "poe.com", "meta.ai", "mistral.ai", "you.com",
            "huggingface.co"
        )

        private val DESKTOP_REQUIRED_DOMAINS = setOf(
            "facebook.com", "fb.com", "fb.watch", "messenger.com", "m.me",
            "whatsapp.com", "instagram.com", "threads.net",
            "twitter.com", "x.com", "t.co",
            "reddit.com", "linkedin.com", "discord.com", "tiktok.com",
            "pinterest.com", "tumblr.com", "telegram.org", "web.telegram.org",
            "chatgpt.com", "openai.com", "claude.ai", "gemini.google.com",
            "copilot.microsoft.com", "perplexity.ai", "deepseek.com",
            "grok.com", "poe.com", "meta.ai", "mistral.ai", "you.com",
            "huggingface.co"
        )

        private val DOWNLOADABLE_EXTENSIONS = setOf(
            "apk", "rar", "zip", "7z", "tar", "gz", "bz2", "xz", "lz", "zst",
            "exe", "msi", "dmg", "pkg", "deb", "rpm", "appimage", "run", "sh", "bat", "cmd", "ps1",
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "mid", "midi",
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "m2ts",
            "iso", "img", "bin", "cue", "nrg",
            "pdf", "epub", "mobi", "azw3", "cbz", "cbr",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp",
            "torrent", "magnet",
            "psd", "ai", "eps", "svgz",
            "ttf", "otf", "woff", "woff2",
            "csv", "json", "xml", "yaml", "yml", "log", "sql", "db", "sqlite",
            "dll", "so", "dylib", "sys", "drv",
            "rom", "iso", "gba", "nds", "cia", "nsp", "xci", "wbfs", "wad", "wud",
            "sav", "bak", "tmp"
        )

        private val MIME_MAP = mapOf(
            "apk" to "application/vnd.android.package-archive",
            "rar" to "application/x-rar-compressed", "zip" to "application/zip",
            "7z" to "application/x-7z-compressed", "tar" to "application/x-tar",
            "gz" to "application/gzip", "bz2" to "application/x-bzip2",
            "xz" to "application/x-xz",
            "exe" to "application/x-msdownload", "msi" to "application/x-msi",
            "dmg" to "application/x-apple-diskimage", "pkg" to "application/x-xar",
            "deb" to "application/vnd.debian.binary-package",
            "mp3" to "audio/mpeg", "wav" to "audio/wav", "flac" to "audio/flac",
            "aac" to "audio/aac", "ogg" to "audio/ogg", "m4a" to "audio/mp4",
            "wma" to "audio/x-ms-wma", "opus" to "audio/opus",
            "mp4" to "video/mp4", "mkv" to "video/x-matroska",
            "avi" to "video/x-msvideo", "mov" to "video/quicktime",
            "wmv" to "video/x-ms-wmv", "flv" to "video/x-flv",
            "webm" to "video/webm", "3gp" to "video/3gpp",
            "iso" to "application/x-iso9660-image",
            "pdf" to "application/pdf", "epub" to "application/epub+zip",
            "doc" to "application/msword",
            "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls" to "application/vnd.ms-excel",
            "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "ppt" to "application/vnd.ms-powerpoint",
            "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "torrent" to "application/x-bittorrent",
            "psd" to "image/vnd.adobe.photoshop",
            "ttf" to "font/ttf", "otf" to "font/otf",
            "woff" to "font/woff", "woff2" to "font/woff2",
            "csv" to "text/csv", "json" to "application/json",
            "xml" to "application/xml",
            "dll" to "application/x-msdownload"
        )

        val webViewVersion: String
            get() = runCatching {
                val pkgInfo = android.webkit.WebView.getCurrentWebViewPackage()
                "${pkgInfo?.packageName ?: "Android System WebView"} ${pkgInfo?.versionName ?: "unknown"}"
            }.getOrDefault("Android System WebView (unknown)")
    }
}
