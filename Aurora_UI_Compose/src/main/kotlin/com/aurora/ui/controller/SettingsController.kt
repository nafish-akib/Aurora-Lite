package com.aurora.ui.controller

import com.aurora.ui.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsController {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setTheme(theme: String) { _state.update { it.copy(activeTheme = theme) } }
    fun setAccent(accent: String) { _state.update { it.copy(activeAccent = accent) } }
    fun setSearchEngine(engine: String) { _state.update { it.copy(searchEngine = engine) } }
    fun setAnimationSpeed(speed: Float) { _state.update { it.copy(animationSpeedMultiplier = speed) } }
    fun setBrightness(brightness: Int) { _state.update { it.copy(brightness = brightness) } }
    fun setLargerUI(enabled: Boolean) { _state.update { it.copy(largerUI = enabled) } }
    fun setSettingsCategory(category: String) { _state.update { it.copy(activeSettingsCategory = category) } }
    fun setPerfOverlayEnabled(enabled: Boolean) { _state.update { it.copy(isPerfOverlayEnabled = enabled) } }
    fun setFpsCounterEnabled(enabled: Boolean) { _state.update { it.copy(isFpsCounterEnabled = enabled) } }
    fun setMemoryUsageEnabled(enabled: Boolean) { _state.update { it.copy(isMemoryUsageEnabled = enabled) } }
    fun setRemoteVisible(enabled: Boolean) { _state.update { it.copy(isRemoteVisible = enabled) } }
    fun setDeveloperMode(enabled: Boolean) { _state.update { it.copy(developerMode = enabled) } }
    fun setSystemKeyboardEnabled(enabled: Boolean) { _state.update { it.copy(useSystemKeyboard = enabled) } }
}
