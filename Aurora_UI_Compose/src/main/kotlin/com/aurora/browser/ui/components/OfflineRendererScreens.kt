package com.aurora.browser.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography

@Composable
fun OfflineModeScreen(
    onBrowseFiles: () -> Unit = {},
    onReadCachedWiki: () -> Unit = {},
    onForceReconnect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .background(Color(0xFF17181F), AuroraShapes.Rounded3Xl)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.Rounded3Xl)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(AuroraColors.Amber.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
                    .border(1.dp, AuroraColors.Amber.copy(alpha = 0.2f), AuroraShapes.RoundedLg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Offline",
                    tint = AuroraColors.Amber,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You Are Offline",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "The Aurora Engine has detected a network interruption. Cached pages remain accessible through the onboard library.",
                    style = AuroraTypography.Body,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuroraColors.Blue.copy(alpha = 0.15f), AuroraShapes.RoundedMd)
                        .border(1.dp, AuroraColors.Blue.copy(alpha = 0.3f), AuroraShapes.RoundedMd)
                        .clickable { onBrowseFiles() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Browse Downloaded Files",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1C23), AuroraShapes.RoundedMd)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedMd)
                        .clickable { onReadCachedWiki() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Read Cached Wikipedia Article",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuroraColors.Amber, AuroraShapes.RoundedMd)
                        .clickable { onForceReconnect() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Force Reconnect",
                        style = AuroraTypography.MonoLabel,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RendererCrashScreen(
    onReloadRestore: () -> Unit = {},
    onReturnHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .background(Color(0xFF17181F), AuroraShapes.Rounded3Xl)
                .border(1.dp, AuroraColors.Red.copy(alpha = 0.3f), AuroraShapes.Rounded3Xl)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(AuroraColors.Red.copy(alpha = 0.1f), AuroraShapes.RoundedLg)
                    .border(1.dp, AuroraColors.Red.copy(alpha = 0.2f), AuroraShapes.RoundedLg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Renderer Crash",
                    tint = AuroraColors.Red,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Renderer Process Crashed",
                    style = AuroraTypography.TitleDisplay,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuroraColors.Red
                )
                Text(
                    text = "A compositing pipeline process has terminated unexpectedly. This may be due to memory constraints or a rendering fault.",
                    style = AuroraTypography.Body,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuroraColors.Blue, AuroraShapes.RoundedMd)
                        .clickable { onReloadRestore() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reload & Restore Tab",
                        style = AuroraTypography.MonoLabel,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1C23), AuroraShapes.RoundedMd)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedMd)
                        .clickable { onReturnHome() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Return to Home Dashboard",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
