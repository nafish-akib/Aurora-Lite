package com.aurora.browser.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraSpacing
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.browser.ui.theme.auroraCardLift
import com.aurora.browser.ui.theme.auroraGlass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteControl(
    isKeyboardMode: Boolean,
    onToggleInputMode: () -> Unit,
    onDpadPress: (String) -> Unit, // "UP" | "DOWN" | "LEFT" | "RIGHT"
    onSelectPress: () -> Unit,
    onBackPress: () -> Unit,
    onHomePress: () -> Unit,
    onMenuPress: (Boolean) -> Unit, // passes isLongPress
    onSearchPress: () -> Unit,
    onVoicePress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for remote activity indicator
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .width(224.dp)
            .auroraGlass()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Remote Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AURORA R1",
                style = AuroraTypography.MonoLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            // pulsating led
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(pulseAlpha)
                    .background(AuroraColors.Blue, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Mode Toggle Button
        var isToggleFocused by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .onFocusChanged { isToggleFocused = it.isFocused }
                .auroraCardLift(
                    isFocused = isToggleFocused, 
                    shape = AuroraShapes.RoundedMd,
                    onFocusedColor = AuroraColors.Blue
                )
                .background(
                    if (isKeyboardMode) AuroraColors.Blue.copy(alpha = 0.15f) else AuroraColors.Neutral800,
                    AuroraShapes.RoundedMd
                )
                .border(
                    1.dp,
                    if (isKeyboardMode) AuroraColors.Blue else Color.White.copy(alpha = 0.05f),
                    AuroraShapes.RoundedMd
                )
                .clickable { onToggleInputMode() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Input Mode",
                tint = if (isKeyboardMode) AuroraColors.Blue else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isKeyboardMode) "D-PAD MODE" else "CURSOR MODE",
                style = AuroraTypography.MonoLabel,
                fontWeight = FontWeight.Bold,
                color = if (isKeyboardMode) AuroraColors.Blue else Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Voice Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Omnibox Search button
            var isSearchFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .auroraCardLift(isFocused = isSearchFocused, shape = AuroraShapes.RoundedLg)
                    .background(AuroraColors.Neutral800, AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .clickable { onSearchPress() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Omnibox",
                    tint = AuroraColors.Blue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "OMNIBOX",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Voice Search Button
            var isVoiceFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .onFocusChanged { isVoiceFocused = it.isFocused }
                    .auroraCardLift(isFocused = isVoiceFocused, shape = AuroraShapes.RoundedLg, onFocusedColor = AuroraColors.Purple)
                    .background(AuroraColors.Neutral800, AuroraShapes.RoundedLg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedLg)
                    .clickable { onVoicePress() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = AuroraColors.Purple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VOICE",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // D-PAD Navigation Disc (Simulated layout)
        Box(
            modifier = Modifier
                .size(144.dp)
                .background(AuroraColors.BgInput, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Center OK Button
            var isOkFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .onFocusChanged { isOkFocused = it.isFocused }
                    .auroraCardLift(isFocused = isOkFocused, shape = RoundedCornerShape(50))
                    .background(AuroraColors.Neutral800, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { onSelectPress() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOkFocused) AuroraColors.Blue else Color.White
                )
            }

            // Up Arrow
            DpadDirectionButton(
                direction = "UP",
                icon = Icons.Default.ArrowUpward,
                onPress = onDpadPress,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
            )

            // Down Arrow
            DpadDirectionButton(
                direction = "DOWN",
                icon = Icons.Default.ArrowDownward,
                onPress = onDpadPress,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )

            // Left Arrow
            DpadDirectionButton(
                direction = "LEFT",
                icon = Icons.Default.ArrowBack,
                onPress = onDpadPress,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            )

            // Right Arrow
            DpadDirectionButton(
                direction = "RIGHT",
                icon = Icons.Default.ArrowForward,
                onPress = onDpadPress,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Core Actions: Back, Home, Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Back Button
            var isBackFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .auroraCardLift(isFocused = isBackFocused, shape = AuroraShapes.RoundedMd)
                    .background(AuroraColors.Neutral800, AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                    .clickable { onBackPress() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // RotateCcw substitute
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "BACK",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Home Button
            var isHomeFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .onFocusChanged { isHomeFocused = it.isFocused }
                    .auroraCardLift(isFocused = isHomeFocused, shape = AuroraShapes.RoundedMd, onFocusedColor = AuroraColors.Emerald)
                    .background(AuroraColors.Neutral800, AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                    .clickable { onHomePress() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = AuroraColors.Emerald,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "HOME",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Menu Button (Combined single & long clicks)
            var isMenuFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .onFocusChanged { isMenuFocused = it.isFocused }
                    .auroraCardLift(isFocused = isMenuFocused, shape = AuroraShapes.RoundedMd)
                    .background(AuroraColors.Neutral800, AuroraShapes.RoundedMd)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                    .combinedClickable(
                        onClick = { onMenuPress(false) },
                        onLongClick = { onMenuPress(true) }
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "MENU",
                    style = AuroraTypography.MonoLabel,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Helpful Keyboard Guide
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f), AuroraShapes.RoundedMd)
                .border(1.dp, Color.White.copy(alpha = 0.03f), AuroraShapes.RoundedMd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "KEYBOARD BINDINGS",
                style = AuroraTypography.MonoLabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            GuideRow("D-Pad", "Arrow Keys")
            GuideRow("OK", "Enter/Space")
            GuideRow("Back", "Esc/Backspace")
            GuideRow("Menu", "M key")
            GuideRow("Quick Set.", "Q key")
        }
    }
}

@Composable
fun DpadDirectionButton(
    direction: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onPress(direction) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction,
            tint = if (isFocused) AuroraColors.Blue else Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun GuideRow(label: String, binding: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
        Text(text = binding, style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
    }
}
