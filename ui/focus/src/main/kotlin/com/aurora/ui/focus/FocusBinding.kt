package com.aurora.ui.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusBinding(
    id: String,
    focusEngine: FocusEngine,
    group: String = "default",
    order: Int = 0,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    externalFocused: Boolean = false,
    content: @Composable (isFocused: Boolean) -> Unit
) {
    val focusedId by focusEngine.focusedId.collectAsState()
    val isFocused = (externalFocused || (focusedId == id)) && enabled
    val bringIntoView = remember { BringIntoViewRequester() }

    val node = remember(id, group, order) { FocusableNode(id = id, group = group, order = order) }

    DisposableEffect(id) {
        focusEngine.register(node, onSelect = {
            if (enabled) onClick()
        })
        onDispose { focusEngine.unregister(id) }
    }

    LaunchedEffect(isFocused) {
        if (isFocused) bringIntoView.bringIntoView()
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier.then(
            if (enabled) Modifier.clickable { onClick() } else Modifier
        ).bringIntoViewRequester(bringIntoView)
    ) {
        content(isFocused)
    }
}
