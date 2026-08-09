package com.aurora.ui.model

data class SettingsUiState(
    val activeTheme: String = "Aurora Dark",
    val activeAccent: String = "#4DA3FF",
    val searchEngine: String = "Google",
    val animationSpeedMultiplier: Float = 1f,
    val brightness: Int = 80,
    val largerUI: Boolean = false,
    val activeSettingsCategory: String = "Appearance",
    val developerMode: Boolean = false,
    val isPerfOverlayEnabled: Boolean = false,
    val isFpsCounterEnabled: Boolean = true,
    val isMemoryUsageEnabled: Boolean = true,
    val isRemoteVisible: Boolean = false,
    val isFocusInspectorEnabled: Boolean = false,
    val useSystemKeyboard: Boolean = true
)
