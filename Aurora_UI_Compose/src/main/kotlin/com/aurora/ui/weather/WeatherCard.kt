package com.aurora.ui.weather

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun WeatherCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var data by remember { mutableStateOf<WeatherData?>(null) }
    LaunchedEffect(Unit) { data = WeatherService.getWeather(context) }
    val w = data ?: return
    val cal = Calendar.getInstance()
    val hr = cal.get(Calendar.HOUR_OF_DAY); val min = cal.get(Calendar.MINUTE)
    val period = when { hr < 6 -> "Night"; hr < 12 -> "Morning"; hr < 17 -> "Afternoon"; hr < 21 -> "Evening"; else -> "Night" }
    val isNight = hr < 6 || hr > 18

    val accent = when (w.condition) {
        WeatherCondition.CLEAR -> if (isNight) Color(0xFF8899CC) else Color(0xFFFFB703)
        WeatherCondition.PARTLY_CLOUDY -> Color(0xFF5BA0D0)
        WeatherCondition.CLOUDY, WeatherCondition.FOG -> Color(0xFF8899AA)
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> Color(0xFF4F9FCF)
        WeatherCondition.HEAVY_RAIN, WeatherCondition.THUNDERSTORM -> Color(0xFF6B5C9E)
        WeatherCondition.SNOW -> Color(0xFFB8C8D8)
        WeatherCondition.UNKNOWN -> Color(0xFF4DA3FF)
    }

    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp))) {
            WeatherBackground(w.condition, isNight, Modifier.fillMaxWidth().height(200.dp), overlayOnly = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0x12FFFFFF)).border(0.5.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("%02d:%02d".format(hr, min), color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(4.dp).background(accent, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(period, color = accent.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${w.temperature.toInt()}", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Thin, letterSpacing = (-3).sp)
                    Text("°", color = Color.White.copy(alpha = 0.5f), fontSize = 26.sp, fontWeight = FontWeight.Thin)
                    Spacer(Modifier.width(14.dp))
                    Column { Spacer(Modifier.height(6.dp)); Text("H ${w.tempHigh.toInt()}°", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace); Spacer(Modifier.height(2.dp)); Text("L ${w.tempLow.toInt()}°", color = Color.White.copy(alpha = 0.25f), fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                }
                Spacer(Modifier.height(2.dp))
                Text(w.condition.label, color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("HI ${(w.temperature - 1.5).toInt()}°", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.width(12.dp))
                    Text("RH ${w.humidity}%", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.width(12.dp))
                    Text("WS ${w.windSpeed.toInt()}", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    if (w.sunrise != null) {
                        Spacer(Modifier.width(12.dp))
                        Text("☀${w.sunrise}", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(Modifier.size(60.dp).background(accent.copy(alpha = 0.08f), RoundedCornerShape(18.dp)).border(0.5.dp, accent.copy(alpha = 0.15f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                    Text(w.condition.emoji, fontSize = 28.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(w.location, color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
            }
        }
    }
}
