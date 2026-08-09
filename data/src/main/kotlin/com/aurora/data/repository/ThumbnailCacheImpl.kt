package com.aurora.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.aurora.data.cache.ThumbnailCache
import java.io.File
import java.security.MessageDigest

class ThumbnailCacheImpl(context: Context) : ThumbnailCache {
    private val memoryCache = LruCache<String, Bitmap>(20)
    private val diskDir = File(context.cacheDir, "thumbnails").also { it.mkdirs() }

    override suspend fun get(url: String): Bitmap? {
        val key = hash(url)
        memoryCache.get(key)?.let { return it }
        val file = File(diskDir, key)
        return if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            bitmap?.let { memoryCache.put(key, it) }
            bitmap
        } else null
    }

    override suspend fun getOrNull(url: String): Bitmap? = get(url)

    override suspend fun put(url: String, bitmap: Bitmap) {
        val key = hash(url)
        memoryCache.put(key, bitmap)
        val file = File(diskDir, key)
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, file.outputStream())
    }

    override suspend fun warmCache(urls: List<String>) {
    }

    override suspend fun evict(url: String) {
        val key = hash(url)
        memoryCache.remove(key)
        File(diskDir, key).delete()
    }

    override suspend fun clear() {
        memoryCache.evictAll()
        diskDir.listFiles()?.forEach { it.delete() }
    }

    override suspend fun size(): Int {
        return diskDir.listFiles()?.size ?: 0
    }

    override fun memoryCached(url: String): Boolean {
        return memoryCache.get(hash(url)) != null
    }

    override fun diskCached(url: String): Boolean {
        return File(diskDir, hash(url)).exists()
    }

    private fun hash(url: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
