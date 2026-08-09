package com.aurora.data.cache

import android.graphics.Bitmap

interface ThumbnailCache {
    suspend fun get(url: String): Bitmap?
    suspend fun getOrNull(url: String): Bitmap?
    suspend fun put(url: String, bitmap: Bitmap)

    suspend fun warmCache(urls: List<String>)
    suspend fun evict(url: String)
    suspend fun clear()

    suspend fun size(): Int

    fun memoryCached(url: String): Boolean
    fun diskCached(url: String): Boolean
}
