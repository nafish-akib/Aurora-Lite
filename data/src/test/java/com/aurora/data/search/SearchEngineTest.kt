package com.aurora.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    @Test
    fun `google builds encoded search url`() {
        val url = GoogleSearchEngine().buildSearchUrl("aurora browser")
        assertEquals("https://www.google.com/search?q=aurora+browser", url)
    }

    @Test
    fun `search url encodes special characters`() {
        val url = DuckDuckGoSearchEngine().buildSearchUrl("what is 2+2? & more")
        assertTrue(url, url.contains("what+is+2%2B2%3F+%26+more"))
    }

    @Test
    fun `every engine has a search url template`() {
        SearchEngineRegistry.all.forEach { engine ->
            assertTrue(engine.searchUrlTemplate.contains("{query}"))
        }
    }

    @Test
    fun `suggestion url replaces query placeholder`() {
        val url = GoogleSearchEngine().buildSuggestionUrl("test query")
        assertNotNull(url)
        assertTrue(url!!.contains("test+query"))
        assertEquals("https://www.google.com/complete/search?client=chrome&q=test+query", url)
    }

    @Test
    fun `engines without suggestions return null`() {
        assertNull(BingSearchEngine().buildSuggestionUrl("x"))
        assertNull(StartpageSearchEngine().buildSuggestionUrl("x"))
    }

    @Test
    fun `registry byId falls back to default for unknown ids`() {
        assertEquals(GoogleSearchEngine::class, SearchEngineRegistry.byId("nope")::class)
        assertEquals(DuckDuckGoSearchEngine::class, SearchEngineRegistry.byId("duckduckgo")::class)
        assertEquals("google", SearchEngineRegistry.default.id)
    }

    @Test
    fun `registry contains all five engines`() {
        assertEquals(5, SearchEngineRegistry.all.size)
    }
}