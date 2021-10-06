package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.ui.theme.backgroundRedColor
import com.joesemper.fishing.ui.theme.backgroundWhiteColor
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.getDateByMilliseconds
import com.joesemper.fishing.utils.getTimeByMilliseconds

@Composable
fun WeatherForADay(weather: WeatherForecast) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Surface(border = BorderStroke(0.1.dp, secondaryFigmaTextColor)) {
            Row(modifier = Modifier.verticalScroll(rememberScrollState())) {

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
                        WeatherText(text = date)
                        WeatherParameterItem(
                            color = primaryFigmaBackgroundTint,
                            icon = R.drawable.ic_clear_sky,
                            text = "Weather",
                            isExpanded = isExpanded.value
                        )
                        WeatherParameterItem(
                            color = backgroundRedColor,
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
                            color = backgroundRedColor,
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
                            color = backgroundRedColor,
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

                Column {
                    LazyRow(
                        content = {
                            items(24) { index ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val time = getTimeByMilliseconds(weather.hourly[index].date)
                                    WeatherText(text = time)
                                    WeatherParameterItemMeaning(
                                        color = primaryFigmaBackgroundTint,
                                        icon = getWeatherIconByName(weather.hourly[index].weather.first().icon),
                                        text = weather.hourly[index].weather.first().description
                                    )
                                    WeatherParameterItemMeaning(
                                        color = backgroundRedColor,
                                        text = weather.hourly[index].temperature.toString() + "°C",
                                    )
                                    WeatherParameterItemMeaning(
                                        color = primaryFigmaBackgroundTint,
                                        text = weather.hourly[index].pressure.toString() + "hPa",
                                    )
                                    WeatherParameterItemMeaning(
                                        color = backgroundRedColor,
                                        text = weather.hourly[index].windSpeed.toString() + "m/s",
                                        icon = R.drawable.ic_arrow_up,
                                        rotation = weather.hourly[index].windDeg
                                    )
                                    WeatherParameterItemMeaning(
                                        color = primaryFigmaBackgroundTint,
                                        text = weather.hourly[index].clouds.toString() + "%"
                                    )
                                    WeatherParameterItemMeaning(
                                        color = backgroundRedColor,
                                        text = weather.hourly[index].probabilityOfPrecipitation.toString() + "%",
                                    )
                                    WeatherParameterItemMeaning(
                                        color = primaryFigmaBackgroundTint,
                                        text = weather.hourly[index].humidity.toString() + "%"
                                    )
                                }
                            }
                        })
                }
            }
        }

    }
}

@Composable
fun WeatherParameterItem(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int,
    text: String,
    isExpanded: Boolean
) {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .width(100.dp),
        color = color,
        border = BorderStroke(0.1.dp, secondaryFigmaTextColor),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                tint = secondaryFigmaTextColor,
                painter = painterResource(id = icon),
                contentDescription = ""
            )
            if (isExpanded) {
                WeatherText(text = text)
            }

        }
    }
}


@Composable
fun WeatherParameterItemMeaning(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int? = null,
    text: String,
    rotation: Int = 0,
) {
    Surface(
        color = color,
        border = BorderStroke(0.5.dp, secondaryFigmaTextColor),
        modifier = modifier.size(100.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(rotation.toFloat()),
                    tint = secondaryFigmaTextColor,
                    painter = painterResource(id = icon),
                    contentDescription = ""
                )
            }
            WeatherText(text = text)
        }
    }
}

@Composable
fun WeatherText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(vertical = 4.dp),
        text = text,
        style = MaterialTheme.typography.subtitle1
    )
}
