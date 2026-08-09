package com.aurora.browser.ui.components

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aurora.engine.InputBridge

class BrowserCoordinator {
    var remoteX by mutableFloatStateOf(540f)
    var remoteY by mutableFloatStateOf(380f)
    var isPointerMode by mutableStateOf(false)
    var remoteClicked by mutableStateOf(false)
    var toolbarVisible by mutableStateOf(true)
    var tabWorkspaceVisible by mutableStateOf(false)
    var isKeyboardOpen by mutableStateOf(false)
    var isTabWorkspaceOpen by mutableStateOf(false)
    var scrollDelta by mutableStateOf(Pair(0f, 0f))
    var scrollTick by mutableLongStateOf(0L)
    var toolbarHeightPx by mutableFloatStateOf(200f)
    var inputBridge: InputBridge? = null
    fun scrollBy(dx: Float, dy: Float) {
        Log.d("AuroraBrowser", "scrollBy called bridge=${inputBridge != null} dx=$dx dy=$dy")
        val bridge = inputBridge ?: return
        bridge.injectScroll(dx, dy)
    }
}
