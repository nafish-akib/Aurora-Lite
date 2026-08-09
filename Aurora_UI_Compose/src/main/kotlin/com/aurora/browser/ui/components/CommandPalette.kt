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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography

data class CommandAction(
    val label: String,
    val action: () -> Unit
)

@Composable
fun CommandPalette(
    isOpen: Boolean,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onClose: () -> Unit = {},
    commands: List<CommandAction> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val filteredCommands = commands.filter {
        it.label.lowercase().contains(query.lowercase())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709).copy(alpha = 0.7f)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .padding(top = 80.dp)
                .widthIn(max = 480.dp)
                .background(Color(0xFF14161C).copy(alpha = 0.95f), AuroraShapes.Rounded3Xl)
                .border(1.dp, Color.White.copy(alpha = 0.08f), AuroraShapes.Rounded3Xl)
                .padding(16.dp)
        ) {
            Text(
                text = "Command Palette",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            filteredCommands.forEach { cmd ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1C23), AuroraShapes.RoundedSm)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedSm)
                        .clickable { cmd.action() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AuroraColors.Blue, CircleShape)
                    )
                    Text(
                        text = cmd.label,
                        style = AuroraTypography.Body,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
