package com.aurora.data.search

import java.net.URLEncoder

interface SearchEngine {
    val id: String
    val name: String
    val displayName: String
    val searchUrlTemplate: String
    val suggestionUrl: String?

    fun buildSearchUrl(query: String): String

    fun buildSuggestionUrl(query: String): String? {
        val url = suggestionUrl ?: return null
        return url.replace("{query}", URLEncoder.encode(query, "UTF-8"))
    }
}

class GoogleSearchEngine : SearchEngine {
    override val id = "google"
    override val name = "Google"
    override val displayName = "Google"
    override val searchUrlTemplate = "https://www.google.com/search?q={query}"
    override val suggestionUrl = "https://www.google.com/complete/search?client=chrome&q={query}"

    override fun buildSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://www.google.com/search?q=$encoded"
    }
}

class DuckDuckGoSearchEngine : SearchEngine {
    override val id = "duckduckgo"
    override val name = "DuckDuckGo"
    override val displayName = "DuckDuckGo"
    override val searchUrlTemplate = "https://duckduckgo.com/?q={query}"
    override val suggestionUrl = "https://duckduckgo.com/ac/?q={query}"

    override fun buildSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://duckduckgo.com/?q=$encoded"
    }
}

class BingSearchEngine : SearchEngine {
    override val id = "bing"
    override val name = "Bing"
    override val displayName = "Bing"
    override val searchUrlTemplate = "https://www.bing.com/search?q={query}"
    override val suggestionUrl = null

    override fun buildSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://www.bing.com/search?q=$encoded"
    }
}

class StartpageSearchEngine : SearchEngine {
    override val id = "startpage"
    override val name = "Startpage"
    override val displayName = "Startpage"
    override val searchUrlTemplate = "https://www.startpage.com/sp/search?q={query}"
    override val suggestionUrl = null

    override fun buildSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://www.startpage.com/sp/search?q=$encoded"
    }
}

class BraveSearchEngine : SearchEngine {
    override val id = "brave"
    override val name = "Brave"
    override val displayName = "Brave Search"
    override val searchUrlTemplate = "https://search.brave.com/search?q={query}"
    override val suggestionUrl = "https://search.brave.com/api/suggest?q={query}"

    override fun buildSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://search.brave.com/search?q=$encoded"
    }
}

object SearchEngineRegistry {
    private val engines = mapOf(
        "google" to GoogleSearchEngine(),
        "duckduckgo" to DuckDuckGoSearchEngine(),
        "bing" to BingSearchEngine(),
        "startpage" to StartpageSearchEngine(),
        "brave" to BraveSearchEngine()
    )

    val default: SearchEngine = GoogleSearchEngine()
    val all: List<SearchEngine> get() = engines.values.toList()

    fun byId(id: String): SearchEngine = engines[id] ?: default
}
