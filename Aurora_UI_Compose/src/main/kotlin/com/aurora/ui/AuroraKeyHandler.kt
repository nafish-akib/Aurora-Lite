package com.aurora.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import com.aurora.browser.ui.components.OverlayCoordinator
import com.aurora.browser.ui.components.SettingsCoordinator
import com.aurora.ui.types.Screen

internal fun Modifier.auroraKeyHandler(
    settings: SettingsCoordinator,
    overlayCoordinator: OverlayCoordinator,
    currentScreen: Screen,
    isDiagnosticsOpen: Boolean,
    isVoiceListening: Boolean,
    onSetDiagnosticsOpen: (Boolean) -> Unit,
    onDpadPress: (String) -> Boolean,
    onSelectPress: () -> Boolean,
    onBackPress: () -> Unit,
    onTriggerToast: (String) -> Unit
): Modifier = this.onKeyEvent { event ->
    val isRepeat = event.nativeKeyEvent?.repeatCount?.let { it > 0 } ?: false
    if (isRepeat && (event.key == Key.Enter || event.key == Key.DirectionCenter)) {
        return@onKeyEvent true
    }
    when {
        event.key == Key.F1 -> {
            settings.isRemoteVisible = !settings.isRemoteVisible
            onTriggerToast(if (settings.isRemoteVisible) "Remote Panel Shown" else "Remote Panel Hidden")
            true
        }
        event.key == Key.F2 -> {
            overlayCoordinator.openCommandPalette()
            true
        }
        event.key == Key.Menu -> {
            if (overlayCoordinator.isShowing || isDiagnosticsOpen || isVoiceListening) {
                overlayCoordinator.close()
                onSetDiagnosticsOpen(false)
            } else if (currentScreen == Screen.Browser) {
                overlayCoordinator.openContextMenu()
            } else {
                overlayCoordinator.openQuickSettings()
            }
            true
        }
        currentScreen == Screen.Browser -> when (event.key) {
            Key.DirectionUp -> { onDpadPress("UP"); true }
            Key.DirectionDown -> { onDpadPress("DOWN"); true }
            Key.DirectionLeft -> { onDpadPress("LEFT"); true }
            Key.DirectionRight -> { onDpadPress("RIGHT"); true }
            Key.Enter, Key.DirectionCenter -> { onSelectPress(); true }
            else -> false
        }
        event.key == Key.DirectionUp -> onDpadPress("UP")
        event.key == Key.DirectionDown -> onDpadPress("DOWN")
        event.key == Key.DirectionLeft -> onDpadPress("LEFT")
        event.key == Key.DirectionRight -> onDpadPress("RIGHT")
        event.key == Key.Enter -> onSelectPress()
        event.key == Key.DirectionCenter -> onSelectPress()
        event.key == Key.Back -> { onBackPress(); true }
        else -> false
    }
}