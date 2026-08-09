package com.aurora.data.service

import android.graphics.Bitmap

interface ThumbnailService {
    suspend fun capture(): Bitmap?

    suspend fun getThumbnail(url: String): Bitmap?

    suspend fun captureAndCache(url: String): Bitmap?

    fun scheduleCapture(url: String)
    
    fun cancel()
}
