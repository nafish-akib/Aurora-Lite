package com.aurora.browser.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HomeCoordinator {
    var focusedZone by mutableStateOf("search")
    private val _focusedItemIndex = mutableIntStateOf(0)
    var focusedItemIndex: Int
        get() = _focusedItemIndex.intValue
        set(value) { _focusedItemIndex.intValue = value.coerceAtLeast(0) }
    var searchQuery by mutableStateOf("")
    var isOmniboxFocused by mutableStateOf(false)
}
