package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.WeatherResponse
import kotlinx.coroutines.delay

@Composable
fun WeatherHeader(
    weather: WeatherResponse,
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(weather) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    val weatherEmoji = getWeatherEmoji(
        weather.weather.firstOrNull()?.main ?: "Clear",
        isNight
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it / 3 }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City name
            Text(
                text = "${weather.cityName}, ${weather.sys.country}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weather emoji
            Text(
                text = weatherEmoji,
                fontSize = 72.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Temperature
            Text(
                text = "${weather.main.temp.toInt()}°",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-2).sp
            )

            // Description
            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(
                    icon = { Icon(Icons.Rounded.Thermostat, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp)) },
                    label = "Feels like",
                    value = "${weather.main.feelsLike.toInt()}°"
                )
                WeatherDetail(
                    icon = { Icon(Icons.Rounded.WaterDrop, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp)) },
                    label = "Humidity",
                    value = "${weather.main.humidity}%"
                )
                WeatherDetail(
                    icon = { Icon(Icons.Rounded.Air, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp)) },
                    label = "Wind",
                    value = "${(weather.wind.speed * 3.6).toInt()} km/h"
                )
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 0.3.sp
        )
    }
}

fun getWeatherEmoji(condition: String, isNight: Boolean): String {
    val conditionLower = condition.lowercase()
    return when {
        conditionLower.contains("thunderstorm") -> "⛈️"
        conditionLower.contains("drizzle") -> "🌦️"
        conditionLower.contains("rain") -> if (isNight) "🌧️" else "🌦️"
        conditionLower.contains("snow") -> "🌨️"
        conditionLower.contains("mist") || conditionLower.contains("fog") || conditionLower.contains("haze") -> "🌫️"
        conditionLower.contains("clear") -> if (isNight) "🌙" else "☀️"
        conditionLower.contains("cloud") -> if (isNight) "☁️" else "⛅"
        else -> "🌤️"
    }
}
