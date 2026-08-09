package com.aurora.home

import com.aurora.data.search.GoogleSearchEngine
import com.aurora.data.search.SearchEngine

object UrlDetector {

    var searchEngine: SearchEngine = GoogleSearchEngine()
        private set

    fun setSearchEngine(engine: SearchEngine) {
        searchEngine = engine
    }

    private val domainPattern = Regex(
        "^[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s]*)?$"
    )

    private val urlPattern = Regex(
        "^(https?://)?[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s]*)?$"
    )

    fun classify(input: String): InputType {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return InputType.EMPTY
        if (trimmed.contains(" ")) return InputType.SEARCH
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return InputType.URL
        }
        if (domainPattern.matches(trimmed)) {
            return InputType.DOMAIN
        }
        if (urlPattern.matches(trimmed)) {
            return InputType.URL
        }
        val internalSchemes = listOf("about:", "data:", "blob:", "chrome:", "file:", "moz-extension:")
        if (internalSchemes.any { trimmed.startsWith(it) }) {
            return InputType.URL
        }
        return InputType.SEARCH
    }

    fun toUrl(input: String): String {
        val trimmed = input.trim()
        return when (classify(trimmed)) {
            InputType.URL -> trimmed
            InputType.DOMAIN -> if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
            InputType.SEARCH, InputType.EMPTY -> searchEngine.buildSearchUrl(trimmed)
        }
    }

    enum class InputType { URL, DOMAIN, SEARCH, EMPTY }
}
