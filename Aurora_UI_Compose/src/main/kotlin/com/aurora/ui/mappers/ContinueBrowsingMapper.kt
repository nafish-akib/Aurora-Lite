package com.aurora.ui.mappers

import com.aurora.data.model.BrowserAsset
import com.aurora.home.ContinueBrowsingState
import com.aurora.ui.model.ContinueBrowsingUiModel
import com.aurora.ui.presentation.BookmarkPresentationRegistry
import androidx.compose.ui.graphics.Color

object ContinueBrowsingMapper {
    fun toUiList(state: ContinueBrowsingState): List<ContinueBrowsingUiModel> {
        return state.assets.map { assetToUi(it) }.distinctBy { it.url }
    }

    private fun assetToUi(asset: BrowserAsset): ContinueBrowsingUiModel {
        val presentation = BookmarkPresentationRegistry.forUrl(asset.url)
        return ContinueBrowsingUiModel(
            id = "asset-${asset.url.hashCode()}",
            name = extractName(asset.url),
            title = asset.title.ifEmpty { extractName(asset.url) },
            timeText = formatTimestamp(asset.lastVisited),
            url = asset.url,
            domain = asset.domain,
            accentColor = asset.dominantColor.let {
                if (it != 0xFF1A1A1A.toInt()) Color(it) else presentation.accentColor
            },
            faviconUri = asset.faviconUri,
            faviconBitmap = asset.favicon,
            thumbnail = asset.thumbnail
        )
    }

    private fun extractName(url: String): String {
        val domain = url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")
            .firstOrNull()
            ?: url
        return domain.split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: domain
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Active now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> "Last visited"
        }
    }
}
