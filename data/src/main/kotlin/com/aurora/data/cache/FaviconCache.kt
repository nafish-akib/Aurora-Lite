package com.aurora.data.cache

import android.graphics.Bitmap

interface FaviconCache {
    suspend fun get(domain: String): Bitmap?
    suspend fun getOrNull(domain: String): Bitmap?
    suspend fun put(domain: String, bitmap: Bitmap)
    suspend fun warmCache(domains: List<String>)
    suspend fun evict(domain: String)
    suspend fun clear()
    suspend fun size(): Int
    fun memoryCached(domain: String): Boolean
    fun diskCached(domain: String): Boolean
}
