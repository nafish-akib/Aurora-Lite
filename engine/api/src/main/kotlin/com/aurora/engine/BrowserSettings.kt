package com.aurora.engine

interface BrowserSettings {
    var userAgent: String
    var allowJavaScript: Boolean
    var allowPopups: Boolean
    var allowCookies: Boolean
    var allowLocation: Boolean
    var allowNotifications: Boolean
    var allowThirdPartyCookies: Boolean
    var textZoom: Int
    var adBlockingEnabled: Boolean
    var sslBypassEnabled: Boolean
}

