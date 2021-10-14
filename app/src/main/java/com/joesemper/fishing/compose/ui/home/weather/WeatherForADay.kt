package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.ui.theme.backgroundGreenColor
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.getDateByMilliseconds
import com.joesemper.fishing.utils.getTimeByMilliseconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForADay(weather: WeatherForecast) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Surface(border = BorderStroke(0.1.dp, secondaryFigmaTextColor)) {
            Row {
                val verticalScrollState = rememberScrollState()
                WeatherParametersForADay(weather = weather, scrollState = verticalScrollState)
                WeatherParametersForADayMeanings(
                    weather = weather,
                    scrollState = verticalScrollState
                )
            }
        }
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
            val date = getDateByMilliseconds(weather.hourly.first().date)
            Text(
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                text = date
            )
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.ic_clear_sky,
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
                    icon = R.drawable.ic_wind,
                    text = "Wind",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.ic_baseline_cloud_24,
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

    Column {
        LazyRow(
            content = {
                items(weather.hourly.size) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val time = getTimeByMilliseconds(weather.hourly[index].date)
                        Text(
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            text = time
                        )
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                icon = getWeatherIconByName(weather.hourly[index].weather.first().icon),
                                text = weather.hourly[index].weather.first().description
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = weather.hourly[index].temperature.toString() + "°C",
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = hPaToMmHg(weather.hourly[index].pressure).toString() + " mmHg",
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = weather.hourly[index].windSpeed.toString() + "m/s",
                                icon = R.drawable.ic_arrow_up,
                                iconRotation = weather.hourly[index].windDeg
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = weather.hourly[index].clouds.toString() + "%"
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = (weather.hourly[index].probabilityOfPrecipitation * 100).toString() + "%",
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = weather.hourly[index].humidity.toString() + "%"
                            )
                        }


                    }
                }
            })
    }
}
