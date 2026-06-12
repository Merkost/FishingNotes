package com.mobileprism.fishing.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.Temperature
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.Weather
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.weather.CurrentWeather
import com.mobileprism.fishing.ui.home.weather.DailyWeatherItem

private val hourOffsets = listOf(0L, 3600L, 7200L, 10800L, 14400L, 18000L)
private val hourTemps = listOf(24f, 23f, 22f, 20f, 19f, 18f)
private val hourIcons = listOf("02d", "02d", "03d", "03d", "04d", "01n")

private val sampleForecast = WeatherForecast(
    latitude = -17.1708,
    longitude = 145.5958,
    hourly = hourOffsets.mapIndexed { i, dt ->
        Hourly(
            date = dt,
            temperature = hourTemps[i],
            pressure = 1012 + (i - 3),
            humidity = 60 + i,
            clouds = 40 + i * 5,
            windSpeed = 8f + i * 0.5f,
            windDeg = 45,
            weather = listOf(Weather(description = "Partly cloudy", icon = hourIcons[i])),
            probabilityOfPrecipitation = 0f,
        )
    },
    daily = listOf(
        Daily(
            date = 1780711200L,
            moonPhase = 0.65f,
            pressure = 1012,
            humidity = 64,
            windSpeed = 8f,
            windDeg = 45,
            weather = listOf(Weather(description = "Partly cloudy", icon = "02d")),
            temperature = Temperature(day = 24f, min = 17f, max = 27f, night = 17f, evening = 21f, morning = 18f),
        ),
        Daily(
            date = 1780797600L,
            moonPhase = 0.72f,
            pressure = 1010,
            humidity = 68,
            windSpeed = 10f,
            windDeg = 50,
            weather = listOf(Weather(description = "Light rain", icon = "10d")),
            temperature = Temperature(day = 22f, min = 16f, max = 25f, night = 16f, evening = 20f, morning = 17f),
        ),
        Daily(
            date = 1780884000L,
            moonPhase = 0.80f,
            pressure = 1014,
            humidity = 58,
            windSpeed = 6f,
            windDeg = 35,
            weather = listOf(Weather(description = "Sunny", icon = "01d")),
            temperature = Temperature(day = 26f, min = 18f, max = 29f, night = 18f, evening = 23f, morning = 19f),
        ),
    ),
)

@Composable
private fun FakeWeatherScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        DefaultAppBar(title = "Lake Tinaroo", subtitle = "Saturday · 2:14 PM")
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CurrentWeather(
                forecast = sampleForecast,
                pressureUnit = PressureValues.Hpa,
                temperatureUnit = TemperatureValues.C,
                windSpeedUnit = WindSpeedValues.kmph,
                is12hTimeFormat = false,
            )
            sampleForecast.daily.forEachIndexed { index, daily ->
                DailyWeatherItem(
                    forecast = daily,
                    temperatureUnit = TemperatureValues.C,
                    onDailyWeatherClick = {},
                )
            }
        }
    }
}

@Preview(
    name = "Weather · marketing",
    device = Devices.PIXEL_7,
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
)
@Composable
fun WeatherMarketingPreview() {
    MarketingFrame(
        headline = "Know the conditions.",
        subline = "Live weather and daily forecasts for every saved spot.",
    ) {
        FakeWeatherScreen()
    }
}
