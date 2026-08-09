package com.aurora.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.aurora.data.cache.FaviconCache
import java.io.File
import java.security.MessageDigest

class FaviconCacheImpl(context: Context) : FaviconCache {
    private val memoryCache = LruCache<String, Bitmap>(48)
    private val diskDir = File(context.cacheDir, "favicons").also { it.mkdirs() }

    override suspend fun get(domain: String): Bitmap? {
        val key = hash(domain)
        memoryCache.get(key)?.let { return it }
        val file = File(diskDir, "$key.png")
        return if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            bitmap?.let { memoryCache.put(key, it) }
            bitmap
        } else null
    }

    override suspend fun getOrNull(domain: String): Bitmap? = get(domain)

    override suspend fun put(domain: String, bitmap: Bitmap) {
        val key = hash(domain)
        memoryCache.put(key, bitmap)
        val file = File(diskDir, "$key.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, file.outputStream())
    }

    override suspend fun warmCache(domains: List<String>) {}
    override suspend fun evict(domain: String) {
        val key = hash(domain)
        memoryCache.remove(key)
        File(diskDir, "$key.png").delete()
    }
    override suspend fun clear() {
        memoryCache.evictAll()
        diskDir.listFiles()?.forEach { it.delete() }
    }
    override suspend fun size(): Int = diskDir.listFiles()?.size ?: 0
    override fun memoryCached(domain: String): Boolean = memoryCache.get(hash(domain)) != null
    override fun diskCached(domain: String): Boolean = File(diskDir, "${hash(domain)}.png").exists()

    private fun hash(domain: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(domain.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
