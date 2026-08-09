package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography

@Composable
fun DeveloperHud(
    isEnabled: Boolean,
    fpsEnabled: Boolean = true,
    memoryEnabled: Boolean = true,
    tabsCount: Int = 0,
    fps: Int = 0,
    memoryMB: Int = 0,
    cpuPercent: Int = 0,
    gpuPercent: Int = 0,
    networkKbps: Long = 0,
    modifier: Modifier = Modifier
) {
    if (!isEnabled) return

    Column(
        modifier = modifier
            .width(208.dp)
            .background(Color(0xFF0E0F14).copy(alpha = 0.85f), AuroraShapes.RoundedLg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Developer HUD",
                style = AuroraTypography.MonoLabel,
                color = AuroraColors.Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "\u25cf LIVE",
                style = AuroraTypography.MonoLabel,
                color = AuroraColors.Emerald,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (fpsEnabled) {
                HudRow("FPS", if (fps > 0) "$fps" else "--")
            }
            if (memoryEnabled) {
                HudRow("RAM", "$memoryMB MB")
            }
            HudRow("CPU", "$cpuPercent%")
            HudRow("GPU", "$gpuPercent%")
            HudRow("Network", if (networkKbps >= 1000) "${networkKbps / 1000} Mbps" else "$networkKbps Kbps")
            HudRow("Gecko", "$tabsCount Tabs")

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 2.dp))

            val cacheEntries = tabsCount * 50 + 64
            HudRow("Cache Size", "$cacheEntries MB", valueColor = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun HudRow(
    label: String,
    value: String,
    valueColor: Color = AuroraColors.Emerald
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AuroraTypography.MonoLabel,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = AuroraTypography.MonoLabel,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
