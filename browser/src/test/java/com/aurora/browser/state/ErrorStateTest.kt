package com.aurora.browser.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorStateTest {

    @Test
    fun `WebView network codes map to NetworkError`() {
        val codes = listOf(-2, -6, -7, -8, -9)
        codes.forEach { code ->
            val state = ErrorState.fromEngineError(code, "connection issue", "https://example.com")
            assertTrue("code $code should be NetworkError but was $state", state is ErrorState.NetworkError)
        }
    }

    @Test
    fun `WebView SSL code maps to SslError`() {
        val state = ErrorState.fromEngineError(-11, "SSL handshake failed", "https://example.com")
        assertTrue(state is ErrorState.SslError)
        assertEquals("SSL handshake failed", (state as ErrorState.SslError).description)
    }

    @Test
    fun `file-not-found code maps to FileNotFound with url`() {
        val state = ErrorState.fromEngineError(-14, "missing", "https://example.com/page")
        assertTrue(state is ErrorState.FileNotFound)
        assertEquals("https://example.com/page", (state as ErrorState.FileNotFound).url)
    }

    @Test
    fun `positive codes map to HttpError`() {
        val state = ErrorState.fromEngineError(404, "Not Found", "https://example.com")
        assertTrue(state is ErrorState.HttpError)
        assertEquals(404, (state as ErrorState.HttpError).code)
    }

    @Test
    fun `unknown codes map to Unknown`() {
        val state = ErrorState.fromEngineError(-99, "weird", "https://example.com")
        assertTrue(state is ErrorState.Unknown)
        assertEquals(-99, (state as ErrorState.Unknown).code)
    }

    @Test
    fun `isError is false only for None`() {
        assertFalse(ErrorState.None.isError)
        assertTrue(ErrorState.fromEngineError(500, "oops", "https://x.com").isError)
    }

    @Test
    fun `auth codes map to Unknown`() {
        val codes = listOf(-1, -3, -4, -5, -10, -12, -13)
        codes.forEach { code ->
            val state = ErrorState.fromEngineError(code, "auth or other", "https://example.com")
            assertTrue("code $code should be Unknown but was $state", state is ErrorState.Unknown)
        }
    }

    @Test
    fun `WebView rate-limit code maps to Unknown with clear message`() {
        val state = ErrorState.fromEngineError(-15, "too many", "https://example.com")
        assertTrue(state is ErrorState.Unknown)
        assertTrue((state as ErrorState.Unknown).description.contains("rate-limiting"))
    }
}
