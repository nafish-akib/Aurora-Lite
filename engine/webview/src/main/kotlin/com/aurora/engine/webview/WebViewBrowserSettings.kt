package com.aurora.engine.webview

import com.aurora.engine.BrowserSettings

class WebViewBrowserSettings(
    initialUserAgent: String? = null,
    initialAllowPopups: Boolean = false,
    initialAllowCookies: Boolean = true,
    initialAllowLocation: Boolean = true,
    initialAllowNotifications: Boolean = true,
    initialThirdPartyCookies: Boolean = true
) : BrowserSettings {

    private var _userAgent: String? = initialUserAgent
    private var _allowPopups: Boolean = initialAllowPopups
    private var _allowCookies: Boolean = initialAllowCookies
    private var _allowLocation: Boolean = initialAllowLocation
    private var _allowNotifications: Boolean = initialAllowNotifications

    val userAgentValue: String?
        get() = _userAgent

    val popupsAllowed: Boolean
        get() = _allowPopups

    val cookiesAllowed: Boolean
        get() = _allowCookies

    val locationAllowed: Boolean
        get() = _allowLocation

    val notificationsAllowed: Boolean
        get() = _allowNotifications

    private var _thirdPartyCookies: Boolean = initialThirdPartyCookies
    val thirdPartyCookiesAllowed: Boolean
        get() = _thirdPartyCookies

    override var userAgent: String
        get() = _userAgent ?: ""
        set(value) { _userAgent = value.ifEmpty { null } }

    override var allowJavaScript: Boolean = true

    override var allowCookies: Boolean
        get() = _allowCookies
        set(value) { _allowCookies = value }

    override var allowPopups: Boolean
        get() = _allowPopups
        set(value) { _allowPopups = value }

    override var allowLocation: Boolean
        get() = _allowLocation
        set(value) { _allowLocation = value }

    override var allowNotifications: Boolean
        get() = _allowNotifications
        set(value) { _allowNotifications = value }

    override var allowThirdPartyCookies: Boolean
        get() = _thirdPartyCookies
        set(value) { _thirdPartyCookies = value }

    override var textZoom: Int = 100

    override var adBlockingEnabled: Boolean = false

    override var sslBypassEnabled: Boolean = false

    val adBlockHosts: Set<String> = setOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "googletagmanager.com",
        "scorecardresearch.com", "quantserve.com", "chartbeat.com",
        "adsrvr.org", "pubmatic.com", "openx.net", "adnxs.com",
        "amazon-adsystem.com", "moatads.com", "criteo.com", "criteo.net",
        "outbrain.com", "taboola.com", "zedo.com", "casalemedia.com",
        "rubiconproject.com", "adsafeprotected.com", "serving-sys.com",
        "connect.facebook.net", "ads.linkedin.com"
    )
}