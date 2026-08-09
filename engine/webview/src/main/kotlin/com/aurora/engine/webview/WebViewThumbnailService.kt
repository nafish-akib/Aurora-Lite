package com.aurora.engine.webview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import com.aurora.data.cache.ThumbnailCache
import com.aurora.data.service.ThumbnailService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WebView thumbnail service. WebView exposes no frame-capture API, so we
 * offscreen-draw the live view into a bitmap (only meaningful while the
 * session has a sized view attached).
 */
class WebViewThumbnailService(
    private val viewProvider: () -> WebView?,
    private val cache: ThumbnailCache
) : ThumbnailService {

    private val scope = CoroutineScope(Dispatchers.Main)

    override suspend fun capture(): Bitmap? = withContext(Dispatchers.Main) {
        val view = viewProvider() ?: return@withContext null
        if (view.width <= 0 || view.height <= 0) return@withContext null
        try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (t: Throwable) {
            null
        }
    }

    override suspend fun getThumbnail(url: String): Bitmap? {
        cache.get(url)?.let { return it }
        return captureAndCache(url)
    }

    override suspend fun captureAndCache(url: String): Bitmap? {
        delay(500)
        val bitmap = capture() ?: return null
        val resized = Bitmap.createScaledBitmap(bitmap, 360, 200, true)
        cache.put(url, resized)
        if (resized !== bitmap) bitmap.recycle()
        return resized
    }

    override fun scheduleCapture(url: String) {
        scope.launch {
            delay(1000)
            captureAndCache(url)
        }
    }

    override fun cancel() {
        scope.coroutineContext[Job]?.cancel()
    }
}