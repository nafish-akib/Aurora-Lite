package com.aurora.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus

@Composable
fun FindInPagePanel(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    currentMatch: Int,
    totalMatches: Int,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(visible) {
        if (visible) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        var upFocused by remember { mutableStateOf(false) }
        var downFocused by remember { mutableStateOf(false) }
        var closeFocused by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth()
                .background(AuroraColors.BgRoot.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = AuroraColors.BorderGlass,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuroraColors.Neutral900)
                    .border(1.dp, AuroraColors.BorderGlass, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter) {
                                onFindNext()
                                true
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(AuroraColors.Blue),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    "Find in page...",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (totalMatches > 0) {
                Text(
                    "$currentMatch/$totalMatches",
                    color = AuroraColors.Blue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else if (query.isNotEmpty() && totalMatches == 0) {
                Text(
                    "No results",
                    color = AuroraColors.Red.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier.size(30.dp)
                    .onFocusChanged { upFocused = it.isFocused }
                    .auroraFocus(
                        state = if (upFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(AuroraColors.Neutral800)
                    .clickable { onFindPrevious() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp, null,
                    Modifier.size(16.dp), Color.White.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier.size(30.dp)
                    .onFocusChanged { downFocused = it.isFocused }
                    .auroraFocus(
                        state = if (downFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(AuroraColors.Neutral800)
                    .clickable { onFindNext() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown, null,
                    Modifier.size(16.dp), Color.White.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier.size(30.dp)
                    .onFocusChanged { closeFocused = it.isFocused }
                    .auroraFocus(
                        state = if (closeFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Accent,
                        focusedStyle = AuroraFocusStyle.AccentFocused
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(AuroraColors.Red.copy(alpha = 0.15f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close, null, Modifier.size(14.dp),
                    AuroraColors.Red.copy(alpha = 0.8f)
                )
            }
        }
    }
}