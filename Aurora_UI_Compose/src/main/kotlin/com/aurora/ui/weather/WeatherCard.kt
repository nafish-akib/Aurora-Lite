package com.aurora.ui.weather

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun WeatherCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var weather by remember { mutableStateOf<WeatherData?>(null) }

    LaunchedEffect(Unit) {
        weather = WeatherService.getWeather(context)
    }

    val data = weather ?: return

    val cal = Calendar.getInstance()
    val hr = cal.get(Calendar.HOUR_OF_DAY)
    val isNight = hr < 6 || hr > 18
    val timeStr = "%02d:%02d".format(hr, cal.get(Calendar.MINUTE))
    val period = when { hr < 6 -> "Night"; hr < 12 -> "Morning"; hr < 17 -> "Afternoon"; hr < 21 -> "Evening"; else -> "Night" }

    val accentColor = when (data.condition) {
        WeatherCondition.CLEAR -> Color(0xFFFFD700)
        WeatherCondition.PARTLY_CLOUDY -> Color(0xFF87CEEB)
        WeatherCondition.CLOUDY, WeatherCondition.FOG -> Color(0xFFB0BEC5)
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> Color(0xFF4FC3F7)
        WeatherCondition.HEAVY_RAIN, WeatherCondition.THUNDERSTORM -> Color(0xFF7E57C2)
        WeatherCondition.SNOW -> Color(0xFFE0E0E0)
        WeatherCondition.UNKNOWN -> Color(0xFF4DA3FF)
    }

    val cardAlpha by animateFloatAsState(1f, tween(600), label = "cardIn")

    Box(modifier.fillMaxWidth().height(180.dp).padding(horizontal = 0.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))) {
            WeatherBackground(data.condition, isNight, Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)))

            Row(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(accentColor, RoundedCornerShape(4.dp)))
                        Spacer(Modifier.width(8.dp))
                        Text(timeStr, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(period, color = accentColor.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${data.temperature.toInt()}°", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Thin, letterSpacing = (-2).sp)
                        Spacer(Modifier.width(8.dp))
                        Column { Spacer(Modifier.height(4.dp)); Text(data.condition.label, color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp); Spacer(Modifier.height(2.dp)); Text("Feels like ${(data.temperature - 1.5).toInt()}°", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, Modifier.size(10.dp), Color.White.copy(alpha = 0.3f))
                            Spacer(Modifier.width(4.dp))
                            Text("${data.humidity}%", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, Modifier.size(10.dp), Color.White.copy(alpha = 0.3f))
                            Spacer(Modifier.width(4.dp))
                            Text("${data.windSpeed.toInt()} km/h", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(top = 4.dp)) {
                    Box(Modifier.size(56.dp).background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text(data.condition.emoji, fontSize = 26.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(data.location, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}
