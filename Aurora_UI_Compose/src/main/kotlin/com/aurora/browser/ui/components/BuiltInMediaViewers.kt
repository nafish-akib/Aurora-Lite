package com.aurora.browser.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.delay

// Data structure corresponding to Download model
data class Download(
    val id: String,
    val fileName: String,
    val url: String,
    val totalSize: String,
    val mimeType: String,
    val fileData: FileSpecData? = null
)

data class FileSpecData(
    val title: String? = null,
    val subtitle: String? = null,
    val pages: List<String> = emptyList(),
    val artist: String? = null,
    val album: String? = null,
    val duration: String? = null,
    val cover: String? = null
)

// ==========================================
// 1. PDF VIEWER
// ==========================================
@Composable
fun PDFViewer(
    download: Download,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(1) }
    var zoom by remember { mutableStateOf(100) }
    var nightMode by remember { mutableStateOf(false) }

    val spec = download.fileData ?: FileSpecData(pages = listOf("Page 1: Default Spec Content"))
    val totalPages = spec.pages.size.coerceAtLeast(1)

    val contentBg = if (nightMode) Color(0xFF17181F) else Color(0xFFF5F6F8)
    val textColors = if (nightMode) Color(0xFFE3E4E8) else Color(0xFF1F2026)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (nightMode) Color(0xFF0A0B0E) else Color.White)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
    ) {
        // PDF Viewer Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "PDF Reader",
                    tint = AuroraColors.Blue,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = download.fileName,
                        style = AuroraTypography.Header,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Built-In PDF Engine v2.0",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp
                    )
                }
            }

            // PDF Top Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Night Mode
                IconButtonTV(
                    onClick = { nightMode = !nightMode },
                    contentDescription = "Night Mode"
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Theme",
                        tint = if (nightMode) AuroraColors.Amber else AuroraColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Zoom Row Control
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(AuroraColors.Neutral800, AuroraShapes.RoundedMd)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { zoom = (zoom - 25).coerceAtLeast(50) }
                    )
                    Text(
                        text = "$zoom%",
                        style = AuroraTypography.MonoLabel,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { zoom = (zoom + 25).coerceAtMost(200) }
                    )
                }

                TextButtonTV(
                    onClick = onClose,
                    backgroundColor = AuroraColors.Red.copy(alpha = 0.15f),
                    borderColor = AuroraColors.Red.copy(alpha = 0.3f),
                    focusedBorderColor = AuroraColors.Red
                ) {
                    Text(
                        text = "Close Viewer",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Document Sheet Viewer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (nightMode) Color(0xFF0C0D12) else Color(0xFFE5E7EB))
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val scale = zoom.toFloat() / 100f
            Column(
                modifier = Modifier
                    .width(540.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(contentBg, AuroraShapes.RoundedLg)
                    .border(
                        1.dp,
                        if (nightMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.1f),
                        AuroraShapes.RoundedLg
                    )
                    .padding(32.dp)
            ) {
                // Header Stamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AURORA PDF READER",
                        style = AuroraTypography.MonoLabel,
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "SECURE LOCAL SANDBOX",
                        style = AuroraTypography.MonoLabel,
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = spec.title ?: "Document Outline",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 20.sp,
                    color = AuroraColors.Blue
                )
                Text(
                    text = spec.subtitle ?: "Confidential Reference Manual",
                    style = AuroraTypography.Body,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Page text body
                Text(
                    text = spec.pages.getOrNull(currentPage - 1) ?: "End of Document",
                    style = AuroraTypography.Body,
                    color = textColors,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.heightIn(min = 180.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Divider(color = Color.Gray.copy(alpha = 0.2f))
                Text(
                    text = "Page $currentPage of $totalPages • Encrypted Cache",
                    style = AuroraTypography.MonoLabel,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }

        // Pager control footers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButtonTV(
                onClick = { currentPage = (currentPage - 1).coerceAtLeast(1) },
                backgroundColor = AuroraColors.Neutral800,
                borderColor = Color.White.copy(alpha = 0.05f),
                focusedBorderColor = AuroraColors.Blue
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, "Prev", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("PREVIOUS PAGE", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "PAGE $currentPage / $totalPages",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.8f)
            )

            TextButtonTV(
                onClick = { currentPage = (currentPage + 1).coerceAtMost(totalPages) },
                backgroundColor = AuroraColors.Neutral800,
                borderColor = Color.White.copy(alpha = 0.05f),
                focusedBorderColor = AuroraColors.Blue
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("NEXT PAGE", style = AuroraTypography.MonoLabel, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}


// ==========================================
// 2. VIDEO PLAYER
// ==========================================
@Composable
fun VideoPlayer(
    download: Download,
    onClose: () -> Unit,
    onTogglePiP: ((Boolean) -> Unit)? = null,
    isPiPActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var speed by remember { mutableStateOf(1.0f) }

    // Dynamic elapsed time simulation while playing
    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                delay(1000)
                progress = (progress + (speed * 0.5f)).coerceAtMost(100f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .border(
                1.dp,
                if (isPiPActive) AuroraColors.Blue.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                AuroraShapes.RoundedLg
            )
    ) {
        // Video viewport simulation (using a visual cover layout)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder/simulated video wallpaper
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AuroraColors.Blue.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Video Renderer",
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(100.dp)
            )
        }

        // PiP Active Overlay Cover
        if (isPiPActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "PiP Mode",
                        tint = AuroraColors.Blue,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Picture-in-Picture Mode Active",
                        style = AuroraTypography.Header,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "This video is currently floating in the corner.",
                        style = AuroraTypography.Body,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButtonTV(
                        onClick = { onTogglePiP?.invoke(false) },
                        backgroundColor = AuroraColors.Blue,
                        focusedBorderColor = Color.White
                    ) {
                        Text(
                            text = "Restore Full Player",
                            style = AuroraTypography.MonoLabel,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Video Player UI Controller (hidden in picture-in-picture)
        if (!isPiPActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(AuroraColors.Neutral900)
                    .border(1.dp, Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Seek Bar Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val minutesElapsed = (progress * 0.025f).toInt()
                    val secondsElapsed = ((progress * 0.025f - minutesElapsed) * 60).toInt()
                    Text(
                        text = String.format("%02d:%02d", minutesElapsed, secondsElapsed),
                        style = AuroraTypography.MonoLabel
                    )

                    // Progress Slider simulated
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress / 100f)
                                .fillMaxHeight()
                                .background(AuroraColors.Blue, CircleShape)
                        )
                    }

                    Text(
                        text = "02:30",
                        style = AuroraTypography.MonoLabel
                    )
                }

                // Controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play/Pause button
                        IconButtonTV(
                            onClick = { playing = !playing },
                            contentDescription = "Play/Pause"
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(4.dp)
                            )
                        }

                        Column {
                            Text(text = download.fileName, style = AuroraTypography.Header, fontSize = 13.sp)
                            Text(text = "OLED Full-Screen Playback", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Playback Speed Toggle
                        TextButtonTV(
                            onClick = { speed = if (speed >= 2.0f) 1.0f else speed + 0.25f },
                            backgroundColor = AuroraColors.Neutral800,
                            focusedBorderColor = AuroraColors.Blue
                        ) {
                            Text(
                                text = String.format("%.2fx Speed", speed),
                                style = AuroraTypography.MonoLabel,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (onTogglePiP != null) {
                            IconButtonTV(
                                onClick = { onTogglePiP(true) },
                                contentDescription = "Picture-in-Picture"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "PiP",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButtonTV(
                            onClick = onClose,
                            backgroundColor = AuroraColors.Red.copy(alpha = 0.1f),
                            borderColor = AuroraColors.Red.copy(alpha = 0.3f),
                            focusedBorderColor = AuroraColors.Red
                        ) {
                            Text(
                                text = "Close Player",
                                style = AuroraTypography.MonoLabel,
                                color = AuroraColors.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. AUDIO PLAYER
// ==========================================
@Composable
fun AudioPlayer(
    download: Download,
    onClose: () -> Unit,
    isPlayingBackground: Boolean,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var playing by remember { mutableStateOf(isPlayingBackground) }
    val audioMeta = download.fileData ?: FileSpecData(
        title = "Ambient Track",
        artist = "Aurora Sound",
        album = "Sunset Spec",
        cover = ""
    )

    // dynamic equalizers state list
    var waves by remember { mutableStateOf(List(24) { (20..100).random() }) }

    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                delay(150)
                waves = List(24) { (20..100).random() }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraColors.BgInput)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
            .padding(32.dp)
    ) {
        // Background radial ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AuroraColors.Purple.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
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
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Audio Player",
                        tint = AuroraColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AURORA AMBIENT AUDIO PLAYER",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }

                TextButtonTV(
                    onClick = onClose,
                    backgroundColor = AuroraColors.Neutral900,
                    focusedBorderColor = Color.White
                ) {
                    Text(
                        text = "Hide Player",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Cover and Visualizer Content Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // cover photo visualizer
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.DarkGray, AuroraShapes.RoundedLg)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedLg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Album Art",
                        tint = AuroraColors.Purple,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Track details
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = audioMeta.title ?: "Ambient Track", style = AuroraTypography.Header)
                    Text(
                        text = "${audioMeta.artist} — ${audioMeta.album}",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Equalizer Bar visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    waves.forEachIndexed { i, waveHeight ->
                        val heightFraction = if (playing) waveHeight.toFloat() / 100f else 0.1f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFraction)
                                .background(AuroraColors.Purple, AuroraShapes.RoundedSm)
                                .alpha(if (playing) 0.8f else 0.3f)
                        )
                    }
                }
            }

            // Controls Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButtonTV(onClick = {}, contentDescription = "Skip Back") {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White.copy(alpha = 0.4f))
                    }

                    // Play core circle trigger
                    var isPlayFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .onFocusChanged { isPlayFocused = it.isFocused }
                            .auroraCardLift(
                                isFocused = isPlayFocused,
                                shape = RoundedCornerShape(50.dp),
                                onFocusedColor = AuroraColors.Purple
                            )
                            .background(AuroraColors.Purple, CircleShape)
                            .clickable {
                                playing = !playing
                                onToggleBackgroundPlay(playing)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButtonTV(onClick = {}, contentDescription = "Skip Forward") {
                        Icon(Icons.Default.ArrowForward, "Next", tint = Color.White.copy(alpha = 0.4f))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("STATUS:", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.3f))
                    Text(
                        text = if (playing) "PLAYING BACKGROUND CHANNELS" else "STANDBY",
                        style = AuroraTypography.MonoLabel,
                        color = if (playing) AuroraColors.Emerald else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ==========================================
// 4. IMAGE GALLERY
// ==========================================
@Composable
fun ImageGallery(
    download: Download,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slideshow by remember { mutableStateOf(false) }
    var zoom by remember { mutableStateOf(100) }

    LaunchedEffect(slideshow) {
        if (slideshow) {
            while (true) {
                delay(4000)
                zoom = if (zoom == 100) 105 else 100
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030304))
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
    ) {
        // Image Gallery Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = AuroraColors.Emerald,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(text = download.fileName, style = AuroraTypography.Header, fontSize = 13.sp)
                    Text(text = "Aurora Media Gallery 4K", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Slideshow Toggle
                TextButtonTV(
                    onClick = { slideshow = !slideshow },
                    backgroundColor = if (slideshow) AuroraColors.Emerald.copy(alpha = 0.2f) else AuroraColors.Neutral850,
                    borderColor = if (slideshow) AuroraColors.Emerald else Color.White.copy(alpha = 0.05f),
                    focusedBorderColor = AuroraColors.Emerald
                ) {
                    Text(
                        text = if (slideshow) "Slideshow: ON" else "Slideshow: OFF",
                        style = AuroraTypography.MonoLabel,
                        color = if (slideshow) AuroraColors.Emerald else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Zoom control buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(AuroraColors.Neutral800, AuroraShapes.RoundedMd)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { zoom = (zoom - 25).coerceAtLeast(50) }
                    )
                    Text(
                        text = "$zoom%",
                        style = AuroraTypography.MonoLabel,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { zoom = (zoom + 25).coerceAtMost(200) }
                    )
                }

                TextButtonTV(
                    onClick = onClose,
                    backgroundColor = AuroraColors.Red.copy(alpha = 0.1f),
                    borderColor = AuroraColors.Red.copy(alpha = 0.3f),
                    focusedBorderColor = AuroraColors.Red
                ) {
                    Text(
                        text = "Close Viewer",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Image display Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            val scale = zoom.toFloat() / 100f
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
            ) {
                // Simulated image frame wallpaper
                Box(
                    modifier = Modifier
                        .size(380.dp, 220.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AuroraColors.Emerald.copy(alpha = 0.15f),
                                    AuroraColors.Blue.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Media Item",
                        tint = AuroraColors.Emerald,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        // Image bottom stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dim: 3840 × 2160 pixels • ${download.totalSize}",
                style = AuroraTypography.MonoLabel,
                color = Color.Gray
            )
            Text(
                text = "Color Profile: HDR (Rec. 2020)",
                style = AuroraTypography.MonoLabel,
                color = Color.Gray
            )
        }
    }
}


// ==========================================
// 5. TEXT VIEWER
// ==========================================
@Composable
fun TextViewer(
    download: Download,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wordWrap by remember { mutableStateOf(true) }
    val textContent = (download.fileData?.pages?.getOrNull(0) ?: "Default log content.")
    val lines = textContent.split("\n")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080B))
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
    ) {
        // Text top headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Text Viewer",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(text = download.fileName, style = AuroraTypography.Header, fontSize = 13.sp)
                    Text(text = "Aurora Document Engine", style = AuroraTypography.MonoLabel, color = Color.Gray, fontSize = 9.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButtonTV(
                    onClick = { wordWrap = !wordWrap },
                    backgroundColor = if (wordWrap) Color.White.copy(alpha = 0.15f) else AuroraColors.Neutral800,
                    focusedBorderColor = Color.White
                ) {
                    Text(
                        text = if (wordWrap) "Wrap: ON" else "Wrap: OFF",
                        style = AuroraTypography.MonoLabel,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButtonTV(
                    onClick = onClose,
                    backgroundColor = AuroraColors.Red.copy(alpha = 0.1f),
                    borderColor = AuroraColors.Red.copy(alpha = 0.3f),
                    focusedBorderColor = AuroraColors.Red
                ) {
                    Text(
                        text = "Close",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Text display body with gutter line numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF020304))
                .padding(24.dp)
        ) {
            // Left Line Numbers Gutter
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .padding(end = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.forEachIndexed { idx, _ ->
                    Text(
                        text = "${idx + 1}",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Gutter Divider Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            // Content pre scroll viewport
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = textContent,
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Text bottom indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Neutral900.copy(alpha = 0.6f))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lines: ${lines.size} • Size: ${download.totalSize}",
                style = AuroraTypography.MonoLabel,
                color = Color.Gray
            )
            Text(
                text = "UTF-8 Document",
                style = AuroraTypography.MonoLabel,
                color = Color.Gray
            )
        }
    }
}


// Color definitions specifically helper on icons or custom button backgrounds
val Color.Companion.Neutral850: Color get() = Color(0xFF1B1D25)
