package com.aurora.browser.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserStateTest {

    @Test
    fun `defaults are safe for fresh state`() {
        val state = BrowserState()
        assertEquals("", state.currentUrl)
        assertEquals("", state.pageTitle)
        assertFalse(state.isLoading)
        assertEquals(0, state.loadingProgress)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertNull(state.errorMessage)
        assertFalse(state.isSecure)
        assertFalse(state.isFullScreen)
        assertTrue(state.errorState is ErrorState.None)
        assertEquals("", state.lastFailedUrl)
    }

    @Test
    fun `copy preserves unrelated fields`() {
        val state = BrowserState(currentUrl = "https://a.com", isLoading = true, loadingProgress = 55)
        val updated = state.copy(canGoBack = true)
        assertEquals("https://a.com", updated.currentUrl)
        assertTrue(updated.isLoading)
        assertEquals(55, updated.loadingProgress)
        assertTrue(updated.canGoBack)
    }
}
