package com.aurora.engine.webview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewMappingsTest {

    @Test
    fun secureUrlDetection() {
        assertTrue(WebViewMappings.isSecureUrl("https://example.com/page"))
        assertFalse(WebViewMappings.isSecureUrl("http://example.com"))
        assertFalse(WebViewMappings.isSecureUrl("about:blank"))
    }

    @Test
    fun domainExtractionStripsSchemeWwwPortAndPath() {
        assertEquals("example.com", WebViewMappings.extractDomain("https://example.com/path?q=1#frag"))
        assertEquals("example.com", WebViewMappings.extractDomain("http://www.example.com:8080/x"))
        assertEquals("m.example.com", WebViewMappings.extractDomain("https://m.example.com/"))
        assertEquals("example.com", WebViewMappings.extractDomain("https://www.example.com"))
    }

    @Test
    fun errorCodeToDescriptionMapping() {
        // WebViewClient constants: ERROR_OK=0, ERROR_HOST_LOOKUP=-2, ERROR_UNKNOWN=-1
        assertEquals("OK", WebViewMappings.describeError(0))
        assertEquals("Server hostname could not be resolved", WebViewMappings.describeError(-2))
        assertEquals("Request timed out", WebViewMappings.describeError(-8))
        assertEquals("SSL handshake failed", WebViewMappings.describeError(-11))
        assertTrue(WebViewMappings.describeError(-99).contains("Network error"))
    }

    @Test
    fun downloadNameFromContentDisposition() {
        assertEquals(
            "report.pdf",
            WebViewMappings.fileNameFrom(
                "https://example.com/download?id=4",
                "attachment; filename=\"report.pdf\""
            )
        )
        assertEquals(
            "na\u00EFve file.txt",
            WebViewMappings.fileNameFrom(
                "https://example.com/dl",
                "attachment; filename*=UTF-8''na%C3%AFve%20file.txt"
            )
        )
    }

    @Test
    fun downloadNameFallsBackToUrlPath() {
        assertEquals("archive.zip", WebViewMappings.fileNameFrom("https://example.com/files/archive.zip", null))
        assertEquals("download", WebViewMappings.fileNameFrom("https://example.com/", null))
    }
}