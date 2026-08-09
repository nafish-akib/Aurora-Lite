package com.aurora.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.browser.ui.theme.auroraCardLift
import com.aurora.browser.ui.theme.auroraGlass
import com.aurora.browser.ui.theme.auroraGlow
import com.aurora.browser.ui.theme.auroraLightSweep
import com.aurora.browser.ui.theme.auroraGradientBorder
import com.aurora.browser.ui.theme.focusPing
import com.aurora.browser.ui.theme.StaggerStep
import com.aurora.browser.ui.components.BrandIcon
import com.aurora.ui.model.ContinueBrowsingUiModel
import com.aurora.ui.data.MockData
import com.aurora.ui.model.DownloadUiModel
import com.aurora.ui.model.HistoryUiModel
import com.aurora.ui.model.HomeUiState
import com.aurora.ui.model.QuickActionUiModel
import kotlinx.coroutines.delay

data class FavoriteSite(
    val name: String,
    val url: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val faviconBitmap: android.graphics.Bitmap? = null,
    val domain: String = ""
)

data class TrendingItem(
    val title: String,
    val domain: String,
    val category: String
)

data class Profile(
    val id: String,
    val name: String,
    val avatar: String,
    val isGuest: Boolean,
    val isSynced: Boolean
)

val POPULAR_SITES = listOf(
    FavoriteSite("Google", "https://google.com", Color(0xFF4285F4), Icons.Default.Search),
    FavoriteSite("YouTube", "https://youtube.com", Color(0xFFFF0000), Icons.Default.PlayArrow),
    FavoriteSite("Facebook", "https://facebook.com", Color(0xFF1877F2), Icons.Default.People),
    FavoriteSite("GitHub", "https://github.com", Color(0xFF181717), Icons.Default.Code),
    FavoriteSite("Wikipedia", "https://wikipedia.org", Color(0xFF333333), Icons.Default.Book),
    FavoriteSite("Reddit", "https://reddit.com", Color(0xFFFF4500), Icons.Default.List),
    FavoriteSite("Twitch", "https://twitch.tv", Color(0xFF9146FF), Icons.Default.LiveTv),
    FavoriteSite("Netflix", "https://netflix.com", Color(0xFFE50914), Icons.Default.Movie),
    FavoriteSite("Google News", "https://news.google.com", Color(0xFF4285F4), Icons.Default.List),
    FavoriteSite("Google Drive", "https://drive.google.com", Color(0xFF34A853), Icons.Default.Folder)
)

val TRENDING_ITEMS = listOf(
    TrendingItem("SpaceX Mars Cargo launch vehicle development specs", "spacex.com", "Tech"),
    TrendingItem("Jetpack Compose for Android TV 1.4 best practices & layouts", "developer.android.com", "Dev"),
    TrendingItem("Local AI model inference optimization on television chips", "huggingface.co", "AI")
)

fun getGreeting(): String {
    val hr = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hr < 12 -> "Good Morning"
        hr < 18 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
fun StaggeredSection(
    step: StaggerStep,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(step) {
        delay(step.delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val yOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 500)
    )
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = yOffset.toPx()
        }
    ) {
        content()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FocusScrollZone(
    zone: String,
    focusedZone: String,
    content: @Composable () -> Unit
) {
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(focusedZone) {
        if (focusedZone == zone) {
            bringIntoView.bringIntoView()
        }
    }
    Box(Modifier.bringIntoViewRequester(bringIntoView)) {
        content()
    }
}

@Composable
fun HomeScreen(
    searchQuery: String = "",
    currentProfile: Profile = Profile("prof-1", "Akib Al Nafij", "", false, true),
    downloads: List<DownloadUiModel> = emptyList(),
    developerMode: Boolean = false,
    focusedZone: String = "search",
    focusedItemIndex: Int = 0,
    onZoneFocusChange: (String, Int) -> Unit = { _, _ -> },
    onNavigate: (String) -> Unit = {},
    onSearchFocus: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    onOpenPasswords: () -> Unit = {},
    onAIAssistant: () -> Unit = {},
    uiState: HomeUiState = HomeUiState(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StaggeredSection(StaggerStep.ZERO) {
            HomeGreetingSection(
                profileName = uiState.profileName.ifEmpty { currentProfile.name },
                greeting = uiState.greeting,
                onBrandClick = {}
            )
        }

        StaggeredSection(StaggerStep.ONE) {
            FocusScrollZone(zone = "search", focusedZone = focusedZone) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HeroSearchBar(
                    searchQuery = searchQuery,
                    isFocused = focusedZone == "search",
                    onClick = {
                        onZoneFocusChange("search", 0)
                        onSearchFocus()
                    }
                )
            }
            }
        }

        FocusScrollZone(zone = "continue", focusedZone = focusedZone) {
        StaggeredSection(StaggerStep.TWO) {
            ContinueBrowsingRow(
                sites = uiState.continueBrowsing,
                focusedZone = focusedZone,
                focusedItemIndex = focusedItemIndex,
                onSiteClick = { site, index ->
                    onZoneFocusChange("continue", index)
                    onNavigate(site.url)
                },
                onZoneFocusChange = onZoneFocusChange
            )
        }
        }

        StreamingHubSection(
            featured = MockData.featuredStreamingSites,
            allSites = MockData.streamingSites,
            focusedZone = focusedZone,
            focusedItemIndex = focusedItemIndex,
            onSiteClick = { site -> onNavigate(site.url) },
            onZoneFocusChange = onZoneFocusChange
        )

        FocusScrollZone(zone = "favorites", focusedZone = focusedZone) {
        StaggeredSection(StaggerStep.THREE) {
            FavoritesRow(
                sites = uiState.favorites.map { FavoriteSite(name = it.title, url = it.url, color = it.accentColor, faviconBitmap = it.faviconBitmap, domain = it.domain) },
                focusedZone = focusedZone,
                focusedItemIndex = focusedItemIndex,
                onSiteClick = { site, index ->
                    onZoneFocusChange("favorites", index)
                    onNavigate(site.url)
                },
                onZoneFocusChange = onZoneFocusChange
            )
        }
        }

        FocusScrollZone(zone = "trending", focusedZone = focusedZone) {
        StaggeredSection(StaggerStep.FIVE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrendingSection(
                    items = TRENDING_ITEMS,
                    focusedZone = focusedZone,
                    focusedItemIndex = focusedItemIndex,
                    onItemClick = { item, index ->
                        onZoneFocusChange("trending", index)
                        onNavigate("https://google.com/search?q=${java.net.URLEncoder.encode(item.title, "UTF-8")}")
                    },
                    onZoneFocusChange = onZoneFocusChange,
                    modifier = Modifier.weight(2f)
                )

                DownloadsCard(
                    downloads = downloads,
                    isFocused = focusedZone == "downloads",
                    onClick = {
                        onZoneFocusChange("downloads", 0)
                        onOpenLibrary()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        }

        FocusScrollZone(zone = "quickActions", focusedZone = focusedZone) {
        StaggeredSection(StaggerStep.SIX) {
            QuickActionsRow(
                actions = uiState.quickActions,
                focusedZone = focusedZone,
                focusedItemIndex = focusedItemIndex,
                onActionClick = { index ->
                    onZoneFocusChange("quickActions", index)
                    when (index) {
                        0 -> onOpenLibrary()
                        1 -> onOpenSettings()
                        2 -> onOpenPasswords()
                        3 -> onAIAssistant()
                        4 -> onOpenDiagnostics()
                    }
                },
                onZoneFocusChange = onZoneFocusChange
            )
        }
        }

        FocusScrollZone(zone = "history", focusedZone = focusedZone) {
        StaggeredSection(StaggerStep.SEVEN) {
            HistoryRow(
                entries = uiState.history,
                focusedZone = focusedZone,
                focusedItemIndex = focusedItemIndex,
                onEntryClick = { entry, index ->
                    onZoneFocusChange("history", index)
                    onNavigate(entry.url)
                },
                onZoneFocusChange = onZoneFocusChange
            )
        }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun HomeGreetingSection(
    profileName: String,
    greeting: String = "",
    onBrandClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Blue, CircleShape)
                        .graphicsLayer {
                            shadowElevation = 8f
                        }
                )
                Text(
                    text = "Aurora Operating Core",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = AuroraColors.Blue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "LIVING GLASS V2.0",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (greeting.isNotEmpty()) "${greeting}, " else "${getGreeting()}, ",
            style = AuroraTypography.TitleDisplay,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = profileName.split(" ").first(),
            style = AuroraTypography.TitleDisplay.copy(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.White, Color.White.copy(alpha = 0.6f))
                )
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = AuroraColors.Purple,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Living Glass interface is fully optimized for 3-meter living room viewing.",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun HeroSearchBar(
    searchQuery: String = "",
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .heightIn(min = 56.dp)
            .clickable { onClick() }
            .auroraGradientBorder(
                isEnabled = isFocused,
                shape = AuroraShapes.Rounded3Xl
            )
            .then(
                if (isFocused) Modifier.auroraGlow(
                    color = AuroraColors.Blue.copy(alpha = 0.32f * glowAlpha),
                    radius = 25.dp,
                    shapeRadius = 24.dp
                ) else Modifier
            )
            .background(
                if (isFocused) Color(0xFF121317) else AuroraColors.GlassBackground,
                AuroraShapes.Rounded3Xl
            )
            .border(
                1.dp,
                if (isFocused) Color.Transparent else Color.White.copy(alpha = 0.06f),
                AuroraShapes.Rounded3Xl
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayText = when {
                        searchQuery.isNotEmpty() -> searchQuery
                        isFocused -> "D-pad OK to search or voice command..."
                        else -> "Search web, enter address, or ask Aurora AI..."
                    }
                    Text(
                        text = displayText,
                        style = AuroraTypography.Body,
                        color = if (searchQuery.isNotEmpty() || isFocused) Color.White else Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                    if (isFocused) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(14.dp)
                                .background(AuroraColors.Blue.copy(alpha = blinkAlpha))
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedMd)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = if (isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                )
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "AI",
                    tint = AuroraColors.Purple,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ContinueBrowsingRow(
    sites: List<ContinueBrowsingUiModel>,
    focusedZone: String,
    focusedItemIndex: Int,
    onSiteClick: (ContinueBrowsingUiModel, Int) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Blue, CircleShape)
                )
                Text(
                    text = "Continue Browsing",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.40f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = if (sites.isNotEmpty()) "${sites.size} site${if (sites.size != 1) "s" else ""}" else "No sessions yet",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .auroraGlass()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start browsing to see your sessions here",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp
                )
            }
        } else {
            val lazyListState = rememberLazyListState()
        LaunchedEffect(focusedItemIndex, focusedZone) {
            if (focusedZone == "continue" && sites.isNotEmpty()) {
                lazyListState.animateScrollToItem(focusedItemIndex.coerceIn(0, sites.size - 1))
            }
        }
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sites.size) { index ->
                val site = sites[index]
                val isFocused = focusedZone == "continue" && focusedItemIndex == index
                ContinueBrowsingCard(
                        site = site,
                        isFocused = isFocused,
                        onClick = { onSiteClick(site, index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueBrowsingCard(
    site: ContinueBrowsingUiModel,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .auroraCardLift(
                isFocused = isFocused,
                shape = AuroraShapes.Rounded3Xl,
                onFocusedColor = site.accentColor
            )
            .auroraLightSweep(isFocused)
            .focusPing(isFocused, site.accentColor)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0A0A0D)),
            contentAlignment = Alignment.TopEnd
        ) {
            val thumbnailBmp = site.thumbnail
            if (thumbnailBmp != null) {
                Image(
                    bitmap = thumbnailBmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.85f },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    site.accentColor.copy(alpha = 0.7f),
                                    site.accentColor.copy(alpha = 0.15f),
                                    Color(0xFF0A0A0D).copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.5f), AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), AuroraShapes.RoundedMd),
                contentAlignment = Alignment.Center
            ) {
                val faviconBmp = site.faviconBitmap
                if (faviconBmp != null) {
                    Image(
                        bitmap = faviconBmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = site.name.take(2).uppercase(),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F14), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(12.dp)
        ) {
            Text(
                text = site.title,
                style = AuroraTypography.Body,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(site.accentColor.copy(alpha = 0.9f), CircleShape)
                )
                Text(
                    text = site.domain,
                    style = AuroraTypography.MonoLabel,
                    color = site.accentColor.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = site.timeText,
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun StreamingHubSection(
    featured: List<MockData.PopularSite>,
    allSites: List<MockData.PopularSite>,
    focusedZone: String,
    focusedItemIndex: Int,
    onSiteClick: (MockData.PopularSite) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Amber, CircleShape)
                )
                Text(
                    text = "Streaming Hub",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "${allSites.size} streaming destinations worldwide",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FocusScrollZone(zone = "streaming", focusedZone = focusedZone) {
        StreamingSiteRow(
            sites = featured,
            zone = "streaming",
            label = "FEATURED",
            focusedZone = focusedZone,
            focusedItemIndex = focusedItemIndex,
            onSiteClick = onSiteClick,
            onZoneFocusChange = onZoneFocusChange
        )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val grid = allSites.chunked(MockData.STREAMING_COLUMNS)
        grid.forEachIndexed { row, chunk ->
            val rowZone = MockData.streamingRowGroupName(row)
            FocusScrollZone(zone = rowZone, focusedZone = focusedZone) {
            StreamingSiteRow(
                sites = chunk,
                zone = rowZone,
                label = if (row == 0) "WORLDWIDE" else null,
                focusedZone = focusedZone,
                focusedItemIndex = focusedItemIndex,
                onSiteClick = onSiteClick,
                onZoneFocusChange = onZoneFocusChange
            )
            }
        }
    }
}

@Composable
private fun StreamingSiteRow(
    sites: List<MockData.PopularSite>,
    zone: String,
    label: String?,
    focusedZone: String,
    focusedItemIndex: Int,
    onSiteClick: (MockData.PopularSite) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        if (label != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                )
                Text(
                    text = label,
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
        val lazyListState = rememberLazyListState()
        LaunchedEffect(focusedItemIndex, focusedZone) {
            if (focusedZone == zone && sites.isNotEmpty()) {
                lazyListState.animateScrollToItem(focusedItemIndex.coerceIn(0, sites.size - 1))
            }
        }
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sites.size) { index ->
                val site = sites[index]
                val isFocused = focusedZone == zone && focusedItemIndex == index
                StreamingSiteTile(
                    site = site,
                    isFocused = isFocused,
                    onClick = {
                        onZoneFocusChange(zone, index)
                        onSiteClick(site)
                    }
                )
            }
        }
    }
}

@Composable
private fun StreamingSiteTile(
    site: MockData.PopularSite,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(site.color)
    Column(
        modifier = Modifier
            .width(140.dp)
            .auroraCardLift(
                isFocused = isFocused,
                shape = AuroraShapes.Rounded3Xl,
                onFocusedColor = accent
            )
            .auroraLightSweep(isFocused)
            .focusPing(isFocused, accent)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF0A0A0D)),
            contentAlignment = Alignment.Center
        ) {
            val res = MockData.logoResFor(site)
            if (res != 0) {
                Image(
                    painter = painterResource(id = res),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                Text(
                    text = site.name.take(2).uppercase(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F14))
                .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = site.name,
                style = AuroraTypography.Body,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryRow(
    entries: List<HistoryUiModel>,
    focusedZone: String,
    focusedItemIndex: Int,
    onEntryClick: (HistoryUiModel, Int) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Blue, CircleShape)
                )
                Text(
                    text = "Recent History",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = if (entries.isNotEmpty()) "${entries.take(4).count()} shown" else "No entries yet",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        entries.take(4).forEachIndexed { index, entry ->
            val isFocused = focusedZone == "history" && focusedItemIndex == index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .auroraCardLift(
                        isFocused = isFocused,
                        shape = AuroraShapes.RoundedLg,
                        onFocusedColor = entry.accentColor
                    )
                    .auroraLightSweep(isFocused)
                    .clickable { onEntryClick(entry, index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(entry.accentColor.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
                        .border(1.dp, entry.accentColor.copy(alpha = 0.2f), AuroraShapes.RoundedLg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = entry.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = AuroraTypography.Body,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isFocused) entry.accentColor else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.domain,
                            style = AuroraTypography.MonoLabel,
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "${entry.actionVerb} ${entry.timeText}",
                            style = AuroraTypography.MonoLabel,
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 8.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(entry.accentColor.copy(alpha = 0.15f), AuroraShapes.RoundedSm)
                        .border(1.dp, entry.accentColor.copy(alpha = 0.2f), AuroraShapes.RoundedSm)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${entry.visitCount}",
                        style = AuroraTypography.MonoLabel,
                        color = entry.accentColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesRow(
    sites: List<FavoriteSite>,
    focusedZone: String,
    focusedItemIndex: Int,
    onSiteClick: (FavoriteSite, Int) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Purple, CircleShape)
                )
                Text(
                    text = "Speed Dial",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = if (sites.isNotEmpty()) "${sites.size} site${if (sites.size != 1) "s" else ""}" else "No sessions yet",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val lazyListState = rememberLazyListState()
        LaunchedEffect(focusedItemIndex, focusedZone) {
            if (focusedZone == "favorites" && sites.isNotEmpty()) {
                lazyListState.animateScrollToItem(focusedItemIndex.coerceIn(0, sites.size - 1))
            }
        }
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sites.size) { index ->
                val site = sites[index]
                val isFocused = focusedZone == "favorites" && focusedItemIndex == index
                FavoriteCard(
                    site = site,
                    isFocused = isFocused,
                    onClick = { onSiteClick(site, index) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    site: FavoriteSite,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .auroraCardLift(
                isFocused = isFocused,
                shape = AuroraShapes.Rounded3Xl,
                onFocusedColor = site.color
            )
            .auroraLightSweep(isFocused)
            .clickable { onClick() }
            .background(Color.White, AuroraShapes.Rounded3Xl)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (site.faviconBitmap != null) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.graphics.painter.BitmapPainter(site.faviconBitmap.asImageBitmap()),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } else {
            BrandIcon(brand = site.name, size = 48.dp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = site.name,
            style = AuroraTypography.Body,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isFocused) site.color else Color(0xFF1A1A1A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = site.domain.ifEmpty { site.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").split("/").firstOrNull() ?: site.url },
            style = AuroraTypography.MonoLabel,
            color = Color(0xFF666666),
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TrendingSection(
    items: List<TrendingItem>,
    focusedZone: String,
    focusedItemIndex: Int,
    onItemClick: (TrendingItem, Int) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .auroraGlass()
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Purple, CircleShape)
                )
                Text(
                    text = "Trending Today",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "${items.size} trending",
                style = AuroraTypography.MonoLabel,
                color = AuroraColors.Purple,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        items.forEachIndexed { index, item ->
            val isFocused = focusedZone == "trending" && focusedItemIndex == index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .auroraCardLift(
                        isFocused = isFocused,
                        shape = AuroraShapes.RoundedLg
                    )
                    .auroraLightSweep(isFocused)
                    .clickable { onItemClick(item, index) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = AuroraTypography.MonoLabel,
                        color = if (isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.2f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = item.title,
                        style = AuroraTypography.Body,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isFocused) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = item.domain,
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun DownloadsCard(
    downloads: List<DownloadUiModel>,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 160.dp)
            .auroraCardLift(
                isFocused = isFocused,
                shape = AuroraShapes.Rounded3Xl,
                onFocusedColor = AuroraColors.Blue
            )
            .auroraLightSweep(isFocused)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Downloads",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.45f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Downloads",
                tint = AuroraColors.Blue,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (downloads.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AuroraColors.Blue.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, AuroraColors.Blue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = AuroraColors.Blue.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No active downloads",
                    style = AuroraTypography.Body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Completed sandbox files will appear in downloads widget.",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 9.sp
                )
            }
        } else {
            downloads.take(2).forEach { dl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0D).copy(alpha = 0.4f), AuroraShapes.RoundedSm)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dl.fileName,
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${dl.progress}%",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Emerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
            if (downloads.size > 2) {
                Text(
                    text = "${downloads.size} files total",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 8.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    actions: List<QuickActionUiModel>,
    focusedZone: String,
    focusedItemIndex: Int,
    onActionClick: (Int) -> Unit,
    onZoneFocusChange: (String, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AuroraColors.Emerald, CircleShape)
                )
                Text(
                    text = "Control Center Quick Actions",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "SYSTEM CONFIG \u2022 D-PAD INTERACTIVE",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            actions.forEachIndexed { index, action ->
                QuickActionCard(
                    title = action.title,
                    subtitle = action.subtitle,
                    icon = action.icon,
                    accentColor = action.accentColor,
                    isFocused = focusedZone == "quickActions" && focusedItemIndex == index,
                    onClick = { onActionClick(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .auroraCardLift(
                isFocused = isFocused,
                shape = AuroraShapes.Rounded3Xl,
                onFocusedColor = accentColor
            )
            .auroraLightSweep(isFocused)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isFocused) accentColor.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.1f),
                    AuroraShapes.RoundedLg
                )
                .border(1.dp, accentColor.copy(alpha = 0.2f), AuroraShapes.RoundedLg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = AuroraTypography.Body,
                fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isFocused) accentColor else Color.White
            )
            Text(
                text = subtitle,
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 9.sp
            )
        }
    }
}
