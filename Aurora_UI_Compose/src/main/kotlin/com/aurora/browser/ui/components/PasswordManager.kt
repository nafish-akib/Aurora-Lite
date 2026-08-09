package com.aurora.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.engine.LoginStorage

@Composable
fun PasswordSavePrompt(
    origin: String,
    username: String,
    password: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .background(AuroraColors.Neutral900, RoundedCornerShape(24.dp))
                .border(1.dp, AuroraColors.Emerald.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Lock, null, Modifier.size(40.dp), AuroraColors.Emerald)
            Text("Save Password?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(origin, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Column(Modifier.background(AuroraColors.Neutral800, RoundedCornerShape(12.dp)).fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Username: $username", color = Color.White, fontSize = 12.sp)
                Text("Password: ${"*".repeat(password.length)}", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).background(AuroraColors.Neutral800, AuroraShapes.RoundedMd).border(1.dp, Color.White.copy(alpha = 0.1f), AuroraShapes.RoundedMd).padding(12.dp).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                    Text("Not Now", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.weight(1f).background(AuroraColors.Emerald, AuroraShapes.RoundedMd).padding(12.dp).clickable { onSave() }, contentAlignment = Alignment.Center) {
                    Text("Save", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PasswordManagerScreen(
    loginStorage: LoginStorage?,
    onClose: () -> Unit,
    onDeleteAll: () -> Unit = {}
) {
    val logins = remember { loginStorage?.findLogins("") ?: emptyList() }
    var allLogins by remember { mutableStateOf(logins) }

    Column(Modifier.fillMaxSize().background(Color(0xFF0E0F12)).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Lock, null, Modifier.size(24.dp), AuroraColors.Emerald)
                Column {
                    Text("Saved Passwords", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${allLogins.size} saved logins", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allLogins.isNotEmpty()) Box(Modifier.background(AuroraColors.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).border(1.dp, AuroraColors.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { loginStorage?.clearAll(); allLogins = emptyList(); onDeleteAll() }) {
                    Text("Clear All", color = AuroraColors.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.background(AuroraColors.Neutral800, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { onClose() }) {
                    Text("Close", color = Color.White, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (allLogins.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No saved passwords", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allLogins) { login ->
                    Row(
                        Modifier.fillMaxWidth().background(AuroraColors.Neutral900.copy(alpha = 0.5f), RoundedCornerShape(12.dp))                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(login.origin, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(login.username, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("${"*".repeat(login.password.length)}", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

data class PasswordPromptState(
    val origin: String = "",
    val username: String = "",
    val password: String = ""
)
