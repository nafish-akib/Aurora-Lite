package com.aurora.browser.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ReaderContentExtractor {
    private val scriptRegex = Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE)
    private val styleRegex = Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE)
    private val tagRegex = Regex("<[^>]*>")
    private val navRegex = Regex("<nav[^>]*>[\\s\\S]*?</nav\\s*>", RegexOption.IGNORE_CASE)
    private val svgRegex = Regex("<svg[^>]*>[\\s\\S]*?</svg>", RegexOption.IGNORE_CASE)
    private val headerFooterRegex = Regex("<(header|footer)[^>]*>[\\s\\S]*?</\\1>", RegexOption.IGNORE_CASE)
    private val entityRegex = Regex("&(\\w{1,6}|#\\d{1,5}|#[xX][\\da-fA-F]{1,4});")
    private val titleRegex = Regex("<title[^>]*>([\\s\\S]*?)</title>", RegexOption.IGNORE_CASE)
    private val multiSpaceRegex = Regex("\\s+")
    private val paragraphBlockRegex = Regex("<(p|h[1-6]|li|td|th|blockquote|article|section|main)[^>]*>([\\s\\S]*?)</\\1>", RegexOption.IGNORE_CASE)
    private val lineBreaksRegex = Regex("\\n{3,}")

    data class ExtractedContent(val title: String, val text: String, val url: String)

    suspend fun extract(url: String): ExtractedContent? = withContext(Dispatchers.IO) {
        try {
            val html = fetch(url) ?: return@withContext null
            parseHtml(html, url)
        } catch (_: Exception) {
            null
        }
    }

    internal fun parseHtml(html: String, url: String): ExtractedContent {
        val cleaned = scriptRegex.replace(html, "")
            .let { styleRegex.replace(it, "") }
            .let { navRegex.replace(it, "") }
            .let { svgRegex.replace(it, "") }
            .let { headerFooterRegex.replace(it, "") }
        val title = titleRegex.find(cleaned)?.groupValues?.getOrNull(1)?.trim()?.take(120) ?: ""
        val body = extractBodyText(cleaned)
        val decoded = entityRegex.replace(body) { decodeEntity(it.groupValues[1]) }
        val normalized = multiSpaceRegex.replace(decoded, " ").trim()
        return ExtractedContent(title = title.ifEmpty { url.removePrefix("https://").removePrefix("http://") }, text = normalized, url = url)
    }

    private fun extractBodyText(html: String): String {
        val sb = StringBuilder()
        paragraphBlockRegex.findAll(html).forEach { match ->
            val inner = tagRegex.replace(match.groupValues[2], "")
            val cleaned = inner.replace("&nbsp;", " ").replace("&amp;", "&").trim()
            if (cleaned.length > 15) {
                sb.appendLine(cleaned)
            }
        }
        if (sb.isEmpty()) {
            sb.append(tagRegex.replace(html, " ").trim())
        }
        return sb.toString()
    }

    private fun fetch(url: String): String? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; AuroraBrowser/1.0)")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return try {
            conn.requestMethod = "GET"
            conn.connect()
            val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
            reader.readText()
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private val namedEntities = mapOf(
        "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
        "nbsp" to " ", "copy" to "\u00A9", "reg" to "\u00AE", "trade" to "\u2122",
        "hellip" to "\u2026", "mdash" to "\u2014", "ndash" to "\u2013",
        "ldquo" to "\u201C", "rdquo" to "\u201D", "lsquo" to "\u2018", "rsquo" to "\u2019",
        "bull" to "\u2022", "middot" to "\u00B7", "deg" to "\u00B0", "plusmn" to "\u00B1",
        "eacute" to "\u00E9", "agrave" to "\u00E0", "egrave" to "\u00E8", "ccedil" to "\u00E7"
    )

    private fun decodeEntity(entity: String): String {
        try {
            val decoded = android.text.Html
                .fromHtml("&$entity;", android.text.Html.FROM_HTML_MODE_LEGACY)
                .toString().trim()
            if (decoded.isNotBlank()) return decoded
        } catch (_: Exception) {
        }
        return when {
            entity.startsWith("#x", ignoreCase = true) ->
                entity.drop(2).toIntOrNull(16)?.toChar()?.toString() ?: "&$entity;"
            entity.startsWith("#") ->
                entity.drop(1).toIntOrNull()?.toChar()?.toString() ?: "&$entity;"
            else -> namedEntities[entity] ?: "&$entity;"
        }
    }
}