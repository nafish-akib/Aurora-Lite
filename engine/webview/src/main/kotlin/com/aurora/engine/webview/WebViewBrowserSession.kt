package com.aurora.engine.webview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.ServiceWorkerController
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.aurora.data.DataService
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class WebViewBrowserSession(
    private val appContext: Context,
    private val settings: WebViewBrowserSettings,
    private val loginVault: WebViewLoginStorage?,
    override val isPrivate: Boolean
) : BrowserSession {

    override var onNewSessionRequest: ((url: String) -> BrowserSession?)? = null

    private val permissionsService = SitePermissionsService()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
    private var lastCachedFaviconDomain = ""
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
        scope.cancel()
        customView?.let { customViewCallback?.onCustomViewHidden() }
        customView = null
        customViewCallback = null
    }

    override fun loadUrl(url: String) {
        val view = webView
        if (view == null) {
            pendingUrl = url
            return
        }
        applyAutoDesktopUa(url)
        if (view.width > 0 && view.height > 0) {
            view.loadUrl(url)
        } else {
            Log.d("AuroraDiag", "loadUrl deferred (view=${view.width}x${view.height}) url=$url")
            pendingUrl = url
            deferLoadUntilSized(view)
        }
    }

    private fun deferLoadUntilSized(view: WebView) {
        view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int) {
                if (r - l > 0 && b - t > 0) {
                    v.removeOnLayoutChangeListener(this)
                    pendingUrl?.let { pending ->
                        Log.d("AuroraDiag", "deferred load now sized=${r - l}x${b - t} url=$pending")
                        applyAutoDesktopUa(pending)
                        view.loadUrl(pending)
                        pendingUrl = null
                    }
                }
            }
        })
        view.postDelayed({
            pendingUrl?.let { pending ->
                Log.d("AuroraDiag", "deferred load fallback (size=${view.width}x${view.height}) url=$pending")
                applyAutoDesktopUa(pending)
                view.loadUrl(pending)
                pendingUrl = null
            }
        }, 10_000)
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
        applyUserAgentMetadata(wv.settings, enabled)
        wv.reload()
    }

    private fun applyAutoDesktopUa(url: String) {
        if (uaOverrideActive || desktopEnabled) return
        if (!settings.userAgentValue.isNullOrEmpty()) return
        val wv = webView ?: return
        val host = runCatching { Uri.parse(url).host?.lowercase() ?: "" }.getOrDefault("")
        val needsDesktop = DEFAULT_DESKTOP_UA_FOR_ALL || DESKTOP_REQUIRED_DOMAINS.any { host == it || host.endsWith(".$it") }
        wv.settings.userAgentString = if (needsDesktop) desktopUserAgent else systemUserAgent
        applyUserAgentMetadata(wv.settings, needsDesktop)
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
        WebView.setWebContentsDebuggingEnabled(true)
        installDocumentStartScript(view)
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
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.setSaveFormData(false)

        webSettings.setGeolocationEnabled(true)

        systemUserAgent = WebSettings.getDefaultUserAgent(view.context)
        desktopUserAgent = settings.userAgentValue
            ?: DESKTOP_USER_AGENT
        webSettings.userAgentString = if (desktopEnabled || DEFAULT_DESKTOP_UA_FOR_ALL) desktopUserAgent else systemUserAgent
        applyBrowserCompatibilityProfile(view, webSettings)
        applyUserAgentMetadata(webSettings, desktopEnabled || DEFAULT_DESKTOP_UA_FOR_ALL)
        configureServiceWorkers()

        CookieManager.getInstance().setAcceptCookie(settings.cookiesAllowed)
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, settings.thirdPartyCookiesAllowed)

        webSettings.textZoom = settings.textZoom
        webSettings.setSafeBrowsingEnabled(true)

        view.overScrollMode = View.OVER_SCROLL_NEVER
        view.setBackgroundColor(Color.WHITE)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        view.isHorizontalScrollBarEnabled = false
        view.isVerticalScrollBarEnabled = false
        view.requestFocus()
        view.post {
            view.layoutParams = view.layoutParams
            view.requestLayout()
        }
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
        view.addOnLayoutChangeListener { v, l, t, r, b, ol, ot, or, ob ->
            Log.d("AuroraDiag", "layoutChange ${r - l}x${b - t} (old ${or - ol}x${ob - ot})")
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                Log.d("AuroraDiag", "viewAttached size=${v.width}x${v.height}")
            }
            override fun onViewDetachedFromWindow(v: View) {
                Log.d("AuroraDiag", "viewDetached size=${v.width}x${v.height}")
            }
        })
        pendingUrl?.let { url ->
            if (view.width > 0 && view.height > 0) {
                Log.d("AuroraDiag", "loadNow sized=${view.width}x${view.height} url=$url")
                applyAutoDesktopUa(url)
                view.loadUrl(url)
                pendingUrl = null
            } else {
                Log.d("AuroraDiag", "loadDeferred (view=${view.width}x${view.height}) url=$url")
                deferLoadUntilSized(view)
            }
        }
    }

    private fun installDocumentStartScript(view: WebView) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            Log.w("AuroraWebView", "Document-start JavaScript is not supported by this WebView provider")
            return
        }
        runCatching {
            WebViewCompat.addDocumentStartJavaScript(view, BROWSER_COMPAT_SCRIPT, setOf("*"))
        }.onFailure { Log.w("AuroraWebView", "Browser compatibility script install failed", it) }
    }

    private fun applyBrowserCompatibilityProfile(view: WebView, webSettings: WebSettings) {
        runCatching {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
                WebSettingsCompat.setOffscreenPreRaster(webSettings, true)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_OFF)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, false)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                WebSettingsCompat.setRequestedWithHeaderOriginAllowList(webSettings, emptySet())
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOWNLOAD_FAVICONS_ENABLED)) {
                WebSettingsCompat.setDownloadFaviconsEnabled(webSettings, true)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_AUTHENTICATION)) {
                WebSettingsCompat.setWebAuthenticationSupport(
                    webSettings,
                    WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_FOR_BROWSER
                )
            }
        }.onFailure { Log.w("AuroraWebView", "Compatibility profile failed", it) }
    }

    private fun applyUserAgentMetadata(webSettings: WebSettings, desktop: Boolean) {
        if (!desktop || !WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) return
        runCatching {
            val brands = listOf(
                UserAgentMetadata.BrandVersion.Builder()
                    .setBrand("Chromium")
                    .setMajorVersion(DESKTOP_CHROME_MAJOR)
                    .setFullVersion(DESKTOP_CHROME_VERSION)
                    .build(),
                UserAgentMetadata.BrandVersion.Builder()
                    .setBrand("Google Chrome")
                    .setMajorVersion(DESKTOP_CHROME_MAJOR)
                    .setFullVersion(DESKTOP_CHROME_VERSION)
                    .build(),
                UserAgentMetadata.BrandVersion.Builder()
                    .setBrand("Not=A?Brand")
                    .setMajorVersion("99")
                    .setFullVersion("99.0.0.0")
                    .build()
            )
            val builder = UserAgentMetadata.Builder()
                .setBrandVersionList(brands)
                .setFullVersion(DESKTOP_CHROME_VERSION)
                .setPlatform("Windows")
                .setPlatformVersion("10.0.0")
                .setArchitecture("x86")
                .setModel("")
                .setMobile(false)
                .setBitness(64)
                .setWow64(false)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA_FORM_FACTORS)) {
                builder.setFormFactors(listOf(UserAgentMetadata.FORM_FACTOR_DESKTOP))
            }
            WebSettingsCompat.setUserAgentMetadata(webSettings, builder.build())
        }.onFailure { Log.w("AuroraWebView", "User-Agent metadata failed", it) }
    }

    private fun configureServiceWorkers() {
        runCatching {
            val serviceWorkerSettings = ServiceWorkerController.getInstance().serviceWorkerWebSettings
            serviceWorkerSettings.allowContentAccess = true
            serviceWorkerSettings.allowFileAccess = false
            serviceWorkerSettings.blockNetworkLoads = false
            serviceWorkerSettings.cacheMode = WebSettings.LOAD_DEFAULT
        }.onFailure { Log.w("AuroraWebView", "Service worker setup failed", it) }
    }

    private fun cachePageFavicon(icon: Bitmap?) {
        if (isPrivate || icon == null || icon.width <= 0 || icon.height <= 0) return
        val url = webView?.url ?: currentUrl
        val domain = WebViewMappings.extractDomain(url)
        if (domain.isBlank() || domain == "about:blank" || domain == lastCachedFaviconDomain) return
        lastCachedFaviconDomain = domain
        val copy = runCatching { icon.copy(Bitmap.Config.ARGB_8888, false) }.getOrNull() ?: icon
        scope.launch {
            runCatching {
                DataService.faviconCache.put(domain, copy)
                callbacks?.onFaviconChange(url)
            }.onFailure { Log.w("AuroraWebView", "Favicon cache failed for $domain", it) }
        }
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
            view.evaluateJavascript(PAGE_DIAG_SCRIPT) { res ->
                Log.d("AuroraDiag", "url=$currentUrl $res")
            }
            view.postDelayed({
                runCatching {
                    view.evaluateJavascript(PAGE_DIAG_SCRIPT) { res ->
                        Log.d("AuroraDiag", "late url=$currentUrl $res")
                    }
                }
            }, 2500)
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

        override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
            cachePageFavicon(icon)
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

        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
            Log.d("AuroraConsole", "[${consoleMessage.messageLevel()}] ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()} ${consoleMessage.message()}")
            return true
        }
    }

    companion object {
        private const val MAX_RENDERER_CRASHES = 3

        private const val DEFAULT_DESKTOP_UA_FOR_ALL = false

        private const val DESKTOP_CHROME_MAJOR = "139"
        private const val DESKTOP_CHROME_VERSION = "139.0.0.0"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$DESKTOP_CHROME_VERSION Safari/537.36"

        private val PAGE_DIAG_SCRIPT = """
            (function(){try{var b=document.body;return 'ready='+document.readyState+' children='+(b?b.children.length:-1)+' bg='+(b?getComputedStyle(b).backgroundColor:'n/a')+' textLen='+(b?b.innerText.length:0)}catch(e){return 'err:'+e.message}})();
        """.trimIndent()

        private val BROWSER_COMPAT_SCRIPT = """
            (function(){
              if (window.__auroraBrowserCompat) return;
              try {
                Object.defineProperty(window, '__auroraBrowserCompat', { value: true, configurable: false });
              } catch(e) {
                window.__auroraBrowserCompat = true;
              }

              var desktopUa = "$DESKTOP_USER_AGENT";
              var chromeMajor = "$DESKTOP_CHROME_MAJOR";
              var chromeFull = "$DESKTOP_CHROME_VERSION";
              var brandVersions = [
                { brand: 'Chromium', version: chromeMajor },
                { brand: 'Google Chrome', version: chromeMajor },
                { brand: 'Not=A?Brand', version: '99' }
              ];
              var fullVersionList = [
                { brand: 'Chromium', version: chromeFull },
                { brand: 'Google Chrome', version: chromeFull },
                { brand: 'Not=A?Brand', version: '99.0.0.0' }
              ];

              function defineNavigatorValue(name, value) {
                var proto = null;
                try { proto = Object.getPrototypeOf(navigator); } catch(e) {}
                var targets = [proto, navigator];
                for (var i = 0; i < targets.length; i++) {
                  if (!targets[i]) continue;
                  try {
                    Object.defineProperty(targets[i], name, {
                      get: function(){ return value; },
                      configurable: true
                    });
                    return;
                  } catch(e) {}
                }
              }

              defineNavigatorValue('userAgent', desktopUa);
              defineNavigatorValue('appVersion', desktopUa.replace(/^Mozilla\//, ''));
              defineNavigatorValue('platform', 'Win32');
              defineNavigatorValue('vendor', 'Google Inc.');
              defineNavigatorValue('maxTouchPoints', 0);

              var uaData = {
                brands: brandVersions,
                mobile: false,
                platform: 'Windows',
                getHighEntropyValues: function(hints) {
                  var values = {
                    architecture: 'x86',
                    bitness: '64',
                    brands: brandVersions,
                    fullVersionList: fullVersionList,
                    mobile: false,
                    model: '',
                    platform: 'Windows',
                    platformVersion: '10.0.0',
                    uaFullVersion: chromeFull,
                    wow64: false
                  };
                  var out = { brands: brandVersions, mobile: false, platform: 'Windows' };
                  (hints || []).forEach(function(hint) {
                    if (Object.prototype.hasOwnProperty.call(values, hint)) out[hint] = values[hint];
                  });
                  return Promise.resolve(out);
                },
                toJSON: function() {
                  return { brands: brandVersions, mobile: false, platform: 'Windows' };
                }
              };
              defineNavigatorValue('userAgentData', uaData);

              try {
                if (!window.chrome) {
                  Object.defineProperty(window, 'chrome', {
                    value: { app: { isInstalled: false }, runtime: {} },
                    configurable: true
                  });
                } else {
                  window.chrome.app = window.chrome.app || { isInstalled: false };
                  window.chrome.runtime = window.chrome.runtime || {};
                }
              } catch(e) {}

              (function(){
                var mo = new MutationObserver(function(){
                  var m = document.querySelector('meta[name=viewport]');
                  if (m && m.getAttribute('content') && m.getAttribute('content').indexOf('interactive-widget') !== -1) {
                    m.setAttribute('content', m.getAttribute('content').replace(/,\s*interactive-widget=[a-z-]+/g, ''));
                  }
                });
                mo.observe(document.documentElement, {childList: true, subtree: true});
              })();
              window.addEventListener('error', function(e){
                console.log('AURORA_ERR ' + (e.message||'unknown') + ' @ ' + (e.filename||'') + ':' + (e.lineno||0));
              }, true);
              window.addEventListener('unhandledrejection', function(e){
                var r = e.reason;
                console.log('AURORA_PROMISE ' + (r && r.message ? r.message : String(r)));
              });
            })();
        """.trimIndent()

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
