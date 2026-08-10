package com.aurora.ui.weather

import android.content.Context
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object WeatherService {
    private const val TAG = "AuroraWeather"
    private const val OPEN_METEO = "https://api.open-meteo.com/v1/forecast"
    private var cached: WeatherData? = null
    private var lastFetchMs: Long = 0L

    suspend fun getWeather(context: Context): WeatherData? {
        if (cached != null && System.currentTimeMillis() - lastFetchMs < 15 * 60 * 1000) return cached!!
        return withContext(Dispatchers.IO) {
            try {
                val (lat, lon, city) = getBestLocation(context)
                if (lat.isNaN() || lon.isNaN()) return@withContext null
                val json = fetch("$OPEN_METEO?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto&forecast_days=1")
                val c = json.getJSONObject("current")
                val d = json.getJSONObject("daily")
                val data = WeatherData(
                    temperature = c.getDouble("temperature_2m"),
                    humidity = c.optInt("relative_humidity_2m", 0),
                    windSpeed = c.optDouble("wind_speed_10m", 0.0),
                    condition = WeatherCondition.fromWmoCode(c.optInt("weather_code", 0)),
                    location = city,
                    tempHigh = d.optJSONArray("temperature_2m_max")?.optDouble(0) ?: (c.getDouble("temperature_2m") + 3),
                    tempLow = d.optJSONArray("temperature_2m_min")?.optDouble(0) ?: (c.getDouble("temperature_2m") - 4),
                    sunrise = d.optJSONArray("sunrise")?.optString(0)?.takeLast(5),
                    sunset = d.optJSONArray("sunset")?.optString(0)?.takeLast(5)
                )
                cached = data; lastFetchMs = System.currentTimeMillis(); data
            } catch (e: Exception) {
                Log.w(TAG, "Weather fetch failed", e)
                cached
            }
        }
    }

    private fun getBestLocation(context: Context): Triple<Double, Double, String> {
        for (url in listOf("https://ipapi.co/json/", "https://ipinfo.io/json", "https://ifconfig.co/json")) {
            try { parseIpResponse(fetch(url))?.let { return it } } catch (_: Exception) {}
        }
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            for (p in listOf("gps", "network")) {
                try { lm?.getLastKnownLocation(p)?.let { return Triple(it.latitude, it.longitude, "") } } catch (_: SecurityException) {}
            }
        } catch (_: Exception) {}
        return Triple(Double.NaN, Double.NaN, "")
    }

    private fun parseIpResponse(json: JSONObject): Triple<Double, Double, String>? {
        val lat = json.optDouble("latitude", Double.NaN)
            .takeUnless { it.isNaN() } ?: json.optString("loc", "").split(",").firstOrNull()?.trim()?.toDoubleOrNull()
        val lon = json.optDouble("longitude", Double.NaN)
            .takeUnless { it.isNaN() } ?: json.optString("loc", "").split(",").getOrNull(1)?.trim()?.toDoubleOrNull()
        val city = json.optString("city", "").ifBlank { json.optString("region", "") }
        if (lat != null && lon != null) return Triple(lat, lon, city)
        return null
    }

    private fun fetch(url: String): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000; conn.readTimeout = 5000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "AuroraBrowser/1.0")
        return try { JSONObject(conn.inputStream.bufferedReader().use { it.readText() }) } finally { conn.disconnect() }
    }
}
