package com.aurora.browser.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraSpacing
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.browser.ui.theme.auroraCardLift
import com.aurora.browser.ui.theme.auroraGlass

// Self-contained Mock Model specifications
data class MockVideo(
    val id: String,
    val title: String,
    val channel: String,
    val views: String,
    val time: String,
    val duration: String,
    val thumbnail: String,
    val videoUrl: String,
    val description: String
)

data class MockArticle(
    val title: String,
    val author: String,
    val content: String
)

data class MockFile(
    val id: String,
    val fileName: String,
    val totalSize: String,
    val mimeType: String
)

// Inline database collections
val MOCK_VIDEOS = listOf(
    MockVideo(
        id = "1",
        title = "TV Space Atmosphere - Relaxing Deep Blue Ambient Loop",
        channel = "Celestial Loops",
        views = "24K views",
        time = "3 days ago",
        duration = "02:30",
        thumbnail = "",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        description = "A deep atmospheric space loop perfect for testing smart TV black levels and color contrast ratios."
    ),
    MockVideo(
        id = "2",
        title = "Symphony of Lights - HDR Rec.2020 OLED Cinematic Test",
        channel = "Pixel Pure",
        views = "142K views",
        time = "1 week ago",
        duration = "01:45",
        thumbnail = "",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        description = "4K HDR Rec.2020 color sampling benchmark designed for high-end television panel testing."
    ),
    MockVideo(
        id = "3",
        title = "The Art of Living Glass - Material Design & Blur Showcase",
        channel = "Aurora Labs",
        views = "5K views",
        time = "Just now",
        duration = "03:10",
        thumbnail = "",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        description = "A visual exploration of translucent backdrop filters, corner radius, and atmospheric color layers."
    )
)

val MOCK_ARTICLES = mapOf(
    "living-glass" to MockArticle(
        title = "Living Glass Design Specification",
        author = "Elena Vance (UI Lead)",
        content = """
            Living Glass represents a breakthrough in television user interface philosophy. 
            Historically, TV browsers have utilized flat, low-contrast solid rectangles which result in a heavy, separate appearance on wide OLED panels.
            
            By employing atmospheric noise, a 24dp blur backdrop filter, and a 65% transparency alpha mask, components appear merged with the background living wallpaper. 
            This reduces cognitive load and creates an ambient, warm environment.
            
            Color sampling is performed continuously from the underlying browser tabs, translating the dominant hue into a soft, glowing halo beneath focused elements.
        """.trimIndent()
    ),
    "performance-architecture" to MockArticle(
        title = "Low-Memory State Management on TV Devices",
        author = "Marcus Brody (Core Dev)",
        content = """
            Android TV dongles operate under severe hardware constraints, often packing only 1.5GB of total system RAM.
            
            To support high-fidelity browser simulations without crashing background activities, Aurora introduces a strict state-compaction protocol.
            
            When a tab becomes inactive, its javascript call-stack is fully compressed and written into a local SQLite transactional ledger. 
            The corresponding web viewport is discarded from graphics memory. 
            Upon re-focus, the tab is warm-booted, restoring scroll offsets and input focus in less than 240ms, achieving a 0MB active RAM footprint for background tabs.
        """.trimIndent()
    )
)

val MOCK_FILES = listOf(
    MockFile("1", "aurora_design_manual_v2.pdf", "4.2 MB", "application/pdf"),
    MockFile("2", "aurora_ambient_sunset.mp3", "8.6 MB", "audio/mp3"),
    MockFile("3", "oled_color_test_pattern.png", "12.4 MB", "image/png"),
    MockFile("4", "browser_release_notes.txt", "142 KB", "text/plain")
)

@Composable
fun WebPageSimulator(
    url: String,
    onNavigate: (String) -> Unit,
    onTriggerDownload: (MockFile) -> Unit,
    onRunAISummary: (String) -> Unit,
    aiSummarizing: Boolean,
    aiSummaryResult: String?,
    onCloseAISummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedUrl = url.lowercase().trim()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14161C))
    ) {
        when {
            normalizedUrl.contains("youtube.com") -> YouTubeSimulator(
                onNavigate = onNavigate,
                onTriggerDownload = onTriggerDownload
            )
            normalizedUrl.contains("wikipedia.org") -> WikipediaSimulator(
                normalizedUrl = normalizedUrl,
                onNavigate = onNavigate,
                onRunAISummary = onRunAISummary,
                aiSummarizing = aiSummarizing,
                aiSummaryResult = aiSummaryResult,
                onCloseAISummary = onCloseAISummary
            )
            normalizedUrl.contains("github.com") -> GitHubSimulator(
                onNavigate = onNavigate,
                onTriggerDownload = onTriggerDownload
            )
            normalizedUrl.contains("reddit.com") -> RedditSimulator(
                onNavigate = onNavigate
            )
            normalizedUrl.contains("drive.google.com") -> GoogleDriveSimulator(
                onTriggerDownload = onTriggerDownload
            )
            normalizedUrl.contains("news.google.com") -> GoogleNewsSimulator(
                onNavigate = onNavigate
            )
            normalizedUrl.contains("google.com") || normalizedUrl.contains("search") -> GoogleSearchSimulator(
                normalizedUrl = normalizedUrl,
                onNavigate = onNavigate
            )
            else -> GenericFallbackSimulator(
                url = url,
                onNavigate = onNavigate
            )
        }
    }
}


// ==========================================
// 1. YOUTUBE SIMULATOR
// ==========================================
@Composable
fun YouTubeSimulator(
    onNavigate: (String) -> Unit,
    onTriggerDownload: (MockFile) -> Unit
) {
    var activeVideo by remember { mutableStateOf<MockVideo?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp)
    ) {
        // YouTube Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "YouTube logo",
                    tint = Color(0xFFFF0000),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "YouTube TV",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 18.sp
                )
            }

            // Search box
            var isSearchFocused by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .width(320.dp)
                    .height(36.dp)
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .background(Color(0xFF1F1F1F), AuroraShapes.RoundedSm)
                    .border(
                        1.dp,
                        if (isSearchFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.05f),
                        AuroraShapes.RoundedSm
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = searchQuery.ifEmpty { "Search streams..." },
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 16.dp))

        if (activeVideo != null) {
            // Video View Mode
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Video Player Screen
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .background(Color.Black, AuroraShapes.RoundedLg)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "Playing",
                            tint = AuroraColors.Blue,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Column {
                        Text(text = activeVideo!!.title, style = AuroraTypography.Header, fontSize = 16.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeVideo!!.channel} • ${activeVideo!!.views} • ${activeVideo!!.time}",
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.4f)
                            )

                            TextButtonTV(
                                onClick = {
                                    val streamFile = MOCK_FILES.first()
                                    onTriggerDownload(streamFile)
                                },
                                backgroundColor = Color.White.copy(alpha = 0.05f),
                                borderColor = Color.White.copy(alpha = 0.1f),
                                focusedBorderColor = AuroraColors.Blue
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Download, "Download", tint = AuroraColors.Blue, modifier = Modifier.size(14.dp))
                                    Text("Download Stream", style = AuroraTypography.MonoLabel, color = AuroraColors.Blue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Text(
                            text = activeVideo!!.description,
                            style = AuroraTypography.Body,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), AuroraShapes.RoundedLg)
                                .border(1.dp, Color.White.copy(alpha = 0.03f), AuroraShapes.RoundedLg)
                                .padding(16.dp)
                        )
                    }
                }

                // Right Recommendations Row
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "RECOMMENDED STREAMS", style = AuroraTypography.MonoLabel, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    MOCK_VIDEOS.filter { it.id != activeVideo!!.id }.forEach { vid ->
                        var isVidFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isVidFocused = it.isFocused }
                                .auroraCardLift(isFocused = isVidFocused, shape = AuroraShapes.RoundedMd)
                                .clickable {
                                    activeVideo = vid
                                    onNavigate("https://youtube.com/watch?v=${vid.id}")
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp, 50.dp)
                                    .background(Color.DarkGray, AuroraShapes.RoundedSm)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vid.title,
                                    style = AuroraTypography.Body,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(text = vid.channel, style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButtonTV(
                        onClick = { activeVideo = null },
                        backgroundColor = AuroraColors.Neutral800,
                        focusedBorderColor = AuroraColors.Blue
                    ) {
                        Text("Back to Feed", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Main feed grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MOCK_VIDEOS) { video ->
                    var isCardFocused by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isCardFocused = it.isFocused }
                            .auroraCardLift(isFocused = isCardFocused, shape = AuroraShapes.RoundedLg)
                            .clickable {
                                activeVideo = video
                                onNavigate("https://youtube.com/watch?v=${video.id}")
                            }
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.6f)
                                .background(Color.DarkGray, AuroraShapes.RoundedMd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.8f), AuroraShapes.RoundedSm)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = video.duration, style = AuroraTypography.MonoLabel, fontSize = 8.sp)
                            }
                        }

                        Column {
                            Text(
                                text = video.title,
                                style = AuroraTypography.Body,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                color = if (isCardFocused) AuroraColors.Blue else Color.White
                            )
                            Text(text = video.channel, style = AuroraTypography.MonoLabel, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            Text(text = "${video.views} • ${video.time}", style = AuroraTypography.MonoLabel, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. WIKIPEDIA SIMULATOR
// ==========================================
@Composable
fun WikipediaSimulator(
    normalizedUrl: String,
    onNavigate: (String) -> Unit,
    onRunAISummary: (String) -> Unit,
    aiSummarizing: Boolean,
    aiSummaryResult: String?,
    onCloseAISummary: () -> Unit
) {
    val isPerformance = normalizedUrl.contains("performance")
    val article = if (isPerformance) MOCK_ARTICLES["performance-architecture"]!! else MOCK_ARTICLES["living-glass"]!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        // Wiki Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Wiki",
                    tint = Color(0xFF23252F),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(text = "WIKIPEDIA", style = AuroraTypography.TitleDisplay, color = Color.Black, fontSize = 20.sp)
                    Text(text = "The Free Encyclopedia", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                }
            }

            // AI Summarizer button
            var isAiButtonFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .onFocusChanged { isAiButtonFocused = it.isFocused }
                    .background(
                        if (isAiButtonFocused) AuroraColors.Purple else AuroraColors.Purple.copy(alpha = 0.1f),
                        AuroraShapes.RoundedLg
                    )
                    .border(
                        1.dp,
                        if (isAiButtonFocused) Color.White else AuroraColors.Purple.copy(alpha = 0.3f),
                        AuroraShapes.RoundedLg
                    )
                    .clickable(enabled = !aiSummarizing) { onRunAISummary(article.content) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI",
                        tint = if (isAiButtonFocused) Color.Black else AuroraColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (aiSummarizing) "Summarizing Page..." else "Summarize with AI",
                        style = AuroraTypography.MonoLabel,
                        color = if (isAiButtonFocused) Color.Black else AuroraColors.Purple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 16.dp))

        // AI Summary Results card
        if (aiSummaryResult != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuroraColors.Purple.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .border(1.dp, AuroraColors.Purple.copy(alpha = 0.2f), AuroraShapes.RoundedLg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gemini Smart Page Summary",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Purple,
                        fontWeight = FontWeight.Bold
                    )
                    TextButtonTV(
                        onClick = onCloseAISummary,
                        backgroundColor = Color.LightGray,
                        focusedBorderColor = AuroraColors.Purple
                    ) {
                        Text("Dismiss", style = AuroraTypography.MonoLabel, color = Color.Black)
                    }
                }
                
                Text(
                    text = aiSummaryResult,
                    style = AuroraTypography.Body,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, AuroraShapes.RoundedMd)
                        .border(1.dp, Color.LightGray, AuroraShapes.RoundedMd)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Article Info
        Text(text = article.title, style = AuroraTypography.TitleDisplay, color = Color.Black, fontSize = 28.sp)
        Text(
            text = "From Wikipedia, the free encyclopedia • Author: ${article.author}",
            style = AuroraTypography.MonoLabel,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Internal wiki navigation guide box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), AuroraShapes.RoundedLg)
                .border(1.dp, Color.LightGray, AuroraShapes.RoundedLg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "JUMP TO OTHER ENCYCLOPEDIA ARTICLES", style = AuroraTypography.MonoLabel, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = "1. Living Glass Design",
                    style = AuroraTypography.MonoLabel,
                    color = if (!isPerformance) AuroraColors.Blue else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/living-glass") }
                )
                Text(
                    text = "2. Performance Architecture",
                    style = AuroraTypography.MonoLabel,
                    color = if (isPerformance) AuroraColors.Blue else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/performance-architecture") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main text
        Text(
            text = article.content,
            style = AuroraTypography.Body,
            color = Color(0xFF333333),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}


// ==========================================
// 3. GITHUB SIMULATOR
// ==========================================
@Composable
fun GitHubSimulator(
    onNavigate: (String) -> Unit,
    onTriggerDownload: (MockFile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // GitHub Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit, // GitHub icon mock
                    contentDescription = "GitHub",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "aurora-browser", style = AuroraTypography.Body, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = " / ", style = AuroraTypography.Body, color = Color.Gray)
                    Text(text = "aurora", style = AuroraTypography.Body, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            TextButtonTV(
                onClick = {
                    onTriggerDownload(MOCK_FILES.first())
                },
                backgroundColor = AuroraColors.Emerald.copy(alpha = 0.15f),
                borderColor = AuroraColors.Emerald.copy(alpha = 0.2f),
                focusedBorderColor = AuroraColors.Emerald
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Download, "ZIP", tint = AuroraColors.Emerald, modifier = Modifier.size(14.dp))
                    Text("Download ZIP", style = AuroraTypography.MonoLabel, color = AuroraColors.Emerald, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stats card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f), AuroraShapes.RoundedLg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column { Text("Commits", style = AuroraTypography.MonoLabel, color = Color.Gray); Text("1,420", style = AuroraTypography.Header) }
            Column { Text("Branches", style = AuroraTypography.MonoLabel, color = Color.Gray); Text("4", style = AuroraTypography.Header) }
            Column { Text("Contributors", style = AuroraTypography.MonoLabel, color = Color.Gray); Text("12", style = AuroraTypography.Header) }
            Column { Text("License", style = AuroraTypography.MonoLabel, color = Color.Gray); Text("Apache-2.0", style = AuroraTypography.Header) }
        }

        // Files List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .clip(AuroraShapes.RoundedLg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("File Name", style = AuroraTypography.MonoLabel, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("Size", style = AuroraTypography.MonoLabel, color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            MOCK_FILES.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161B22))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.List, "File", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text(text = file.fileName, style = AuroraTypography.Body)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = file.totalSize, style = AuroraTypography.MonoLabel, color = Color.Gray)
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = AuroraColors.Blue,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onTriggerDownload(file) }
                        )
                    }
                }
                Divider(color = Color.White.copy(alpha = 0.05f))
            }
        }

        // README section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f), AuroraShapes.RoundedLg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "README.md", style = AuroraTypography.Header, fontSize = 14.sp)
            Text(
                text = "Aurora is a premium web browser designed specifically for televisions, with an elegant, completely custom dark-themed Compose UI. It features multi-tab groups, local media viewports, and process monitoring dashboards.",
                style = AuroraTypography.Body,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "View Design Specs",
                    style = AuroraTypography.MonoLabel,
                    color = AuroraColors.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/living-glass") }
                )
                Text(
                    text = "Performance Details",
                    style = AuroraTypography.MonoLabel,
                    color = AuroraColors.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/performance-architecture") }
                )
            }
        }
    }
}


// ==========================================
// 4. REDDIT SIMULATOR
// ==========================================
@Composable
fun RedditSimulator(
    onNavigate: (String) -> Unit
) {
    var upvoteCount1 by remember { mutableStateOf(142) }
    var upvoteCount2 by remember { mutableStateOf(89) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reddit Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Reddit",
                    tint = Color(0xFFFF4500),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "reddit /r/AuroraBrowser",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 16.sp
                )
            }
            Text("Trending Discussions", style = AuroraTypography.MonoLabel, color = Color.Gray)
        }

        Divider(color = Color.White.copy(alpha = 0.05f))

        // Feed Post 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1B), AuroraShapes.RoundedLg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Upvote",
                    tint = AuroraColors.Amber,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { upvoteCount1++ }
                )
                Text(text = "$upvoteCount1", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Posted by u/ElenaDesign • 3 hours ago", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 8.sp)
                Text(
                    text = "Is \"Living Glass\" the best TV browser design we have seen so far?",
                    style = AuroraTypography.Header,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/living-glass") }
                )
                Text(
                    text = "I have been exploring TV Bro and some Android TV WebViews, and they all feel very heavy. The translucency and heavy blur specs of Aurora are gorgeous.",
                    style = AuroraTypography.Body,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // Feed Post 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1B), AuroraShapes.RoundedLg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Upvote",
                    tint = AuroraColors.Amber,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { upvoteCount2++ }
                )
                Text(text = "$upvoteCount2", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Posted by u/DeveloperPro • Yesterday", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 8.sp)
                Text(
                    text = "How Aurora manages tab memory budgets on standard 1.5GB Google TV dongles",
                    style = AuroraTypography.Header,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigate("https://wikipedia.org/wiki/performance-architecture") }
                )
                Text(
                    text = "The concept of automatically compressing JavaScript states and using SQLite to track scroll positions is amazing. It allows background tabs to take 0MB of active RAM.",
                    style = AuroraTypography.Body,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}


// ==========================================
// 5. GOOGLE DRIVE SIMULATOR
// ==========================================
@Composable
fun GoogleDriveSimulator(
    onTriggerDownload: (MockFile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131314))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Drive",
                    tint = AuroraColors.Emerald,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(text = "Google Drive", style = AuroraTypography.TitleDisplay, fontSize = 18.sp)
                    Text(text = "Cloud Sandbox Workspace", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                }
            }
            Text("Secure local storage files", style = AuroraTypography.MonoLabel, color = Color.Gray)
        }

        Divider(color = Color.White.copy(alpha = 0.05f))

        // Grid folders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriveFolderCard(title = "Manuals / Docs", desc = "1 PDF file available", icon = Icons.Default.List, color = AuroraColors.Blue)
            DriveFolderCard(title = "Music Beats", desc = "1 MP3 track available", icon = Icons.Default.PlayArrow, color = AuroraColors.Purple)
            DriveFolderCard(title = "Gallery Photos", desc = "1 PNG image available", icon = Icons.Default.Star, color = AuroraColors.Emerald)
            DriveFolderCard(title = "Release Logs", desc = "1 text file available", icon = Icons.Default.List, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "STORED SANDBOXED FILES", style = AuroraTypography.MonoLabel, color = Color.Gray, fontWeight = FontWeight.Bold)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(MOCK_FILES) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E20), AuroraShapes.RoundedLg)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.List, "File", tint = AuroraColors.Blue, modifier = Modifier.size(18.dp))
                        Column {
                            Text(text = file.fileName, style = AuroraTypography.Body, fontWeight = FontWeight.Bold)
                            Text(text = "${file.mimeType} • ${file.totalSize}", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                        }
                    }

                    TextButtonTV(
                        onClick = { onTriggerDownload(file) },
                        backgroundColor = AuroraColors.Neutral800,
                        focusedBorderColor = AuroraColors.Blue
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Download, "Download", tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("Download File", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DriveFolderCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(90.dp)
            .background(AuroraColors.Neutral900, AuroraShapes.RoundedLg)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, title, tint = color, modifier = Modifier.size(24.dp))
        Column {
            Text(text = title, style = AuroraTypography.Body, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(text = desc, style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 8.sp)
        }
    }
}


// ==========================================
// 6. GOOGLE NEWS SIMULATOR
// ==========================================
@Composable
fun GoogleNewsSimulator(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Icon(Icons.Default.List, "News", tint = Color(0xFF4285F4), modifier = Modifier.size(24.dp))
                Text(text = "Google News TV", style = AuroraTypography.TitleDisplay, fontSize = 18.sp)
            }
            Text(text = "Trending Stories", style = AuroraTypography.MonoLabel, color = Color.Gray)
        }

        Divider(color = Color.White.copy(alpha = 0.05f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NewsCard(
                title = "Living Glass: The Next Frontiers of Smart TV Web Aesthetics",
                desc = "Read the official specification covering layout, spacing, corner radius, and ambient focus glows.",
                category = "Design & UI",
                tagColor = AuroraColors.Blue,
                onClick = { onNavigate("https://wikipedia.org/wiki/living-glass") },
                modifier = Modifier.weight(1f)
            )

            NewsCard(
                title = "Low-Memory Solutions: Tab Discarding and Client Cache Models on Lean Sticks",
                desc = "Learn how memory compression allows multi-tab processes to run smoothly under tight hardware constraints.",
                category = "Performance",
                tagColor = AuroraColors.Purple,
                onClick = { onNavigate("https://wikipedia.org/wiki/performance-architecture") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NewsCard(
    title: String,
    desc: String,
    category: String,
    tagColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .height(180.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .auroraCardLift(isFocused = isFocused, shape = AuroraShapes.RoundedLg, onFocusedColor = tagColor)
            .clickable { onClick() }
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .background(tagColor.copy(alpha = 0.15f), AuroraShapes.RoundedSm)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = category.uppercase(), style = AuroraTypography.MonoLabel, color = tagColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = title, style = AuroraTypography.Header, fontSize = 14.sp, maxLines = 2)
        }
        Text(text = desc, style = AuroraTypography.Body, color = Color.Gray, fontSize = 11.sp, maxLines = 2)
    }
}


// ==========================================
// 7. GOOGLE SEARCH & RESULTS
// ==========================================
@Composable
fun GoogleSearchSimulator(
    normalizedUrl: String,
    onNavigate: (String) -> Unit
) {
    val isSearchMode = normalizedUrl.contains("search")
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isSearchMode) Arrangement.Top else Arrangement.Center
    ) {
        if (isSearchMode) {
            // SEARCH RESULTS VIEW
            Column(
                modifier = Modifier.width(620.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // mini search row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Google",
                        style = AuroraTypography.TitleDisplay,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { onNavigate("https://google.com") }
                    )

                    var isSearchRowFocused by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .onFocusChanged { isSearchRowFocused = it.isFocused }
                            .background(Color(0xFF262626), AuroraShapes.RoundedLg)
                            .border(1.dp, if (isSearchRowFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Search Google...", style = AuroraTypography.MonoLabel, color = Color.Gray)
                        Icon(Icons.Default.Search, "Search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Simulated Web Results:", style = AuroraTypography.MonoLabel, color = Color.Gray)

                SearchResultCard(
                    breadcrumbs = "wikipedia.org › wiki › living-glass",
                    title = "Living Glass Design Specification — Wikipedia",
                    desc = "Read about the visual characteristics of atmospheric noise, translucent backdrop-filters, and color-sampled ambient glows on TV devices.",
                    onClick = { onNavigate("https://wikipedia.org/wiki/living-glass") }
                )

                SearchResultCard(
                    breadcrumbs = "wikipedia.org › wiki › performance",
                    title = "Atmospheric Rendering & Tab Compressions — Wikipedia",
                    desc = "Understanding active, background, sleeping, and discarded tab states. Optimization methods for low-resource TV dongles.",
                    onClick = { onNavigate("https://wikipedia.org/wiki/performance-architecture") }
                )

                SearchResultCard(
                    breadcrumbs = "github.com › aurora-browser › aurora",
                    title = "Aurora TV Browser Source Repository — GitHub",
                    desc = "Access manuals, spec docs, release notes, and downloadeable PDF drafts inside the official code-base of Aurora v2.0.",
                    onClick = { onNavigate("https://github.com/aurora-browser/aurora") }
                )
            }
        } else {
            // GOOGLE HOME SCREEN VIEW
            Column(
                modifier = Modifier.width(360.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Google",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                )

                var isHomeSearchFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .onFocusChanged { isHomeSearchFocused = it.isFocused }
                        .background(Color(0xFF262626), AuroraShapes.RoundedLg)
                        .border(1.dp, if (isHomeSearchFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { onNavigate("https://google.com/search") },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Search Google or type a URL...", style = AuroraTypography.MonoLabel, color = Color.Gray)
                    Icon(Icons.Default.Search, "Search", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButtonTV(onClick = { onNavigate("https://youtube.com") }, backgroundColor = Color(0xFF262626), focusedBorderColor = Color.White) {
                        Text("YouTube Feed", style = AuroraTypography.MonoLabel, color = Color.LightGray)
                    }
                    TextButtonTV(onClick = { onNavigate("https://drive.google.com") }, backgroundColor = Color(0xFF262626), focusedBorderColor = Color.White) {
                        Text("Drive Sandbox", style = AuroraTypography.MonoLabel, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    breadcrumbs: String,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .auroraCardLift(isFocused = isFocused, shape = AuroraShapes.RoundedLg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = breadcrumbs, style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
        Text(text = title, style = AuroraTypography.Header, fontSize = 14.sp, color = AuroraColors.Blue, fontWeight = FontWeight.Bold)
        Text(text = desc, style = AuroraTypography.Body, color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
    }
}


// ==========================================
// 8. GENERIC FALLBACK SIMULATOR
// ==========================================
@Composable
fun GenericFallbackSimulator(
    url: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(AuroraColors.Neutral900, AuroraShapes.Rounded3Xl)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Language, "Web", tint = AuroraColors.Blue, modifier = Modifier.size(36.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = url.ifEmpty { "Unnamed Domain" }, style = AuroraTypography.Header, fontSize = 14.sp)
                Text(
                    text = "This page represents a custom or external webpage in our TV browser simulator.",
                    style = AuroraTypography.Body,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            TextButtonTV(
                onClick = { onNavigate("https://google.com") },
                backgroundColor = AuroraColors.Blue,
                focusedBorderColor = Color.White
            ) {
                Text("Go to Google Home", style = AuroraTypography.MonoLabel, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
