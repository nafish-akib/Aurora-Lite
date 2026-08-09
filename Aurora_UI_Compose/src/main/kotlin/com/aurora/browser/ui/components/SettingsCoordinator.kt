package com.aurora.browser.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SettingsCoordinator {
    var activeAccent by mutableStateOf("#4DA3FF")
    var activeTheme by mutableStateOf("Aurora Dark")
    var brightness by mutableIntStateOf(80)
    var largerUI by mutableStateOf(false)
    var searchEngine by mutableStateOf("Google")
    var isPerfOverlayEnabled by mutableStateOf(false)
    var isFpsCounterEnabled by mutableStateOf(true)
    var isMemoryUsageEnabled by mutableStateOf(true)
    var animationSpeedMultiplier by mutableFloatStateOf(1f)
    var activeSettingsCategory by mutableStateOf("Appearance")
    var isRemoteVisible by mutableStateOf(false)
    var isFocusInspectorEnabled by mutableStateOf(false)
}
