package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.compose.ui.theme.backgroundGreenColor
import com.joesemper.fishing.compose.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.getDateByMilliseconds
import com.joesemper.fishing.utils.getDateBySeconds
import com.joesemper.fishing.utils.getTimeByMilliseconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForAWeek(weather: WeatherForecast) {
    Surface(border = BorderStroke(0.1.dp, secondaryFigmaTextColor)) {
        Row {
            val verticalScrollState = rememberScrollState()
            WeatherParametersForAWeek(weather = weather, scrollState = verticalScrollState)
            WeatherParametersForAWeekMeanings(weather = weather, scrollState = verticalScrollState)
        }
    }
}

@Composable
fun WeatherParametersForAWeek(weather: WeatherForecast, scrollState: ScrollState) {

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
            modifier = Modifier.wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryText(text = "Date:")
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
                WeatherParameterItem(
                    color = backgroundGreenColor,
                    icon = R.drawable.ic_moon_waning_crescent,
                    text = "Moon phase",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = primaryFigmaBackgroundTint,
                    icon = R.drawable.weather_sunset_up,
                    text = "Sunrise",
                    isExpanded = isExpanded.value
                )
                WeatherParameterItem(
                    color = backgroundGreenColor,
                    icon = R.drawable.weather_sunset_down,
                    text = "Sunset",
                    isExpanded = isExpanded.value
                )
            }

        }
    }
}

@Composable
fun WeatherParametersForAWeekMeanings(weather: WeatherForecast, scrollState: ScrollState) {
    Column {
        LazyRow(
            content = {
                items(weather.daily.size) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val date = getDateBySeconds(weather.daily[index].date)
                        PrimaryText(text = date)
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                icon = getWeatherIconByName(weather.daily[index].weather.first().icon),
                                text = weather.daily[index].weather.first().description
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = weather.daily[index].temperature.day.toString()
                                        + stringResource(R.string.celsius),
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = hPaToMmHg(weather.daily[index].pressure).toString()
                                        + stringResource(R.string.pressure_units),
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = weather.daily[index].windSpeed.toString()
                                        + stringResource(R.string.wind_speed_units),
                                icon = R.drawable.ic_arrow_up,
                                iconRotation = weather.daily[index].windDeg
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = weather.daily[index].clouds.toString()
                                        + stringResource(R.string.percent)
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = (weather.daily[index].probabilityOfPrecipitation * 100).toString()
                                        + stringResource(R.string.percent),
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = weather.daily[index].humidity.toString()
                                        + stringResource(R.string.percent)
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                icon = getMoonIconByPhase(weather.daily[index].moonPhase),
                            )
                            WeatherParameterItemMeaning(
                                color = primaryFigmaBackgroundTint,
                                text = getTimeByMilliseconds(weather.daily[index].sunrise)
                            )
                            WeatherParameterItemMeaning(
                                color = backgroundGreenColor,
                                text = getTimeByMilliseconds(weather.daily[index].sunset)
                            )
                        }

                    }
                }
            })
    }
}