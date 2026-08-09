package com.aurora.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.ui.theme.AuroraColors

@Composable
fun NetworkInfoPanel(isOffline: Boolean, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val cm = remember { ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager }
    val network = remember { cm?.activeNetwork }
    val caps = remember { network?.let { cm?.getNetworkCapabilities(it) } }
    val linkSpeed = caps?.linkDownstreamBandwidthKbps ?: 0
    val linkUpSpeed = caps?.linkUpstreamBandwidthKbps ?: 0
    val type = when {
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
        else -> "Unknown"
    }
    val validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    val metered = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { onClose() }, contentAlignment = Alignment.Center) {
        Column(Modifier.background(AuroraColors.neutral900, RoundedCornerShape(24.dp)).border(1.dp, if (isOffline) AuroraColors.auroraAmber else AuroraColors.auroraEmerald, RoundedCornerShape(24.dp)).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(if (isOffline) Icons.Default.Warning else Icons.Default.Wifi, null, Modifier.size(48.dp), if (isOffline) AuroraColors.auroraAmber else AuroraColors.auroraEmerald)
            Text(if (isOffline) "No Internet Connection" else "Connected to Internet", color = AuroraColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (!isOffline) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connection: $type", color = AuroraColors.white70, fontSize = 12.sp)
                    if (linkSpeed > 0) Text("Down: ${formatSpeedKbps(linkSpeed)}  -  Up: ${formatSpeedKbps(linkUpSpeed)}", color = AuroraColors.white50, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Validated: ${if (validated) "Yes" else "No"}", color = AuroraColors.white50, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    if (metered) Text("Metered network", color = AuroraColors.auroraAmber.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
            Box(Modifier.background(AuroraColors.auroraBlue, RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 10.dp).clickable { onClose() }) { Text("Close", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

private fun formatSpeedKbps(kbps: Int): String = when {
    kbps >= 1_000_000 -> "%.1f Gbps".format(kbps / 1_000_000.0)
    kbps >= 1_000 -> "%.0f Mbps".format(kbps / 1_000.0)
    else -> "$kbps Kbps"
}
