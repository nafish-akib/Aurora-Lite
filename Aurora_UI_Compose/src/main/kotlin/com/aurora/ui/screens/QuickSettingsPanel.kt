package com.aurora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.ui.model.QuickSettingsUiState
import com.aurora.ui.theme.AuroraColors

@Composable
fun QuickSettingsPanel(
    uiState: QuickSettingsUiState,
    onClose: () -> Unit,
    onOpenFullSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(AuroraColors.neutral900, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .border(1.dp, AuroraColors.white10)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "QUICK SETTINGS", color = AuroraColors.white,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )
                Icon(
                    Icons.Default.Close, null, Modifier.size(16.dp).clickable { onClose() },
                    AuroraColors.white40
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Brightness HUD", color = AuroraColors.white60, fontSize = 10.sp)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${uiState.brightness}%", color = AuroraColors.auroraBlue,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(6.dp)
                    .background(AuroraColors.neutral800, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            AuroraColors.auroraBlue,
                            size = Size(size.width * (uiState.brightness - 30) / 70f, size.height)
                        )
                    }
            )
            Spacer(Modifier.height(16.dp))
            Text("Simulated Network", color = AuroraColors.white60, fontSize = 10.sp)
            Text(
                "? High Speed", color = AuroraColors.auroraEmerald,
                fontSize = 11.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text("Memory Optimizer", color = AuroraColors.white60, fontSize = 10.sp)
            Text(
                "Enabled", color = AuroraColors.auroraEmerald,
                fontSize = 11.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.fillMaxWidth()
                    .background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp)
                    .clickable { onOpenFullSettings() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Full Control Center", color = Color.Black,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp)
                    .background(AuroraColors.neutral800, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Close Settings", color = AuroraColors.white80,
                    fontSize = 11.sp
                )
            }
        }
    }
}
