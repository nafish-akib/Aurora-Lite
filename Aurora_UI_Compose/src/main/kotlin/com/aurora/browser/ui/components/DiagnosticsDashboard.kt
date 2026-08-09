package com.aurora.browser.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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

// Data classes corresponding to the types used
data class Process(
    val pid: Int,
    val name: String,
    val type: String, // "Browser" | "Renderer" | "GPU" | "Plugin"
    val cpu: Int,
    val memory: Int
)

data class LogEvent(
    val id: String,
    val timestamp: String,
    val category: String,
    val type: String, // "info" | "warn" | "error"
    val message: String
)

@Composable
fun DiagnosticsDashboard(
    processes: List<Process>,
    onKillProcess: (Int) -> Unit,
    timeline: List<LogEvent>,
    onClearTimeline: () -> Unit,
    onRunDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("metrics") }
    var latencies by remember { mutableStateOf(listOf(42, 45, 40, 48, 41, 44)) }

    // Make latency dynamic just like in React!
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            latencies = latencies.drop(1) + (40..52).random()
        }
    }

    val totalRam = processes.sumOf { it.memory }
    val avgCpu = (processes.sumOf { it.cpu }).coerceAtMost(100)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(AuroraColors.BgCard, AuroraShapes.Rounded3Xl)
            .border(1.dp, Color.White.copy(alpha = 0.08f), AuroraShapes.Rounded3Xl)
    ) {
        // System Diagnostics Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.BgInput)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AURORA DIAGNOSTICS & SYSTEM PANEL",
                    style = AuroraTypography.Header,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Developer Mode Enabled (Gate v1.0)",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
            }

            TextButtonTV(
                onClick = onRunDiagnostics,
                backgroundColor = AuroraColors.Blue.copy(alpha = 0.1f),
                borderColor = AuroraColors.Blue.copy(alpha = 0.2f),
                focusedBorderColor = AuroraColors.Blue
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Test",
                        tint = AuroraColors.Blue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Run Self-Test",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Blue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tabs Menu Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.BgInput.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp)
        ) {
            DiagnosticsTab(
                title = "System Metrics",
                isSelected = activeTab == "metrics",
                onClick = { activeTab = "metrics" }
            )
            DiagnosticsTab(
                title = "Process Manager (${processes.size})",
                isSelected = activeTab == "processes",
                onClick = { activeTab = "processes" }
            )
            DiagnosticsTab(
                title = "Aurora Timeline (${timeline.size})",
                isSelected = activeTab == "timeline",
                onClick = { activeTab = "timeline" }
            )
        }

        Divider(color = Color.White.copy(alpha = 0.05f))

        // Main Tab Content Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
        ) {
            when (activeTab) {
                "metrics" -> SystemMetricsView(totalRam, avgCpu, latencies)
                "processes" -> ProcessManagerView(processes, onKillProcess)
                "timeline" -> TimelineLoggerView(timeline, onClearTimeline)
            }
        }
    }
}

@Composable
fun DiagnosticsTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .background(
                if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = AuroraTypography.MonoLabel,
            fontWeight = FontWeight.Bold,
            color = if (isSelected || isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
        // Indicator underline
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(AuroraColors.Blue)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun SystemMetricsView(
    totalRam: Int,
    avgCpu: Int,
    latencies: List<Int>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memory Usage Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(AuroraColors.BgInput.copy(alpha = 0.5f), AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Memory Budget", style = AuroraTypography.Header, fontSize = 13.sp)
                    Text(text = "● Healthy", style = AuroraTypography.MonoLabel, color = AuroraColors.Emerald)
                }
                
                Column {
                    Text(text = "$totalRam MB", style = AuroraTypography.TitleDisplay, fontSize = 28.sp)
                    Text(
                        text = "Allocated of 2048 MB budget (TV Class-2)",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.08f), AuroraShapes.Circular)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (totalRam.toFloat() / 2048f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(AuroraColors.Blue, AuroraShapes.Circular)
                    )
                }
            }

            // CPU Usage Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(AuroraColors.BgInput.copy(alpha = 0.5f), AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Main CPU Threads", style = AuroraTypography.Header, fontSize = 13.sp)
                    Text(text = "● Operational", style = AuroraTypography.MonoLabel, color = AuroraColors.Emerald)
                }

                Column {
                    Text(text = "$avgCpu%", style = AuroraTypography.TitleDisplay, fontSize = 28.sp)
                    Text(
                        text = "Total loads across 4 virtual clusters",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.08f), AuroraShapes.Circular)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (avgCpu.toFloat() / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(AuroraColors.Purple, AuroraShapes.Circular)
                    )
                }
            }
        }

        // Latency Chart Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.BgInput.copy(alpha = 0.5f), AuroraShapes.RoundedLg)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ping Connection Latency", style = AuroraTypography.Header, fontSize = 13.sp)
                val averageLatency = latencies.average().toInt()
                Text(text = "Avg: ${averageLatency}ms", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f))
            }

            // Simulated Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                latencies.forEachIndexed { idx, lat ->
                    val heightRatio = (lat.toFloat() / 60f).coerceIn(0.1f, 1.0f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightRatio)
                            .background(AuroraColors.Emerald.copy(alpha = 0.25f), AuroraShapes.RoundedSm)
                            .border(1.dp, AuroraColors.Emerald.copy(alpha = 0.5f), AuroraShapes.RoundedSm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$lat",
                            style = AuroraTypography.MonoLabel,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessManagerView(
    processes: List<Process>,
    onKillProcess: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "PID", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
            Text(text = "Process Name", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(3f))
            Text(text = "Type", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1.5f))
            Text(text = "CPU", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(text = "RAM", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            Text(text = "Actions", style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.width(100.dp), textAlign = TextAlign.Center)
        }

        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 8.dp))

        // Table Rows
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(processes) { p ->
                var isRowFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isRowFocused = it.isFocused }
                        .background(
                            if (isRowFocused) Color.White.copy(alpha = 0.08f) else AuroraColors.BgInput.copy(alpha = 0.4f),
                            AuroraShapes.RoundedMd
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${p.pid}", style = AuroraTypography.MonoLabel, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    
                    Text(
                        text = p.name,
                        style = AuroraTypography.Body,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Badges for process types
                    Box(
                        modifier = Modifier.weight(1.5f)
                    ) {
                        val (bgColor, textColor) = when (p.type) {
                            "Browser" -> Pair(AuroraColors.Blue.copy(alpha = 0.15f), AuroraColors.Blue)
                            "Renderer" -> Pair(AuroraColors.Emerald.copy(alpha = 0.15f), AuroraColors.Emerald)
                            "GPU" -> Pair(AuroraColors.Purple.copy(alpha = 0.15f), AuroraColors.Purple)
                            else -> Pair(Color.White.copy(alpha = 0.1f), Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .background(bgColor, AuroraShapes.Circular)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = p.type,
                                style = AuroraTypography.MonoLabel,
                                color = textColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(text = "${p.cpu}%", style = AuroraTypography.MonoLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    
                    Text(text = "${p.memory} MB", style = AuroraTypography.MonoLabel, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)

                    Box(
                        modifier = Modifier.width(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (p.type == "Renderer") {
                            var isKillButtonFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .onFocusChanged { isKillButtonFocused = it.isFocused }
                                    .background(
                                        if (isKillButtonFocused) AuroraColors.Red else AuroraColors.Red.copy(alpha = 0.1f),
                                        AuroraShapes.RoundedSm
                                    )
                                    .border(
                                        1.dp,
                                        if (isKillButtonFocused) Color.White else AuroraColors.Red.copy(alpha = 0.3f),
                                        AuroraShapes.RoundedSm
                                    )
                                    .clickable { onKillProcess(p.pid) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "KILL",
                                    style = AuroraTypography.MonoLabel,
                                    color = if (isKillButtonFocused) Color.Black else AuroraColors.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        } else {
                            Text(
                                text = "SYSTEM",
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineLoggerView(
    timeline: List<LogEvent>,
    onClearTimeline: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Events logged in real-time (OLED Sandbox)",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.4f)
            )
            
            TextButtonTV(
                onClick = onClearTimeline,
                backgroundColor = Color.White.copy(alpha = 0.05f),
                borderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = AuroraColors.Blue
            ) {
                Text(
                    text = "Clear Timeline",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(timeline) { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuroraColors.BgInput.copy(alpha = 0.3f), AuroraShapes.RoundedSm)
                        .border(1.dp, Color.White.copy(alpha = 0.03f), AuroraShapes.RoundedSm)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[${event.timestamp}]",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.3f)
                    )

                    // Severity tag
                    val (tagBg, tagText) = when (event.type) {
                        "info" -> Pair(AuroraColors.Blue.copy(alpha = 0.15f), AuroraColors.Blue)
                        "warn" -> Pair(AuroraColors.Amber.copy(alpha = 0.15f), AuroraColors.Amber)
                        "error" -> Pair(AuroraColors.Red.copy(alpha = 0.15f), AuroraColors.Red)
                        else -> Pair(Color.White.copy(alpha = 0.08f), Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .background(tagBg, AuroraShapes.RoundedSm)
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = event.type.uppercase(),
                            style = AuroraTypography.MonoLabel,
                            color = tagText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = event.category.uppercase(),
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = event.message,
                        style = AuroraTypography.Body,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
