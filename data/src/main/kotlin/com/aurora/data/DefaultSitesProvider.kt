package com.aurora.data

data class DefaultSite(val url: String, val title: String)

interface DefaultSitesProvider {
    fun getDefaultFavorites(): List<DefaultSite>
    fun getPopularSites(): List<DefaultSite>
}

class SimpleDefaultSitesProvider : DefaultSitesProvider {

    override fun getDefaultFavorites(): List<DefaultSite> = listOf(
        DefaultSite("https://www.youtube.com", "YouTube"),
        DefaultSite("https://github.com", "GitHub"),
        DefaultSite("https://www.wikipedia.org", "Wikipedia"),
        DefaultSite("https://news.google.com", "Google News"),
        DefaultSite("https://www.reddit.com", "Reddit"),
        DefaultSite("https://drive.google.com", "Google Drive")
    )

    override fun getPopularSites(): List<DefaultSite> = listOf(
        DefaultSite("https://www.youtube.com", "YouTube"),
        DefaultSite("https://github.com", "GitHub"),
        DefaultSite("https://www.wikipedia.org", "Wikipedia"),
        DefaultSite("https://news.google.com", "Google News"),
        DefaultSite("https://www.reddit.com", "Reddit")
    )
}
