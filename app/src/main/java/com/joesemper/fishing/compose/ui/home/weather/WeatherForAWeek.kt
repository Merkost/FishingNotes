package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joesemper.fishing.model.entity.weather.WeatherForecast

@Composable
fun WeatherForAWeek(weather: WeatherForecast) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Text(text = weather.daily.first().weather.first().description)
    }
}