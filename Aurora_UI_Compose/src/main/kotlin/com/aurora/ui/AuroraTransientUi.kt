package com.aurora.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.components.BrowserCoordinator
import com.aurora.browser.ui.components.HomeCoordinator
import com.aurora.browser.ui.components.SettingsCoordinator
import com.aurora.browser.ui.components.SystemKeyboardInput
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.theme.AuroraTheme
import com.aurora.ui.theme.Particle
import com.aurora.ui.theme.rememberBrandDotPulse
import com.aurora.ui.types.Tab
import kotlinx.coroutines.delay

@Composable
internal fun RuntimeWarmupScreen() {
    AuroraTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF070709)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "AURORA",
                    color = AuroraColors.white,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Text(
                    "Starting browser engine",
                    color = AuroraColors.white40,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
internal fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AuroraColors.white40, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = AuroraColors.white, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun BoxScope.AuroraTransientUi(
    browser: BrowserCoordinator,
    home: HomeCoordinator,
    settings: SettingsCoordinator,
    uiTabs: List<Tab>,
    showSplash: Boolean,
    splashStep: Int,
    particles: List<Particle>,
    onWebNavigation: (String) -> Unit,
    realFps: Int = 0,
    realMemory: Int = 0,
    realCpu: Int = 0,
    realGpu: Int = 0,
    realNetwork: Long = 0
) {
    val context = LocalContext.current
    if (browser.isKeyboardOpen) {
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .fillMaxWidth(0.6f)
        ) {
            SystemKeyboardInput(
                initialText = home.searchQuery,
                onTextChange = { home.searchQuery = it },
                onSubmit = {
                    onWebNavigation(home.searchQuery)
                    home.searchQuery = ""
                },
                onDismiss = {
                    browser.isKeyboardOpen = false
                    home.isOmniboxFocused = false
                    val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                    val activity = context as? android.app.Activity
                    val token = activity?.window?.decorView?.windowToken
                    if (imm != null && token != null) {
                        runCatching { imm.hideSoftInputFromWindow(token, 0) }
                    }
                }
            )
        }
    }

    if (settings.isPerfOverlayEnabled) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 24.dp)
                .background(AuroraColors.neutral950.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                .border(1.dp, AuroraColors.white10, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .width(200.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Developer HUD",
                        color = AuroraColors.auroraBlue,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    val lp = rememberBrandDotPulse()
                    Text("• LIVE", color = AuroraColors.auroraEmerald.copy(alpha = lp), fontSize = 8.sp)
                }
                if (settings.isFpsCounterEnabled) StatRow("FPS", if (realFps > 0) "$realFps" else "--")
                if (settings.isMemoryUsageEnabled) StatRow("RAM", "$realMemory MB")
                StatRow("CPU", "$realCpu%")
                StatRow("GPU", "$realGpu%")
                StatRow("Network", if (realNetwork >= 1000) "${realNetwork / 1000} Mbps" else "$realNetwork Kbps")
                StatRow("Gecko", "${uiTabs.size} Tabs")
            }
        }
    }

    if (particles.isNotEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.9f)
        ) {
            particles.forEach { p ->
                var prog by remember(p.id) { mutableFloatStateOf(0f) }
                LaunchedEffect(p.id) {
                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() - start < 800) {
                        prog = ((System.currentTimeMillis() - start) / 800f).coerceAtMost(1f)
                        delay(16)
                    }
                    prog = 1f
                }
                Box(
                    Modifier
                        .offset {
                            IntOffset((p.startX + p.dx * prog).toInt(), (p.startY + p.dy * prog).toInt())
                        }
                        .size((4 * (1f - prog * 0.5f)).dp)
                        .graphicsLayer { alpha = (1f - prog).coerceIn(0f, 1f) }
                        .background(Color.White, CircleShape)
                )
            }
        }
    }

    if (showSplash) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF070709)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(if (splashStep >= 1) 16.dp else 8.dp)
                            .background(
                                if (splashStep >= 1) AuroraColors.auroraBlue else AuroraColors.white20,
                                CircleShape
                            )
                    )
                    Box(
                        Modifier
                            .size(if (splashStep >= 2) 16.dp else 8.dp)
                            .background(
                                if (splashStep >= 2) AuroraColors.auroraBlue else AuroraColors.white20,
                                CircleShape
                            )
                    )
                    Box(
                        Modifier
                            .size(if (splashStep >= 3) 16.dp else 8.dp)
                            .background(
                                if (splashStep >= 3) AuroraColors.auroraBlue else AuroraColors.white20,
                                CircleShape
                            )
                    )
                }
                Text(
                    "AURORA",
                    color = AuroraColors.white,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
                Text(
                    "Living Glass Interface",
                    color = AuroraColors.white40,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
