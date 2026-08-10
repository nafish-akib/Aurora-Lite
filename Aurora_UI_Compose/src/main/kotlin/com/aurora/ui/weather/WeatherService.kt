package com.aurora.ui.weather

import android.content.Context
import android.location.Address
import android.location.Geocoder
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
    private const val NOMINATIM = "https://nominatim.openstreetmap.org/reverse"
    private var cached: WeatherData? = null
    private var lastFetchMs: Long = 0L

    suspend fun getWeather(context: Context): WeatherData {
        if (cached != null && System.currentTimeMillis() - lastFetchMs < 15 * 60 * 1000) return cached!!
        return withContext(Dispatchers.IO) {
            try {
                val loc = getBestLocation(context)
                val lat = loc.first; val lon = loc.second
                if (lat.isNaN() || lon.isNaN()) {
                    return@withContext WeatherData(0.0, 0, 0.0, WeatherCondition.UNKNOWN, "Unavailable")
                }
                val json = fetch("$OPEN_METEO?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto&forecast_days=1", emptyMap())
                val c = json.getJSONObject("current")
                val d = json.getJSONObject("daily")
                val city = resolveCity(context, lat, lon, loc.third)
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
                cached ?: WeatherData(22.0, 60, 5.0, WeatherCondition.PARTLY_CLOUDY, "Unknown")
            }
        }
    }

    private fun getBestLocation(context: Context): Triple<Double, Double, String> {
        tryGetIpLocation("https://ipapi.co/json/", "")?.let { return it }
        tryGetIpLocation("https://ipinfo.io/json", "city")?.let { return it }
        tryGetGpsLocation(context)?.let { return Triple(it.first, it.second, "") }
        return Triple(Double.NaN, Double.NaN, "")
    }

    private fun tryGetIpLocation(url: String, cityField: String): Triple<Double, Double, String>? {
        return try {
            val json = fetch(url, emptyMap())
            val lat = json.optDouble("latitude", Double.NaN).let { if (it.isNaN()) json.optDouble("loc", Double.NaN).let { l -> if (!l.isNaN()) l.toString().split(",").firstOrNull()?.toDoubleOrNull() ?: Double.NaN else Double.NaN } else it }
            val lon = json.optDouble("longitude", Double.NaN).let { if (it.isNaN()) json.optString("loc", "").split(",").getOrNull(1)?.trim()?.toDoubleOrNull() ?: Double.NaN else it }
            val city = json.optString(if (cityField.isNotBlank()) cityField else "city", "")
            if (!lat.isNaN() && !lon.isNaN()) Triple(lat, lon, city) else null
        } catch (_: Exception) { null }
    }

    private fun tryGetGpsLocation(context: Context): Pair<Double, Double>? {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            for (p in listOf("gps", "network")) {
                try { lm?.getLastKnownLocation(p)?.let { return it.latitude to it.longitude } } catch (_: SecurityException) {}
            }
        } catch (_: Exception) {}
        return null
    }

    private suspend fun resolveCity(context: Context, lat: Double, lon: Double, ipCity: String): String {
        if (ipCity.isNotBlank()) return ipCity
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addrs = geocoder.getFromLocation(lat, lon, 1) as? MutableList<Address>
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

    private fun fetch(url: String, headers: Map<String, String>): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000; conn.readTimeout = 8000
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        return try { JSONObject(conn.inputStream.bufferedReader().use { it.readText() }) } finally { conn.disconnect() }
    }
}
