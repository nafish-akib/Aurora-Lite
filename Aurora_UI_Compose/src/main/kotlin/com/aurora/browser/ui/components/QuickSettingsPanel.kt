package com.aurora.browser.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
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
fun QuickSettingsPanel(
    isVisible: Boolean,
    brightness: Int = 80,
    onBrightnessChange: (Int) -> Unit = {},
    onClose: () -> Unit = {},
    onOpenFullSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFF0E0F14).copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK SETTINGS",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onClose() }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Brightness",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
            Slider(
                value = brightness.toFloat(),
                onValueChange = { onBrightnessChange(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${brightness}%",
                style = AuroraTypography.MonoLabel,
                color = AuroraColors.Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }

        QuickSettingToggle(
            label = "Private Session",
            icon = Icons.Default.Lock,
            isActive = false,
            onClick = {}
        )
        QuickSettingToggle(
            label = "Performance Mode",
            icon = Icons.Default.Speed,
            isActive = false,
            onClick = {}
        )
        QuickSettingToggle(
            label = "Background Audio",
            icon = Icons.Default.MusicNote,
            isActive = false,
            onClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuroraColors.Emerald.copy(alpha = 0.15f), AuroraShapes.RoundedMd)
                .border(1.dp, AuroraColors.Emerald.copy(alpha = 0.3f), AuroraShapes.RoundedMd)
                .clickable { onOpenFullSettings() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open Full Settings",
                style = AuroraTypography.MonoLabel,
                color = AuroraColors.Emerald,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun QuickSettingToggle(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1C23), AuroraShapes.RoundedSm)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) AuroraColors.Blue else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (isActive) AuroraColors.Emerald else Color(0xFF23252F),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        )
    }
}
