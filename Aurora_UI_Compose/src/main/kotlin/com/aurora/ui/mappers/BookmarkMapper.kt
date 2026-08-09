package com.aurora.ui.mappers

import androidx.compose.ui.graphics.Color
import com.aurora.data.model.BrowserAsset
import com.aurora.data.model.Favorite
import com.aurora.ui.model.FavoriteUiModel

object BookmarkMapper {
    fun toUi(favorite: Favorite, asset: BrowserAsset? = null): FavoriteUiModel {
        return FavoriteUiModel(
            id = favorite.id,
            url = favorite.url,
            title = asset?.title?.ifEmpty { favorite.title } ?: favorite.title.ifEmpty { extractTitleFromUrl(favorite.url) },
            domain = asset?.domain ?: extractDomain(favorite.url),
            faviconBitmap = asset?.favicon,
            faviconUri = asset?.faviconUri,
            accentColor = if (asset != null && asset.dominantColor != 0xFF1A1A1A.toInt()) Color(asset.dominantColor) else Color(0xFF4DA3FF)
        )
    }

    fun toUiList(favorites: List<Favorite>, assets: Map<String, BrowserAsset> = emptyMap()): List<FavoriteUiModel> {
        return favorites.map { toUi(it, assets[it.url]) }
    }

    private fun extractTitleFromUrl(url: String): String {
        val domain = extractDomain(url)
        return domain.takeWhile { it != '.' }.replaceFirstChar { it.uppercase() }
    }

    private fun extractDomain(url: String): String {
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?: url
    }
}
