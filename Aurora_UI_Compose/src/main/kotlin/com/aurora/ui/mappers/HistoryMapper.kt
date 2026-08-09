package com.aurora.ui.mappers

import androidx.compose.ui.graphics.Color
import com.aurora.data.model.BrowserAsset
import com.aurora.data.model.HistoryEntry
import com.aurora.ui.model.HistoryGroup
import com.aurora.ui.model.HistoryUiModel
import com.aurora.ui.presentation.BookmarkPresentationRegistry
import java.util.Calendar

object HistoryMapper {
    fun toUiList(entries: List<HistoryEntry>, assets: Map<String, BrowserAsset> = emptyMap()): List<HistoryUiModel> =
        entries.map { entryToUi(it, assets[it.url]) }

    fun toGroupedList(entries: List<HistoryEntry>, assets: Map<String, BrowserAsset> = emptyMap()): List<HistoryGroup> {
        val models = toUiList(entries, assets)
        val groups = linkedMapOf<String, MutableList<HistoryUiModel>>()
        for (item in models) {
            val today = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = entries.firstOrNull { it.url == item.url }?.lastVisited ?: System.currentTimeMillis() }
            val label = when {
                isSameDay(today, target) -> "Today"
                isYesterday(today, target) -> "Yesterday"
                isSameWeek(today, target) -> "This Week"
                isSameMonth(today, target) -> "Earlier This Month"
                else -> {
                    val cal = Calendar.getInstance().apply { timeInMillis = entries.firstOrNull { it.url == item.url }?.lastVisited ?: System.currentTimeMillis() }
                    "${cal.get(Calendar.YEAR)}"
                }
            }
            groups.getOrPut(label) { mutableListOf() }.add(item)
        }
        return groups.map { (label, items) -> HistoryGroup(label, items) }
    }

    private fun entryToUi(entry: HistoryEntry, asset: BrowserAsset?): HistoryUiModel {
        val accentColor = if (asset != null && asset.dominantColor != 0xFF1A1A1A.toInt()) {
            Color(asset.dominantColor)
        } else {
            BookmarkPresentationRegistry.forUrl(entry.url).accentColor
        }
        val domain = asset?.domain ?: extractDomain(entry.url)
        return HistoryUiModel(
            id = "history-${entry.id}",
            title = asset?.title?.ifEmpty { entry.title } ?: entry.title.ifEmpty { extractName(entry.url) },
            url = entry.url,
            domain = domain,
            timeText = formatTimestamp(entry.lastVisited),
            actionVerb = actionVerbForDomain(domain, entry.url),
            visitCount = entry.visitCount,
            accentColor = accentColor,
            faviconUri = asset?.faviconUri ?: entry.favicon.takeIf { it.isNotBlank() },
            faviconBitmap = asset?.favicon,
            thumbnail = asset?.thumbnail
        )
    }

    private fun extractName(url: String): String {
        val d = extractDomain(url)
        return d.split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: d
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

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> "Last visited"
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(today: Calendar, target: Calendar): Boolean {
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return target.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
               target.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(a: Calendar, b: Calendar): Boolean {
        val diff = kotlin.math.abs((a.timeInMillis - b.timeInMillis) / (24 * 60 * 60 * 1000))
        return diff < 7
    }

    private fun isSameMonth(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.MONTH) == b.get(Calendar.MONTH)

    private fun actionVerbForDomain(domain: String, url: String): String {
        val d = domain.lowercase()
        return when {
            "youtube" in d || "twitch.tv" in d || "netflix" in d || "vimeo" in d || "dailymotion" in d -> "Watched"
            "github" in d || "gitlab" in d || "bitbucket" in d || "stackoverflow" in d || "codepen" in d -> "Edited"
            "wikipedia" in d || "medium" in d || "substack" in d || "blog" in d -> "Read"
            "reddit" in d || "twitter" in d || "x.com" in d || "facebook" in d || "instagram" in d || "threads" in d -> "Browsed"
            "google" in d || "bing" in d || "duckduckgo" in d || "search" in d -> "Searched"
            "spotify" in d || "soundcloud" in d || "music" in d -> "Listened"
            "amazon" in d || "ebay" in d || "shop" in d || "store" in d -> "Shopped"
            "gmail" in d || "mail" in d || "outlook" in d || "calendar" in d -> "Checked"
            url.contains("/map") || "maps" in d -> "Navigated"
            else -> "Visited"
        }
    }
}
