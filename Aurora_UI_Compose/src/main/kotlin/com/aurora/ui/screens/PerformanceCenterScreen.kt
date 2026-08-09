package com.aurora.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.components.LogEvent
import com.aurora.browser.ui.components.Process
import com.aurora.ui.theme.AuroraColors

@Composable
fun PerformanceCenterScreen(
    processes: List<Process> = emptyList(),
    timeline: List<LogEvent> = emptyList(),
    onKillProcess: (Int) -> Unit = {},
    onClearTimeline: () -> Unit = {},
    onRunDiagnostics: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D12))
            .padding(24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, AuroraColors.white5).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp)
                        .background(Brush.linearGradient(listOf(AuroraColors.auroraBlue, AuroraColors.auroraPurple)), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BugReport, null, Modifier.size(22.dp), Color.White)
                }
                Column {
                    Text(
                        "AURORA TELEMETRY & PERFORMANCE CENTER", color = AuroraColors.white,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Real-time system health, process scheduling, and diagnostic matrices",
                        color = AuroraColors.white40, fontSize = 9.sp
                    )
                }
            }
            Box(
                Modifier.background(AuroraColors.neutral900, RoundedCornerShape(12.dp))
                    .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp).clickable { onBack() }
            ) {
                Text("Back to Dashboard", color = AuroraColors.white, fontSize = 11.sp)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val totalRam = processes.sumOf { it.memory }
            val avgCpu = (processes.sumOf { it.cpu }).coerceAtMost(100)

            TelemetryCard("Memory Budget", "$totalRam MB", "Allocated / 2048 MB Limit", AuroraColors.auroraBlue, (totalRam.toFloat() / 2048f).coerceIn(0f, 1f), Modifier.weight(1f))
            TelemetryCard("CPU Core Load", "$avgCpu%", "Average load (4 physical threads)", AuroraColors.auroraPurple, avgCpu / 100f, Modifier.weight(1f))
            TelemetryCard("GPU pipeline", "11%", "Frame renderer: 16ms budget", AuroraColors.auroraEmerald, 0.11f, Modifier.weight(1f))
            TelemetryCard("Network Bandwidth", "15 Mbps", "Avg ping: 42ms connection", AuroraColors.auroraAmber, 0.75f, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(AuroraColors.auroraBlue.copy(alpha = 0.1f), AuroraColors.neutral900.copy(alpha = 0.4f), AuroraColors.auroraPurple.copy(alpha = 0.1f))), RoundedCornerShape(16.dp))
                .border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Automated Hardware & Memory Optimization Engine", color = AuroraColors.white, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Simulate kernel diagnostics self-test, flush garbage collection allocations, and verify thread integrity.", color = AuroraColors.white40, fontSize = 9.sp)
                }
                Box(Modifier.background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp).clickable { onRunDiagnostics() }) {
                    Text("Run System Self-Test", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(0.58f).background(AuroraColors.neutral900.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active Processes Manager", color = AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${processes.size} tasks running", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    processes.forEach { p ->
                        Row(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("#${p.pid}", color = AuroraColors.white40, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(0.2f))
                            Text(p.name, color = AuroraColors.white90, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(0.5f))
                            Text("${p.cpu}%", color = AuroraColors.auroraBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(0.15f))
                            if (p.type == "Renderer") {
                                Box(Modifier.background(AuroraColors.auroraRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).border(1.dp, AuroraColors.auroraRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp).clickable { onKillProcess(p.pid) }) {
                                    Text("Kill Process", color = AuroraColors.auroraRed, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Protected", color = AuroraColors.white20, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
                            }
                        }
                    }
                }
            }
            Column(Modifier.weight(0.42f).background(AuroraColors.neutral900.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Telemetry Event Logs", color = AuroraColors.white60, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.background(AuroraColors.neutral850, RoundedCornerShape(4.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp).clickable { onClearTimeline() }) {
                        Text("Clear", color = AuroraColors.white40, fontSize = 8.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (timeline.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No logged telemetry events.", color = AuroraColors.white30, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        timeline.take(15).forEach { event ->
                            Column(Modifier.fillMaxWidth().background(AuroraColors.neutral950.copy(alpha = 0.3f), RoundedCornerShape(6.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(6.dp)).padding(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("[]", color = AuroraColors.white30, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Text(event.category, color = AuroraColors.auroraBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                }
                                Text(event.message, color = AuroraColors.white80, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    title: String, value: String, subtitle: String, color: Color, percent: Float, modifier: Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = percent, animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "progress")
    Column(modifier = modifier.padding(4.dp).background(AuroraColors.neutral900.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).border(1.dp, AuroraColors.white5, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = AuroraColors.white50, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("? Healthy", color = AuroraColors.auroraEmerald, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Text(value, color = AuroraColors.white, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = AuroraColors.white40, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).background(AuroraColors.neutral800, RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(animatedProgress).height(6.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}
