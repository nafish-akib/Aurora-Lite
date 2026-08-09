package com.aurora.ui.engine

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aurora.engine.BrowserSession
import com.aurora.engine.InputBridge

/**
 * Engine-agnostic view host. The engine supplies the Android view (via
 * [BrowserSession.createView]) and the input bridge; this Composable hosts them
 * and wires TV remote / cursor handling exactly once.
 */
@Composable
fun EngineView(
    session: BrowserSession,
    modifier: Modifier = Modifier,
    blockFocus: Boolean = false,
    onInputBridgeReady: (InputBridge) -> Unit = {},
    onHoverMove: (Float, Float) -> Unit = { _, _ -> },
    onKeySelectPress: () -> Unit = {},
    onDpadPress: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val view = remember(session, context) { session.createView(context) }
    val focusRequester = remember { FocusRequester() }
    val currentBlockFocus = rememberUpdatedState(blockFocus)
    val currentOnHoverMove = rememberUpdatedState(onHoverMove)
    val currentOnKeySelectPress = rememberUpdatedState(onKeySelectPress)
    val currentOnDpadPress = rememberUpdatedState(onDpadPress)
    val currentOnInputBridgeReady = rememberUpdatedState(onInputBridgeReady)

    val inputBridge = remember(session) { session.createInputBridge() }

    DisposableEffect(Unit) {
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                if (!v.hasFocus() && !currentBlockFocus.value) {
                    v.requestFocus()
                }
            }

            override fun onViewDetachedFromWindow(v: View) { }
        }
        view.addOnAttachStateChangeListener(attachListener)

val hoverListener = View.OnGenericMotionListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_HOVER_MOVE -> currentOnHoverMove.value(event.x, event.y)
                else -> { }
            }
            false
        }
        view.setOnGenericMotionListener(hoverListener)

        val keyListener = View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.repeatCount > 0) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) true else false
                } else {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { currentOnKeySelectPress.value(); true }
                        KeyEvent.KEYCODE_DPAD_UP -> { currentOnDpadPress.value("UP"); true }
                        KeyEvent.KEYCODE_DPAD_DOWN -> { currentOnDpadPress.value("DOWN"); true }
                        KeyEvent.KEYCODE_DPAD_LEFT -> { currentOnDpadPress.value("LEFT"); true }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> { currentOnDpadPress.value("RIGHT"); true }
                        else -> false
                    }
                }
            } else false
        }
        view.setOnKeyListener(keyListener)

        onDispose {
            view.removeOnAttachStateChangeListener(attachListener)
            view.setOnGenericMotionListener(null)
            view.setOnKeyListener(null)
        }
    }

    LaunchedEffect(inputBridge) {
        if (inputBridge != null) {
            currentOnInputBridgeReady.value(inputBridge)
        }
    }

    LaunchedEffect(Unit) {
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        if (!view.hasFocus() && !currentBlockFocus.value) {
            view.requestFocus()
        }
    }

    LaunchedEffect(blockFocus) {
        view.isFocusable = !blockFocus
        view.isFocusableInTouchMode = !blockFocus
        if (blockFocus) {
            view.clearFocus()
        }
    }

    AndroidView(
        factory = { view },
        update = { },
        modifier = modifier.focusRequester(focusRequester)
    )
}