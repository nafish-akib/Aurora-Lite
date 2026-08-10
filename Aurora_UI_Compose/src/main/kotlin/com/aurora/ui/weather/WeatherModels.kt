package com.aurora.ui.weather

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val location: String
)

enum class WeatherCondition(val label: String, val emoji: String) {
    CLEAR("Clear", "☀"),
    PARTLY_CLOUDY("Partly Cloudy", "⛅"),
    CLOUDY("Cloudy", "☁"),
    FOG("Foggy", "🌫"),
    DRIZZLE("Drizzle", "🌦"),
    RAIN("Rain", "🌧"),
    HEAVY_RAIN("Heavy Rain", "⛈"),
    SNOW("Snow", "🌨"),
    THUNDERSTORM("Thunderstorm", "⚡"),
    UNKNOWN("Unknown", "🌡");

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2, 3 -> PARTLY_CLOUDY
            45, 48 -> FOG
            51, 53, 55 -> DRIZZLE
            61, 63, 65 -> RAIN
            80, 81, 82 -> HEAVY_RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}
