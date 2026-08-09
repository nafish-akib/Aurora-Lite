package com.aurora.ui.screens

import androidx.compose.foundation.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.ui.AuroraEngineConfig
import com.aurora.ui.theme.AuroraColors
import com.aurora.ui.model.SettingsUiState
import com.aurora.ui.theme.rememberSettingsAlpha

var systemKeyboardToggleCallback: (Boolean) -> Unit = {}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit,
    onSearchEngineChange: (String) -> Unit,
    onAnimationSpeedChange: (Float) -> Unit,
    onLargerUIChange: (Boolean) -> Unit,
    onSettingsCategoryChange: (String) -> Unit,
    onPerfOverlayToggle: (Boolean) -> Unit,
    onFpsCounterToggle: (Boolean) -> Unit,
    onMemoryUsageToggle: (Boolean) -> Unit,
    onRemoteVisibleToggle: (Boolean) -> Unit,
    onBackToDashboard: () -> Unit,
    onNavigatePerformanceCenter: () -> Unit,
    onClearHistory: () -> Unit,
    onTriggerToast: (String) -> Unit,
    benchmarkMode: Boolean = false,
    onBenchmarkModeToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (sA, sTY) = rememberSettingsAlpha()

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = sA; translationY = sTY }
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings, null, Modifier.size(24.dp), AuroraColors.auroraEmerald
                    )
                    Column {
                        Text(
                            "AURORA CONTROL CENTER", color = AuroraColors.white,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                        Text(
                            "Adjust parameters of your Living Glass environment",
                            color = AuroraColors.white40, fontSize = 9.sp
                        )
                    }
                }
                var backFocused by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .onFocusChanged { backFocused = it.isFocused }
                        .auroraFocus(
                            state = if (backFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Toolbar,
                            focusedStyle = AuroraFocusStyle.ToolbarFocused
                        )
                        .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
                        .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { onBackToDashboard() }
                ) {
                    Text("Back to Dashboard", color = AuroraColors.white, fontSize = 10.sp)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    Modifier.weight(0.33f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    buildList {
                        add("Appearance"); add("Privacy"); add("Search Engine")
                        add("Performance"); if (uiState.developerMode) add("Developer"); add("About")
                    }.forEach { cat ->
                        val ia = uiState.activeSettingsCategory == cat
                        var catFocused by remember(cat) { mutableStateOf(false) }
                        Box(
                            Modifier.fillMaxWidth()
                                .onFocusChanged {
                                    catFocused = it.isFocused
                                    if (it.isFocused) onSettingsCategoryChange(cat)
                                }
                                .auroraFocus(
                                    state = if (catFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Surface,
                                    focusedStyle = AuroraFocusStyle.SurfaceFocused
                                )
                                .background(
                                    if (ia) AuroraColors.auroraEmerald.copy(alpha = 0.15f)
                                    else AuroraColors.neutral900,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (ia) AuroraColors.auroraEmerald else AuroraColors.white5,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .clickable { onSettingsCategoryChange(cat) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    cat,
                                    color = if (ia) AuroraColors.auroraEmerald else AuroraColors.white60,
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ChevronRight, null, Modifier.size(16.dp),
                                    AuroraColors.white30
                                )
                            }
                        }
                    }
                }

                Column(
                    Modifier.weight(0.67f)
                        .background(AuroraColors.neutral900.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        "${uiState.activeSettingsCategory} OPTIONS", color = AuroraColors.white,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    when (uiState.activeSettingsCategory) {
                        "Appearance" -> AppearanceSettings(
                            uiState, onThemeChange, onAccentChange, onLargerUIChange, onTriggerToast
                        )
                        "Privacy" -> PrivacySettings(onClearHistory, onTriggerToast)
                        "Search Engine" -> SearchEngineSettings(
                            uiState.searchEngine, onSearchEngineChange, onTriggerToast
                        )
                        "Performance" -> PerformanceSettings()
"Developer" -> DeveloperSettings(
                        uiState, onPerfOverlayToggle, onFpsCounterToggle,
                        onMemoryUsageToggle, onRemoteVisibleToggle,
                        onAnimationSpeedChange, onNavigatePerformanceCenter,
                        benchmarkMode, onBenchmarkModeToggle
                    )
                        "About" -> AboutSettings()
                    }

                    Spacer(Modifier.weight(1f))
                    Text(
                        "State: Local Sync Engaged", color = AuroraColors.white30,
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceSettings(
    uiState: SettingsUiState,
    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit,
    onLargerUIChange: (Boolean) -> Unit,
    onTriggerToast: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Select Theme Palette", color = AuroraColors.white40,
                fontSize = 10.sp, fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Aurora Dark", "Midnight Blue", "Graphite Slate").forEach { t ->
                    val ita = uiState.activeTheme == t
                    var themFocused by remember(t) { mutableStateOf(false) }
                    Box(
                        Modifier
                            .onFocusChanged { themFocused = it.isFocused }
                            .auroraFocus(
                                state = if (themFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Tab,
                                focusedStyle = AuroraFocusStyle.TabFocused
                            )
                            .background(
                            if (ita) AuroraColors.auroraEmerald.copy(alpha = 0.2f)
                            else AuroraColors.neutral900,
                            RoundedCornerShape(12.dp)
                        ).border(
                            1.dp,
                            if (ita) AuroraColors.auroraEmerald else AuroraColors.white5,
                            RoundedCornerShape(12.dp)
                        ).padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable { onThemeChange(t); onTriggerToast("Theme: $t") }
                    ) {
                        Text(
                            t, color = if (ita) AuroraColors.auroraEmerald else AuroraColors.white60,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Select Brand Accent Color", color = AuroraColors.white40,
                fontSize = 10.sp, fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "Blue" to "#4DA3FF", "Emerald" to "#34D399",
                    "Purple" to "#A78BFA", "Orange" to "#F97316"
                ).forEach { (n, c) ->
                    val iac = uiState.activeAccent == c
                    var accFocused by remember(n) { mutableStateOf(false) }
                    Box(
                        Modifier.size(32.dp)
                            .onFocusChanged { accFocused = it.isFocused }
                            .auroraFocus(
                                state = if (accFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Accent,
                                focusedStyle = AuroraFocusStyle.AccentFocused
                            )
                            .background(Color(android.graphics.Color.parseColor(c)), CircleShape)
                            .border(1.dp, AuroraColors.white20, CircleShape)
                            .clickable { onAccentChange(c); onTriggerToast("Accent: $n") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (iac) Icon(Icons.Default.Check, null, Modifier.size(16.dp), Color.Black)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "UI Scaling preferences", color = AuroraColors.white40,
                fontSize = 10.sp, fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(16.dp)
                        .background(
                            if (uiState.largerUI) AuroraColors.auroraEmerald else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (uiState.largerUI) AuroraColors.auroraEmerald else AuroraColors.white30,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onLargerUIChange(!uiState.largerUI) },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.largerUI) Icon(
                        Icons.Default.Check, null, Modifier.size(12.dp), Color.Black
                    )
                }
                Text(
                    "Enable Larger UI mode (+4sp comfort boost)",
                    color = AuroraColors.white80, fontSize = 10.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PrivacySettings(
    onClearHistory: () -> Unit,
    onTriggerToast: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Aurora manages all database keys with AES-256 local envelopes. Guest sessions never write sync data.",
            color = AuroraColors.white50, fontSize = 10.sp
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            var flushFocused by remember { mutableStateOf(false) }
            Box(
                Modifier.fillMaxWidth()
                    .onFocusChanged { flushFocused = it.isFocused }
                    .auroraFocus(
                        state = if (flushFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
                    .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                    .padding(12.dp).clickable { onClearHistory() }
            ) {
                Text(
                    "Flush SQLite History & Cookies",
                    color = AuroraColors.auroraRed, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SearchEngineSettings(
    currentSearchEngine: String,
    onSearchEngineChange: (String) -> Unit,
    onTriggerToast: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Configure Default Search Protocol", color = AuroraColors.white40,
            fontSize = 10.sp, fontWeight = FontWeight.Medium
        )
        listOf("Google", "DuckDuckGo", "Bing").forEach { e ->
            val ie = currentSearchEngine == e
            var seFocused by remember(e) { mutableStateOf(false) }
            Row(
                Modifier.fillMaxWidth()
                    .onFocusChanged { seFocused = it.isFocused }
                    .auroraFocus(
                        state = if (seFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Surface,
                        focusedStyle = AuroraFocusStyle.SurfaceFocused
                    )
                    .background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
                    .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .clickable { onSearchEngineChange(e); onTriggerToast("Search: $e") },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(16.dp)
                        .background(
                            if (ie) AuroraColors.auroraEmerald else Color.Transparent,
                            CircleShape
                        )
                        .border(
                            2.dp,
                            if (ie) AuroraColors.auroraEmerald else AuroraColors.white30,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (ie) Icon(Icons.Default.Check, null, Modifier.size(10.dp), Color.Black)
                }
                Text("$e Engine", color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PerformanceSettings() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Your active television utilizes TV Class-2 memory budget allocations.",
            color = AuroraColors.white50, fontSize = 10.sp
        )
        Box(
            Modifier.fillMaxWidth()
                .background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Renderer Compressor:", color = AuroraColors.white,
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace
                )
                Text(
                    "ACTIVE", color = AuroraColors.auroraEmerald,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Box(
            Modifier.fillMaxWidth()
                .background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tab Sleep Threshold:", color = AuroraColors.white,
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace
                )
                Text(
                    "After 30 minutes", color = AuroraColors.white70,
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DeveloperSettings(
    uiState: SettingsUiState,
    onPerfOverlayToggle: (Boolean) -> Unit,
    onFpsCounterToggle: (Boolean) -> Unit,
    onMemoryUsageToggle: (Boolean) -> Unit,
    onRemoteVisibleToggle: (Boolean) -> Unit,
    onAnimationSpeedChange: (Float) -> Unit,
    onNavigatePerformanceCenter: () -> Unit,
    benchmarkMode: Boolean = false,
    onBenchmarkModeToggle: (Boolean) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(AuroraColors.auroraBlue.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, AuroraColors.auroraBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Terminal, null, Modifier.size(14.dp),
                        AuroraColors.auroraBlue
                    )
                    Text(
                        "Kernel Diagnostic Suite", color = AuroraColors.white,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Real-time television telemetry pipeline controls",
                    color = AuroraColors.white50, fontSize = 9.sp
                )
            }
        }

        ToggleRow(
            label = "Enable Diagnostics HUD Overlay",
            subtitle = "Float FPS, memory, render times on top right",
            isEnabled = uiState.isPerfOverlayEnabled,
            onToggle = { onPerfOverlayToggle(!uiState.isPerfOverlayEnabled) }
        )

        ToggleRow(
            label = "Show Virtual Simulation Remote Control (F1 shortcut)",
            subtitle = "Keep on screen for mouse navigation inputs",
            isEnabled = uiState.isRemoteVisible,
            onToggle = { onRemoteVisibleToggle(!uiState.isRemoteVisible) }
        )



        ToggleRow(
            label = "Use Android TV System Keyboard",
            subtitle = "Default: On. Disable to use Aurora's custom keyboard instead",
            isEnabled = uiState.useSystemKeyboard,
            onToggle = { systemKeyboardToggleCallback(!uiState.useSystemKeyboard) }
        )

        if (uiState.isPerfOverlayEnabled) {
            Column(
                Modifier.padding(start = 24.dp)
                    .border(
                        1.dp, AuroraColors.white10,
                        RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    )
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubToggleRow(
                    label = "Render Frame Counter (FPS)",
                    isEnabled = uiState.isFpsCounterEnabled,
                    onToggle = { onFpsCounterToggle(!uiState.isFpsCounterEnabled) }
                )
                SubToggleRow(
                    label = "Resident RAM Allocations",
                    isEnabled = uiState.isMemoryUsageEnabled,
                    onToggle = { onMemoryUsageToggle(!uiState.isMemoryUsageEnabled) }
                )
            }
        }

        AnimationSpeedSlider(
            speed = uiState.animationSpeedMultiplier,
            onSpeedChange = onAnimationSpeedChange
        )

        ToggleRow(
            label = "Benchmark Mode (Requires Restart)",
            subtitle = "Disables Spectre mitigations, tracking protection, accessibility, safe browsing. Improves Speedometer/JetStream scores significantly. Reverts on restart unless saved.",
            isEnabled = benchmarkMode,
            onToggle = { onBenchmarkModeToggle(!benchmarkMode) }
        )

        var perfFocused by remember { mutableStateOf(false) }
        Box(
            Modifier.fillMaxWidth()
                .onFocusChanged { perfFocused = it.isFocused }
                .auroraFocus(
                    state = if (perfFocused) FocusState.Focused else FocusState.Idle,
                    idleStyle = AuroraFocusStyle.Primary,
                    focusedStyle = AuroraFocusStyle.PrimaryFocused
                )
                .background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp)
                .clickable { onNavigatePerformanceCenter() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Launch Full Telemetry Performance Center Screen",
                color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var toggleFocused by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth()
            .onFocusChanged { toggleFocused = it.isFocused }
            .auroraFocus(
                state = if (toggleFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBoxIcon(isChecked = isEnabled)
        Column {
            Text(label, color = AuroraColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AuroraColors.white40, fontSize = 8.sp)
        }
    }
}

@Composable
private fun SubToggleRow(
    label: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subFocused by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .onFocusChanged { subFocused = it.isFocused }
            .auroraFocus(
                state = if (subFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Surface,
                focusedStyle = AuroraFocusStyle.SurfaceFocused
            )
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
            ) {
                var largeFocused by remember { mutableStateOf(false) }
                Box(
                    Modifier.size(16.dp)
                        .onFocusChanged { largeFocused = it.isFocused }
                        .auroraFocus(
                            state = if (largeFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Accent,
                            focusedStyle = AuroraFocusStyle.AccentFocused
                        )
                        .background(
                    if (isEnabled) AuroraColors.auroraBlue else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .border(
                    1.dp,
                    if (isEnabled) AuroraColors.auroraBlue else AuroraColors.white30,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isEnabled) Icon(Icons.Default.Check, null, Modifier.size(10.dp), Color.Black)
        }
        Text(label, color = AuroraColors.white80, fontSize = 9.sp)
    }
}

@Composable
private fun AnimationSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .background(AuroraColors.neutral950.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Gecko CSS Animation Scale", color = AuroraColors.white,
                fontSize = 9.sp, fontWeight = FontWeight.Bold
            )
            Text(
                "${speed}x", color = AuroraColors.auroraBlue,
                fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .background(AuroraColors.neutral800, RoundedCornerShape(2.dp))
                .drawWithContent {
                    drawContent()
                    drawRect(
                        AuroraColors.auroraBlue.copy(alpha = 0.5f),
                        size = Size(size.width * (speed / 2f), size.height)
                    )
                }
        )
        Text(
            "Set to 0x to clear composite layout processing threads",
            color = AuroraColors.white30, fontSize = 7.sp
        )
    }
}

@Composable
private fun AboutSettings() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow("Application:", "Aurora Premium Browser")
        InfoRow("Version:", "v2.0.0 Stable Build")
        InfoRow("Compositor Engine:", AuroraEngineConfig.displayName)
        Text(
            "Designed & engineered by Aurora Labs. Living Glass TV v2.0.",
            color = AuroraColors.white60, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label, color = AuroraColors.auroraBlue, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
        )
        Text(
            value, color = AuroraColors.white80, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun CheckBoxIcon(isChecked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(16.dp)
            .background(
                if (isChecked) AuroraColors.auroraBlue else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (isChecked) AuroraColors.auroraBlue else AuroraColors.white30,
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) Icon(Icons.Default.Check, null, Modifier.size(12.dp), Color.Black)
    }
}
