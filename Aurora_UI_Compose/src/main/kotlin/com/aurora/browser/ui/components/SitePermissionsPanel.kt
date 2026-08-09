package com.aurora.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.engine.PermissionRequest
import com.aurora.engine.SitePermissionsService
import com.aurora.browser.ui.theme.AuroraColors

@Composable
fun SitePermissionsPanel(
    request: PermissionRequest?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && request != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val r = request ?: return@AnimatedVisibility
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuroraColors.BgRoot.copy(alpha = 0.98f))
                    .border(1.dp, AuroraColors.BorderGlass, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(AuroraColors.Blue.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text(SitePermissionsService.permissionIcon(r.permission), fontSize = 20.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(r.domain, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("wants to use", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${SitePermissionsService.permissionIcon(r.permission)} ${SitePermissionsService.permissionLabel(r.permission)}",
                    color = AuroraColors.Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(AuroraColors.Neutral800).clickable { onDeny() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Deny", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(AuroraColors.Blue).clickable { onAllow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Allow", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SiteInfoPanel(
    visible: Boolean,
    url: String,
    domain: String,
    isSecure: Boolean,
    isBookmarked: Boolean,
    isDesktopMode: Boolean,
    permissions: Map<String, Boolean>,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onClearSiteData: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuroraColors.BgRoot.copy(alpha = 0.98f))
                    .border(1.dp, AuroraColors.BorderGlass, RoundedCornerShape(16.dp))
                    .padding(0.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isSecure) Icons.Default.Lock else Icons.Default.Shield, null, Modifier.size(18.dp), if (isSecure) AuroraColors.Emerald else AuroraColors.Red)
                        Text("Site Information", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(AuroraColors.Neutral800).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, null, Modifier.size(12.dp), Color.White.copy(alpha = 0.6f))
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(AuroraColors.BorderGlass))
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(domain, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(url, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (isSecure) AuroraColors.Emerald.copy(alpha = 0.15f) else AuroraColors.Red.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(if (isSecure) "Secure" else "Not Secure", color = if (isSecure) AuroraColors.Emerald else AuroraColors.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(AuroraColors.Blue.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("Site Info", color = AuroraColors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(AuroraColors.BorderGlass))

                // Permissions section
                if (permissions.isNotEmpty()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Permissions", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        permissions.forEach { (perm, granted) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${SitePermissionsService.permissionIcon(perm)} ${SitePermissionsService.permissionLabel(perm)}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Icon(
                                    if (granted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null, Modifier.size(14.dp),
                                    if (granted) AuroraColors.Emerald.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AuroraColors.BorderGlass))
                }

                // Actions
                Column(Modifier.padding(8.dp)) {
                    SiteInfoAction("Bookmark", if (isBookmarked) "Remove" else "Add", onClick = onToggleBookmark)
                    SiteInfoAction("Desktop Mode", if (isDesktopMode) "On" else "Off", onClick = onToggleDesktopMode)
                    SiteInfoAction("Clear Site Data", "", onClick = onClearSiteData)
                }
            }
        }
    }
}

@Composable
private fun SiteInfoAction(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        if (value.isNotEmpty()) Text(value, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
