package com.aurora.ui.weather

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object WeatherService {
    private const val TAG = "AuroraWeather"
    private const val OPEN_METEO = "https://api.open-meteo.com/v1/forecast"
    private const val NOMINATIM = "https://nominatim.openstreetmap.org/reverse"
    private var cached: WeatherData? = null
    private var lastFetchMs: Long = 0L

    suspend fun getWeather(context: Context): WeatherData {
        if (cached != null && System.currentTimeMillis() - lastFetchMs < 15 * 60 * 1000) return cached!!
        return withContext(Dispatchers.IO) {
            try {
                val (lat, lon) = getLocation(context)
                val json = fetch("$OPEN_METEO?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto")
                val c = json.getJSONObject("current")
                val name = reverseGeocode(context, lat, lon)
                val data = WeatherData(
                    temperature = c.getDouble("temperature_2m"),
                    humidity = c.optInt("relative_humidity_2m", 0),
                    windSpeed = c.optDouble("wind_speed_10m", 0.0),
                    condition = WeatherCondition.fromWmoCode(c.optInt("weather_code", 0)),
                    location = name
                )
                cached = data; lastFetchMs = System.currentTimeMillis(); data
            } catch (e: Exception) {
                Log.w(TAG, "Weather fetch failed", e)
                cached ?: WeatherData(22.0, 60, 5.0, WeatherCondition.PARTLY_CLOUDY, "Unknown")
            }
        }
    }

    private fun getLocation(context: Context): Pair<Double, Double> {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            for (p in listOf("gps", "network")) {
                try { lm?.getLastKnownLocation(p)?.let { return it.latitude to it.longitude } } catch (_: SecurityException) {}
            }
        } catch (_: Exception) {}
        return 40.7128 to -74.0060
    }

    private suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): String {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addrs: MutableList<Address>? = geocoder.getFromLocation(lat, lon, 1)
            val a = addrs?.firstOrNull()
            val name = a?.locality ?: a?.subAdminArea ?: a?.adminArea ?: a?.countryName
            if (!name.isNullOrBlank()) return name
        } catch (_: Exception) {}
        return try {
            val json = fetch("$NOMINATIM?lat=$lat&lon=$lon&format=json&zoom=10", mapOf("User-Agent" to "AuroraBrowser/1.0"))
            val addr = json.getJSONObject("address")
            addr.optString("city", "").ifBlank { addr.optString("town", "").ifBlank { addr.optString("village", "").ifBlank { addr.optString("state", "").ifBlank { addr.optString("country", "Unknown") } } } }
        } catch (_: Exception) { "Unknown" }
    }

    private fun fetch(url: String, headers: Map<String, String> = emptyMap()): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000; conn.readTimeout = 8000
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        return try { JSONObject(conn.inputStream.bufferedReader().use { it.readText() }) } finally { conn.disconnect() }
    }
}
