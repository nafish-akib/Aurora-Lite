package com.aurora.data.repository

import android.graphics.Bitmap
import com.aurora.data.cache.ThumbnailCache

class InMemoryThumbnailCache : ThumbnailCache {
    private val store = mutableMapOf<String, Bitmap>()

    override suspend fun get(url: String): Bitmap? = store[hash(url)]
    override suspend fun getOrNull(url: String): Bitmap? = store[hash(url)]
    override suspend fun put(url: String, bitmap: Bitmap) { store[hash(url)] = bitmap }
    override suspend fun warmCache(urls: List<String>) {}
    override suspend fun evict(url: String) { store.remove(hash(url)) }
    override suspend fun clear() { store.clear() }
    override suspend fun size(): Int = store.size
    override fun memoryCached(url: String): Boolean = store.containsKey(hash(url))
    override fun diskCached(url: String): Boolean = false

    private fun hash(url: String): String {
        val digest = java.security.MessageDigest.getInstance("MD5")
        return digest.digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
