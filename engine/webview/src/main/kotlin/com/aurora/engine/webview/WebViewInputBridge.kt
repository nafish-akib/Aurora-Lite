package com.aurora.engine.webview

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.webkit.WebView
import com.aurora.engine.InputBridge
import com.aurora.engine.PointerIconType

/**
 * Input bridge for TV remote / cursor injection. Routes injected events
 * straight into the WebView (which owns its own input dispatch).
 */
class WebViewInputBridge(
    private val viewProvider: () -> WebView?
) : InputBridge {

    override var onPointerIconChange: ((PointerIconType) -> Unit)? = null

    private var lastHoverX = -1f
    private var lastHoverY = -1f
    private var lastHoverTime = 0L

    override fun injectHoverMove(x: Float, y: Float) {
        val view = viewProvider() ?: return
        val now = SystemClock.uptimeMillis()
        if (Math.abs(x - lastHoverX) < 1.0f && Math.abs(y - lastHoverY) < 1.0f && (now - lastHoverTime) < 16L) {
            return
        }
        lastHoverX = x
        lastHoverY = y
        lastHoverTime = now
        val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_HOVER_MOVE, x, y, 0)
        event.source = InputDevice.SOURCE_MOUSE
        try {
            view.dispatchGenericMotionEvent(event)
        } finally {
            event.recycle()
        }
    }

    override fun injectClick(x: Float, y: Float) {
        val view = viewProvider() ?: return
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)
        down.source = InputDevice.SOURCE_MOUSE
        try {
            view.dispatchTouchEvent(down)
        } finally {
            down.recycle()
        }
        val up = MotionEvent.obtain(now, now + 50, MotionEvent.ACTION_UP, x, y, 0)
        up.source = InputDevice.SOURCE_MOUSE
        try {
            view.dispatchTouchEvent(up)
        } finally {
            up.recycle()
        }
    }

    override fun injectScroll(deltaX: Float, deltaY: Float) {
        val view = viewProvider() ?: return
        if (deltaX == 0f && deltaY == 0f) return
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_SCROLL, deltaX, deltaY, 0)
        event.source = InputDevice.SOURCE_MOUSE
        try {
            view.dispatchGenericMotionEvent(event)
        } finally {
            event.recycle()
        }
    }
}