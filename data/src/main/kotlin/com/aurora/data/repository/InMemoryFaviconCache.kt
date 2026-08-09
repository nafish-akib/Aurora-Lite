package com.aurora.data.repository

import android.graphics.Bitmap
import com.aurora.data.cache.FaviconCache

class InMemoryFaviconCache : FaviconCache {
    private val store = mutableMapOf<String, Bitmap>()

    override suspend fun get(domain: String): Bitmap? = store[domain]
    override suspend fun getOrNull(domain: String): Bitmap? = store[domain]
    override suspend fun put(domain: String, bitmap: Bitmap) { store[domain] = bitmap }
    override suspend fun warmCache(domains: List<String>) {}
    override suspend fun evict(domain: String) { store.remove(domain) }
    override suspend fun clear() { store.clear() }
    override suspend fun size(): Int = store.size
    override fun memoryCached(domain: String): Boolean = store.containsKey(domain)
    override fun diskCached(domain: String): Boolean = false
}
