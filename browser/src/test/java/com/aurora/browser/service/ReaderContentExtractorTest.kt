package com.aurora.browser.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderContentExtractorTest {

    private val extractor = ReaderContentExtractor

    @Test
    fun `extracts title and drops scripts styles and nav`() {
        val html = """
            <html><head><title>   My Page Title   </title>
            <style>.x{color:red}</style></head>
            <body>
            <nav>Navigation links here</nav>
            <script>alert('xss');</script>
            <p>This is the first paragraph with enough length to pass the filter.</p>
            <h2>A heading that is long enough to be kept</h2>
            <div>Short div text</div>
            <p>This is the second paragraph that also passes the length check.</p>
            </body></html>
        """.trimIndent()

        val result = extractor.parseHtml(html, "https://example.com")

        assertEquals("My Page Title", result.title)
        assertTrue(result.text.contains("first paragraph"))
        assertTrue(result.text.contains("second paragraph"))
        assertTrue(result.text.contains("heading"))
        assertTrue(!result.text.contains("Navigation"))
        assertTrue(!result.text.contains("var('xss')"))
        assertTrue(!result.text.contains("color:red"))
        assertEquals("https://example.com", result.url)
    }

    @Test
    fun `missing title falls back to url host`() {
        val html = "<html><body><p>Only a body paragraph that is long enough to survive.</p></body></html>"
        val result = extractor.parseHtml(html, "https://example.com/article")
        assertEquals("example.com/article", result.title)
    }

    @Test
    fun `decodes html entities`() {
        val html = "<html><body><p>Price is &amp; 10 &lt; 20 and highlight &nbsp; space.</p></body></html>"
        val result = extractor.parseHtml(html, "https://example.com")
        assertTrue(result.text, result.text.contains("&"))
        assertTrue(result.text, result.text.contains("<"))
    }

    @Test
    fun `short paragraphs are dropped`() {
        val html = "<html><body><p>hi</p><p>way too short</p><p>This paragraph is sufficiently long to be included in the output text.</p></body></html>"
        val result = extractor.parseHtml(html, "https://example.com")
        assertTrue(!result.text.contains("way too short"))
        assertTrue(result.text.contains("sufficiently long"))
    }
}