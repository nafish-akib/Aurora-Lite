package com.aurora.engine.webview

import android.webkit.WebViewClient

/**
 * Pure mapping helpers for the WebView bridge. Kept free of Android runtime
 * state so callback-mapping logic is JVM-unit-testable.
 */
object WebViewMappings {

    fun isSecureUrl(url: String): Boolean =
        url.startsWith("https://", ignoreCase = true)

    fun extractDomain(url: String): String = url
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .split("/", "?", "#")
        .firstOrNull()
        ?.substringBefore(':')
        ?.takeIf { it.isNotBlank() }
        ?: url

    /** Gecko-compatible alias used by permission flows. */
    fun domainFromUrl(url: String): String = extractDomain(url)

    fun describeError(code: Int): String = when (code) {
        0 -> "OK"
        WebViewClient.ERROR_HOST_LOOKUP -> "Server hostname could not be resolved"
        WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME -> "Unsupported authentication scheme"
        WebViewClient.ERROR_AUTHENTICATION -> "Authentication error"
        WebViewClient.ERROR_PROXY_AUTHENTICATION -> "Proxy authentication required"
        WebViewClient.ERROR_CONNECT -> "Connection failed"
        WebViewClient.ERROR_IO -> "I/O error while reading the request"
        WebViewClient.ERROR_TIMEOUT -> "Request timed out"
        WebViewClient.ERROR_REDIRECT_LOOP -> "Too many redirects"
        WebViewClient.ERROR_UNSUPPORTED_SCHEME -> "Unsupported URL scheme"
        WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "SSL handshake failed"
        WebViewClient.ERROR_BAD_URL -> "Malformed URL"
        WebViewClient.ERROR_FILE -> "File error"
        WebViewClient.ERROR_FILE_NOT_FOUND -> "File not found"
        WebViewClient.ERROR_TOO_MANY_REQUESTS -> "Too many requests"
        WebViewClient.ERROR_UNKNOWN -> "Unknown error"
        else -> "Network error ($code)"
    }

    fun describeHttpError(statusCode: Int): String = when (statusCode) {
        400 -> "Bad request"
        401 -> "Unauthorized"
        402 -> "Payment required"
        403 -> "Access denied"
        404 -> "Page not found"
        405 -> "Method not allowed"
        406 -> "Not acceptable"
        408 -> "Request timeout"
        409 -> "Conflict"
        410 -> "Gone"
        413 -> "Payload too large"
        414 -> "URI too long"
        415 -> "Unsupported media type"
        429 -> "Too many requests"
        451 -> "Unavailable for legal reasons"
        500 -> "Internal server error"
        501 -> "Not implemented"
        502 -> "Bad gateway"
        503 -> "Service unavailable"
        504 -> "Gateway timeout"
        else -> "HTTP error $statusCode"
    }

    /**
     * Infers a download file name from the URL and the Content-Disposition
     * header (filename*=UTF-8''... or filename="...").
     */
    fun fileNameFrom(url: String, contentDisposition: String?): String {
        contentDisposition?.let { cd ->
            val star = Regex("""filename\*=UTF-8''([^;]+)""", RegexOption.IGNORE_CASE).find(cd)
            if (star != null) {
                val decoded = java.net.URLDecoder.decode(star.groupValues[1].trim().trim('"'), "UTF-8")
                if (decoded.isNotBlank()) return decoded
            }
            val plain = Regex("""filename="?([^";]+)"?""", RegexOption.IGNORE_CASE).find(cd)
            if (plain != null) {
                val name = plain.groupValues[1].trim().trim('"')
                if (name.isNotBlank() && name != "download") return name
            }
        }
        val path = url.substringBefore("?").substringBefore("#")
        val fromPath = path.substringAfterLast('/')
        if (fromPath.isNotBlank() && fromPath != "/") return fromPath
        return "download"
    }
}