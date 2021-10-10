package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
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
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.ui.theme.backgroundGreenColor
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.getDateByMilliseconds
import com.joesemper.fishing.utils.getTimeByMilliseconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForAWeek(weather: WeatherForecast) {
    Surface(border = BorderStroke(0.1.dp, secondaryFigmaTextColor)) {
        Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
            WeatherParametersForAWeek(weather = weather)
            WeatherParametersForAWeekMeanings(weather = weather)
        }
    }
}

@Composable
fun WeatherParametersForAWeek(weather: WeatherForecast) {

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
            Text(
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                text = "Date:"
            )
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
            WeatherParameterItem(
                color = backgroundGreenColor,
                icon = R.drawable.ic_moon_waning_crescent,
                text = "Moon phase",
                isExpanded = isExpanded.value
            )
            WeatherParameterItem(
                color = primaryFigmaBackgroundTint,
                icon = R.drawable.ic_weather_sunny,
                text = "Sunrise",
                isExpanded = isExpanded.value
            )
            WeatherParameterItem(
                color = backgroundGreenColor,
                icon = R.drawable.ic_weather_sunny,
                text = "Sunset",
                isExpanded = isExpanded.value
            )
        }
    }
}

@Composable
fun WeatherParametersForAWeekMeanings(weather: WeatherForecast) {
    Column {
        LazyRow(
            content = {
                items(weather.daily.size) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val date = getDateByMilliseconds(weather.daily[index].date)
                        Text(
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            text = date
                        )
                        WeatherParameterItemMeaning(
                            color = primaryFigmaBackgroundTint,
                            icon = getWeatherIconByName(weather.daily[index].weather.first().icon),
                            text = weather.daily[index].weather.first().description
                        )
                        WeatherParameterItemMeaning(
                            color = backgroundGreenColor,
                            text = weather.daily[index].temperature.day.toString() + " °C",
                        )
                        WeatherParameterItemMeaning(
                            color = primaryFigmaBackgroundTint,
                            text = hPaToMmHg(weather.daily[index].pressure).toString() + " mmHg",
                        )
                        WeatherParameterItemMeaning(
                            color = backgroundGreenColor,
                            text = weather.daily[index].windSpeed.toString() + " m/s",
                            icon = R.drawable.ic_arrow_up,
                            iconRotation = weather.daily[index].windDeg
                        )
                        WeatherParameterItemMeaning(
                            color = primaryFigmaBackgroundTint,
                            text = weather.daily[index].clouds.toString() + "%"
                        )
                        WeatherParameterItemMeaning(
                            color = backgroundGreenColor,
                            text = (weather.daily[index].probabilityOfPrecipitation * 100).toString() + "%",
                        )
                        WeatherParameterItemMeaning(
                            color = primaryFigmaBackgroundTint,
                            text = weather.daily[index].humidity.toString() + "%"
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
            })
    }
}