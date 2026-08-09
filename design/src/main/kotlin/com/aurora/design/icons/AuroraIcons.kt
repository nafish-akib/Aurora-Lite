package com.aurora.design.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object AuroraIcons {
    val Search get() = Icons.Default.Search
    val Settings get() = Icons.Default.Settings
    val Home get() = Icons.Default.Home
    val Close get() = Icons.Default.Close
    val Book get() = Icons.Default.Book
    val Bookmark get() = Icons.Default.Bookmark
    val Mic get() = Icons.Default.Mic
    val Download get() = Icons.Default.Download
    val FileDownload get() = Icons.Default.FileDownload
    val Delete get() = Icons.Default.Delete
    val Check get() = Icons.Default.Check
    val ChevronLeft get() = Icons.Default.ChevronLeft
    val ChevronRight get() = Icons.Default.ChevronRight
    val Tv get() = Icons.Default.Tv
    val Terminal get() = Icons.Default.Terminal
    val Wifi get() = Icons.Default.Wifi
    val Shield get() = Icons.Default.Shield
    val MusicNote get() = Icons.Default.MusicNote
    val Warning get() = Icons.Default.Warning
    val Link get() = Icons.Default.Link
    val DesktopWindows get() = Icons.Default.DesktopWindows
    val AutoAwesome get() = Icons.Default.AutoAwesome
    val LightMode get() = Icons.Default.LightMode
    val DarkMode get() = Icons.Default.DarkMode
    val ZoomIn get() = Icons.Default.ZoomIn
    val ZoomOut get() = Icons.Default.ZoomOut
    val PlayArrow get() = Icons.Default.PlayArrow
    val Pause get() = Icons.Default.Pause
    val SkipNext get() = Icons.Default.SkipNext
    val SkipPrevious get() = Icons.Default.SkipPrevious
    val Movie get() = Icons.Default.Movie
    val Image get() = Icons.Default.Image
    val Keyboard get() = Icons.Default.Keyboard
    val KeyboardReturn get() = Icons.AutoMirrored.Filled.KeyboardReturn
    val Backspace get() = Icons.AutoMirrored.Filled.Backspace
}

fun siteIcon(iconName: String): ImageVector = when (iconName) {
    "Youtube" -> Icons.Default.Tv
    "Github" -> Icons.Default.Terminal
    "BookOpen", "Newspaper" -> Icons.Default.Book
    "MessageSquare" -> Icons.Default.Link
    "HardDrive" -> Icons.Default.DesktopWindows
    else -> Icons.Default.Book
}

@Composable
fun AuroraSiteIcon(iconName: String, tint: Color = Color.White, modifier: Modifier = Modifier) {
    Icon(siteIcon(iconName), contentDescription = null, tint = tint, modifier = modifier)
}
