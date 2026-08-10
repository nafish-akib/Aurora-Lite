package com.aurora.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var weather by remember { mutableStateOf<WeatherData?>(null) }

    LaunchedEffect(Unit) {
        weather = WeatherService.getWeather(context)
    }

    val data = weather
    if (data == null) {
        Box(modifier.fillMaxWidth().height(0.dp))
        return
    }

    val gradientColors = when (data.condition) {
        WeatherCondition.CLEAR -> listOf(Color(0x40FFD700), Color.Transparent)
        WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN, WeatherCondition.DRIZZLE -> listOf(Color(0x404DA3FF), Color.Transparent)
        WeatherCondition.THUNDERSTORM -> listOf(Color(0x40A78BFA), Color.Transparent)
        WeatherCondition.SNOW -> listOf(Color(0x40FFFFFF), Color.Transparent)
        else -> listOf(Color(0x20FFFFFF), Color.Transparent)
    }

    Box(modifier.fillMaxWidth().height(140.dp).padding(top = 8.dp)) {
        WeatherAnimation(data.condition, Modifier.fillMaxWidth().height(140.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    data.location,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${data.temperature.toInt()}°",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Thin
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        data.condition.label,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Humidity ${data.humidity}%",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Wind ${data.windSpeed.toInt()} km/h",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Box(
                modifier = Modifier.size(64.dp).background(Brush.radialGradient(gradientColors), RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(data.condition.emoji, fontSize = 28.sp)
            }
        }
    }
}
