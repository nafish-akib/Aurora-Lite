package com.aurora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.state.ErrorState
import com.aurora.ui.theme.AuroraColors

@Composable
fun LoadErrorScreen(
    errorState: ErrorState,
    failedUrl: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOpenExternally: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (icon, title, message) = when (errorState) {
        is ErrorState.NetworkError -> Triple(
            Icons.Default.SignalWifiOff,
            "Network Error",
            "Unable to reach the server. Check your connection and try again."
        )
        is ErrorState.SslError -> Triple(
            Icons.Default.Lock,
            "Secure Connection Failed",
            "The connection to this site is not secure or uses an untrusted certificate."
        )
        is ErrorState.FileNotFound -> Triple(
            Icons.Default.Error,
            "Page Not Found",
            "The page at ${(errorState as ErrorState.FileNotFound).url} could not be found."
        )
        is ErrorState.HttpError -> Triple(
            Icons.Default.Warning,
            "HTTP Error ${(errorState as ErrorState.HttpError).code}",
            (errorState as ErrorState.HttpError).description
        )
        is ErrorState.Unknown -> Triple(
            Icons.Default.Warning,
            "Load Error",
            "An unexpected error occurred while loading this page."
        )
        is ErrorState.None -> return
    }

    Box(
        modifier.fillMaxSize().background(Color(0xFF0C0C0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(AuroraColors.neutral900, RoundedCornerShape(24.dp))
                .border(1.dp, AuroraColors.white5, RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Box(
                Modifier.size(64.dp)
                    .background(AuroraColors.auroraRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, AuroraColors.auroraRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(32.dp), AuroraColors.auroraRed)
            }
            Text(title, color = AuroraColors.white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                message,
                color = AuroraColors.white50,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            if (failedUrl.isNotBlank()) {
                Text(
                    failedUrl,
                    color = AuroraColors.white30,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                if (failedUrl.startsWith("http://", ignoreCase = true)) {
                    Text(
                        "Tip: try typing the address with https:// instead",
                        color = AuroraColors.auroraBlue.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp))
                        .focusable()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .clickable { onRetry() }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, null, Modifier.size(14.dp), Color.Black)
                        Text("Retry", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    Modifier
                        .background(AuroraColors.neutral800, RoundedCornerShape(12.dp))
                        .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                        .focusable()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .clickable { onBack() }
                ) {
                    Text("Back", color = AuroraColors.white, fontSize = 11.sp)
                }
                Box(
                    Modifier
                        .background(AuroraColors.neutral800, RoundedCornerShape(12.dp))
                        .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                        .focusable()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .clickable { onHome() }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Home, null, Modifier.size(14.dp), AuroraColors.white)
                        Text("Home", color = AuroraColors.white, fontSize = 11.sp)
                    }
                }
                Box(
                    Modifier
                        .background(AuroraColors.neutral800, RoundedCornerShape(12.dp))
                        .border(1.dp, AuroraColors.white5, RoundedCornerShape(12.dp))
                        .focusable()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .clickable { onOpenExternally() }
                ) {
                    Text("External", color = AuroraColors.white50, fontSize = 11.sp)
                }
            }
        }
    }
}
