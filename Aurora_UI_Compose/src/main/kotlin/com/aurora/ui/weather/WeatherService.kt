package com.aurora.ui.weather

import android.content.Context
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object WeatherService {
    private const val TAG = "AuroraWeather"
    private const val OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"
    private var cached: WeatherData? = null
    private var lastFetchMs: Long = 0L

    suspend fun getWeather(context: Context): WeatherData {
        if (cached != null && System.currentTimeMillis() - lastFetchMs < 15 * 60 * 1000) {
            return cached!!
        }
        return withContext(Dispatchers.IO) {
            try {
                val loc = getLocation(context)
                val json = fetchOpenMeteo(loc.first, loc.second)
                val temp = json.getJSONObject("current").getDouble("temperature_2m")
                val humidity = json.getJSONObject("current").optInt("relative_humidity_2m", 0)
                val wind = json.getJSONObject("current").optDouble("wind_speed_10m", 0.0)
                val code = json.getJSONObject("current").optInt("weather_code", 0)
                val data = WeatherData(
                    temperature = temp,
                    humidity = humidity,
                    windSpeed = wind,
                    condition = WeatherCondition.fromWmoCode(code),
                    location = loc.third
                )
                cached = data
                lastFetchMs = System.currentTimeMillis()
                data
            } catch (e: Exception) {
                Log.w(TAG, "Weather fetch failed", e)
                cached ?: WeatherData(22.0, 60, 5.0, WeatherCondition.PARTLY_CLOUDY, "Unknown")
            }
        }
    }

    private fun getLocation(context: Context): Triple<Double, Double, String> {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = listOf("gps", "network")
            for (p in providers) {
                try {
                    val loc = lm?.getLastKnownLocation(p)
                    if (loc != null) {
                        return Triple(loc.latitude, loc.longitude, "Current Location")
                    }
                } catch (_: SecurityException) {}
            }
            Triple(40.7128, -74.0060, "New York")
        } catch (_: Exception) {
            Triple(40.7128, -74.0060, "New York")
        }
    }

    private fun fetchOpenMeteo(lat: Double, lon: Double): JSONObject {
        val url = URL("$OPEN_METEO_URL?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return try {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }
}
