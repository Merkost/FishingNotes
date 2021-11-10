package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.theme.backgroundGreenColor
import com.joesemper.fishing.compose.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySeconds
import com.joesemper.fishing.utils.getTimeBySeconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForADay(weather: WeatherForecast) {
    Scaffold(
        backgroundColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn() {
            items(weather.hourly.size) { index ->
                HourlyWeatherItem(forecast = weather.hourly[index])
            }
        }

//        Surface(border = BorderStroke(0.1.dp, secondaryFigmaTextColor)) {
//            Row {
//                val verticalScrollState = rememberScrollState()
//                WeatherParametersForADay(weather = weather, scrollState = verticalScrollState)
//                WeatherParametersForADayMeanings(
//                    weather = weather,
//                    scrollState = verticalScrollState
//                )
//            }
//        }
    }
}

@Composable
fun WeatherParametersForADay(weather: WeatherForecast, scrollState: ScrollState) {

    val isExpanded = remember {
        mutableStateOf(true)
    }

    Surface(
        modifier = Modifier
            .clickable {
                isExpanded.value = !isExpanded.value
            }
            .wrapContentWidth()
            .animateContentSize(),
        elevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val date = getDateBySeconds(weather.hourly.first().date)
            PrimaryText(text = date)
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.weather_sunny,
                    text = "Weather",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = backgroundGreenColor,
                    icon = R.drawable.ic_thermometer,
                    text = "Temperature",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.ic_gauge,
                    text = "Pressure",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = backgroundGreenColor,
                    icon = R.drawable.weather_windy,
                    text = "Wind",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.weather_cloudy,
                    text = "Cloudiness",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = backgroundGreenColor,
                    icon = R.drawable.ic_baseline_umbrella_24,
                    text = "Probability of \nprecipitation",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.ic_baseline_opacity_24,
                    text = "Humidity",
                    isExpanded = isExpanded.value
                )
            }

        }
    }
}

@Composable
fun WeatherParametersForADayMeanings(weather: WeatherForecast, scrollState: ScrollState) {

    Crossfade(weather) { weatherForecast ->
        Column {
            LazyRow(
                content = {
                    items(weather.hourly.size) { index ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val time = getTimeBySeconds(weatherForecast.hourly[index].date)
                            PrimaryText(text = time)
                            Column(modifier = Modifier.verticalScroll(scrollState)) {
                                WeatherParameterItemMeaning(
                                    color = primaryFigmaBackgroundTint,
                                    icon = getWeatherIconByName(weatherForecast.hourly[index].weather.first().icon),
                                    text = weatherForecast.hourly[index].weather.first().description
                                )
                                WeatherParameterItemMeaning(
                                    color = backgroundGreenColor,
                                    text = weatherForecast.hourly[index].temperature.toString()
                                            + stringResource(R.string.celsius),
                                )
                                WeatherParameterItemMeaning(
                                    color = primaryFigmaBackgroundTint,
                                    text = hPaToMmHg(weatherForecast.hourly[index].pressure).toString()
                                            + stringResource(R.string.pressure_units),
                                )
                                WeatherParameterItemMeaning(
                                    color = backgroundGreenColor,
                                    text = weatherForecast.hourly[index].windSpeed.toString()
                                            + stringResource(R.string.wind_speed_units),
                                    icon = R.drawable.ic_arrow_up,
                                    iconRotation = weatherForecast.hourly[index].windDeg
                                )
                                WeatherParameterItemMeaning(
                                    color = primaryFigmaBackgroundTint,
                                    text = weatherForecast.hourly[index].clouds.toString()
                                            + stringResource(R.string.percent)
                                )
                                WeatherParameterItemMeaning(
                                    color = backgroundGreenColor,
                                    text = (weatherForecast.hourly[index].probabilityOfPrecipitation * 100).toString()
                                            + stringResource(R.string.percent),
                                )
                                WeatherParameterItemMeaning(
                                    color = primaryFigmaBackgroundTint,
                                    text = weatherForecast.hourly[index].humidity.toString()
                                            + stringResource(R.string.percent)
                                )
                            }


                        }
                    }
                })
        }
    }

}
