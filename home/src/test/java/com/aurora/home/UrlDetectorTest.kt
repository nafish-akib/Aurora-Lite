package com.aurora.home

import com.aurora.data.search.BingSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UrlDetectorTest {

    @Before
    fun setUp() {
        UrlDetector.setSearchEngine(BingSearchEngine())
    }

    @Test
    fun `blank input is EMPTY`() {
        assertEquals(UrlDetector.InputType.EMPTY, UrlDetector.classify(""))
        assertEquals(UrlDetector.InputType.EMPTY, UrlDetector.classify("   "))
    }

    @Test
    fun `explicit http and https are URL`() {
        assertEquals(UrlDetector.InputType.URL, UrlDetector.classify("https://example.com"))
        assertEquals(UrlDetector.InputType.URL, UrlDetector.classify("http://example.com/path?q=1"))
    }

    @Test
    fun `bare domain is DOMAIN`() {
        assertEquals(UrlDetector.InputType.DOMAIN, UrlDetector.classify("example.com"))
        assertEquals(UrlDetector.InputType.DOMAIN, UrlDetector.classify("sub.example.co.uk"))
        assertEquals(UrlDetector.InputType.DOMAIN, UrlDetector.classify("example.com:8080"))
        assertEquals(UrlDetector.InputType.DOMAIN, UrlDetector.classify("example.com/path"))
    }

    @Test
    fun `internal schemes are URL`() {
        assertEquals(UrlDetector.InputType.URL, UrlDetector.classify("about:blank"))
        assertEquals(UrlDetector.InputType.URL, UrlDetector.classify("file:///sdcard/a.pdf"))
    }

    @Test
    fun `text with spaces is SEARCH`() {
        assertEquals(UrlDetector.InputType.SEARCH, UrlDetector.classify("aurora browser"))
    }

    @Test
    fun `single words default to SEARCH`() {
        assertEquals(UrlDetector.InputType.SEARCH, UrlDetector.classify("kotlin"))
    }

    @Test
    fun `toUrl upgrades domains to https`() {
        assertEquals("https://example.com", UrlDetector.toUrl("example.com"))
        assertEquals("https://example.com/path", UrlDetector.toUrl("example.com/path"))
    }

    @Test
    fun `toUrl passes through explicit urls`() {
        assertEquals("http://example.com", UrlDetector.toUrl("http://example.com"))
        assertEquals("about:blank", UrlDetector.toUrl("about:blank"))
    }

    @Test
    fun `toUrl builds search url for queries`() {
        assertEquals("https://www.bing.com/search?q=aurora+browser", UrlDetector.toUrl("aurora browser"))
    }
}