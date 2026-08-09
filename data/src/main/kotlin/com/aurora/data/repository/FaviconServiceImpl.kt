package com.aurora.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.aurora.data.cache.FaviconCache
import com.aurora.data.service.FaviconService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class FaviconServiceImpl(
    private val cache: FaviconCache
) : FaviconService {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun getCached(domain: String): Bitmap? = cache.get(domain)

    override suspend fun getFavicon(domain: String): Bitmap? {
        cache.get(domain)?.let { return it }
        return fetchFavicon(domain)
    }

    override suspend fun fetchFavicon(domain: String): Bitmap? {
        val bitmap = downloadFromGoogle(domain)
            ?: downloadFromDirect(domain)
        bitmap?.let { cache.put(domain, it) }
        return bitmap
    }

    override fun preload(domain: String) {
        scope.launch { fetchFavicon(domain) }
    }

    private fun downloadFromGoogle(domain: String): Bitmap? {
        return download("https://www.google.com/s2/favicons?domain=$domain&sz=64")
    }

    private fun downloadFromDirect(domain: String): Bitmap? {
        return download("https://$domain/favicon.ico")
    }

    private fun download(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.instanceFollowRedirects = true
            val stream = conn.inputStream
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            conn.disconnect()
            if (bitmap != null && bitmap.width > 0) bitmap else null
        } catch (_: Exception) {
            null
        }
    }
}
