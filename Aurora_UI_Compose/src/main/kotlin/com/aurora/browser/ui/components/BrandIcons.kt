package com.aurora.browser.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val YouTubeRed = Color(0xFFFF0000)
private val GitHubBg = Color(0xFF1F1F1F)
private val WikipediaBg = Color(0xFF333333)
private val RedditOrange = Color(0xFFFF4500)
private val GoogleDriveGreen = Color(0xFF34A853)
private val GoogleNewsBlue = Color(0xFF4285F4)
private val TwitchPurple = Color(0xFF9146FF)
private val NetflixRed = Color(0xFFE50914)
private val FacebookBlue = Color(0xFF1877F2)
private val GoogleBlue = Color(0xFF4285F4)

@Composable
fun BrandIcon(
    brand: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val name = brand.lowercase()
    val bgColor = brandColor(name)
    Box(
        modifier = modifier
            .size(size)
            .background(Color.White, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            when {
                name.contains("youtube") -> {
                    drawRoundRect(YouTubeRed, cornerRadius = CornerRadius(w * 0.18f, w * 0.18f), size = Size(w, h))
                    val tri = Path().apply {
                        moveTo(w * 0.30f, h * 0.28f)
                        lineTo(w * 0.78f, h * 0.50f)
                        lineTo(w * 0.30f, h * 0.72f)
                        close()
                    }
                    drawPath(tri, Color.White)
                }
                name.contains("github") -> {
                    val r = w / 2f
                    drawCircle(GitHubBg, radius = r)
                    drawCircle(Color.White, radius = r * 0.35f, center = Offset(w * 0.50f, h * 0.42f))
                    drawCircle(Color.White, radius = r * 0.20f, center = Offset(w * 0.35f, h * 0.62f))
                }
                name.contains("wikipedia") -> {
                    drawCircle(WikipediaBg, radius = w / 2f)
                }
                name.contains("reddit") -> {
                    drawCircle(RedditOrange, radius = w / 2f)
                    drawCircle(Color.White, radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.40f))
                    drawLine(Color.White, Offset(w * 0.35f, h * 0.55f), Offset(w * 0.65f, h * 0.55f), strokeWidth = w * 0.06f)
                }
                name.contains("drive") -> {
                    val path = Path().apply {
                        moveTo(w * 0.15f, h * 0.30f)
                        lineTo(w * 0.50f, h * 0.05f)
                        lineTo(w * 0.85f, h * 0.30f)
                        close()
                    }
                    drawPath(path, GoogleDriveGreen)
                    drawRect(Color(0xFFFFD600), topLeft = Offset(0f, h * 0.50f), size = Size(w * 0.85f, h * 0.40f))
                    drawLine(Color.White, Offset(w * 0.10f, h * 0.50f), Offset(w * 0.50f, h * 0.85f), strokeWidth = w * 0.05f)
                }
                name.contains("news") -> {
                    drawCircle(GoogleNewsBlue, radius = w / 2f)
                }
                name.contains("twitch") -> {
                    drawRect(TwitchPurple, size = Size(w, h))
                    drawRect(Color.White, topLeft = Offset(w * 0.30f, h * 0.25f), size = Size(w * 0.15f, h * 0.50f))
                    drawRect(Color.White, topLeft = Offset(w * 0.55f, h * 0.25f), size = Size(w * 0.15f, h * 0.50f))
                }
                name.contains("netflix") -> {
                    drawRect(NetflixRed, size = Size(w, h))
                    drawRect(Color.White, topLeft = Offset(w * 0.20f, h * 0.10f), size = Size(w * 0.18f, h * 0.80f))
                    drawRect(Color.White, topLeft = Offset(w * 0.62f, h * 0.10f), size = Size(w * 0.18f, h * 0.80f))
                }
                name.contains("facebook") -> {
                    drawCircle(FacebookBlue, radius = w / 2f)
                }
                else -> {
                    drawCircle(bgColor, radius = w / 2f)
                }
            }
        }

        if (name.contains("wikipedia")) {
            Text("W", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (name.contains("news") || name.contains("google news")) {
            Text("N", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (name.contains("facebook")) {
            Text("f", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (name.contains("google") && !name.contains("news") && !name.contains("drive")) {
            Text("G", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private fun brandColor(name: String): Color = when {
    name.contains("youtube") -> YouTubeRed
    name.contains("github") -> GitHubBg
    name.contains("wikipedia") -> WikipediaBg
    name.contains("reddit") -> RedditOrange
    name.contains("drive") -> GoogleDriveGreen
    name.contains("news") -> GoogleNewsBlue
    name.contains("twitch") -> TwitchPurple
    name.contains("netflix") -> NetflixRed
    name.contains("facebook") -> FacebookBlue
    else -> GoogleBlue
}
