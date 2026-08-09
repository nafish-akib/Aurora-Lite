package com.aurora.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.auroraCardLift

@Composable
fun IconButtonTV(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(32.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .auroraCardLift(isFocused = isFocused, shape = AuroraShapes.RoundedSm)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
fun TextButtonTV(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    focusedBorderColor: Color = AuroraColors.Blue,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .auroraCardLift(isFocused = isFocused, shape = AuroraShapes.RoundedSm)
            .background(backgroundColor, AuroraShapes.RoundedSm)
            .border(1.dp, if (isFocused) focusedBorderColor else borderColor, AuroraShapes.RoundedSm)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
