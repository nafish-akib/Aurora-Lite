package com.aurora.data.service

import android.graphics.Bitmap

interface FaviconService {
    suspend fun getCached(domain: String): Bitmap?
    suspend fun getFavicon(domain: String): Bitmap?
    suspend fun fetchFavicon(domain: String): Bitmap?
    fun preload(domain: String)
}
