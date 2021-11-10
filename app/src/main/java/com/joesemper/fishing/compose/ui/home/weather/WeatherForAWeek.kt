package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.DefaultCard
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySecondsTextMonth
import com.joesemper.fishing.utils.getTimeBySeconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForAWeek(weather: WeatherForecast) {

    LazyColumn() {
        items(weather.daily.size) { index ->
            DailyWeatherItem(forecast = weather.daily[index])
        }
    }
}

@Composable
fun DailyWeatherItem(forecast: Daily) {
    DefaultCard(modifier = Modifier
        .clickable {

        }) {
        ConstraintLayout(
            modifier = Modifier.padding(8.dp)
        ) {
            val (date, divider, weather, temp, wind, pressure, precipitation, moon, humidity, sunrise) = createRefs()
            PrimaryText(
                modifier = Modifier.constrainAs(date) {
                    top.linkTo(parent.top, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                text = getDateBySecondsTextMonth(forecast.date)
            )
            Divider(
                modifier = Modifier.constrainAs(divider) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(date.bottom)
                })
            WeatherTemperatureMeaning(
                modifier = Modifier.constrainAs(temp) {
                    top.linkTo(divider.bottom, 4.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                temperature = forecast.temperature.day.toInt().toString(),
                maxTemperature = forecast.temperature.max.toInt()
                    .toString() + stringResource(id = R.string.celsius),
                minTemperature = forecast.temperature.min.toInt()
                    .toString() + stringResource(id = R.string.celsius),
            )
            PrimaryWeatherParameterMeaning(
                modifier = Modifier.constrainAs(weather) {
                    top.linkTo(temp.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                icon = getWeatherIconByName(forecast.weather.first().icon),
                text = forecast.weather.first().description
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(wind) {
                    top.linkTo(temp.top)
                    absoluteLeft.linkTo(temp.absoluteRight, 2.dp)
                },
                title = stringResource(id = R.string.wind),
                text = forecast.windSpeed.toString()
                        + stringResource(R.string.wind_speed_units),
                primaryIconId = R.drawable.weather_windy,
                iconId = R.drawable.ic_arrow_up,
                iconRotation = forecast.windDeg
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(pressure) {
                    top.linkTo(wind.bottom)
                    absoluteLeft.linkTo(wind.absoluteLeft)
                },
                title = stringResource(id = R.string.pressure),
                text = hPaToMmHg(forecast.pressure).toString()
                        + stringResource(R.string.pressure_units),
                primaryIconId = R.drawable.ic_gauge
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(precipitation) {
                    top.linkTo(pressure.bottom)
                    absoluteLeft.linkTo(pressure.absoluteLeft)
                },
                title = stringResource(R.string.precipitation),
                text = (forecast.probabilityOfPrecipitation * 100).toString()
                        + stringResource(R.string.percent),
                primaryIconId = R.drawable.ic_baseline_umbrella_24
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(moon) {
                    top.linkTo(wind.top)
                    absoluteLeft.linkTo(wind.absoluteRight)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                title = stringResource(R.string.moon_phase),
                text = (forecast.moonPhase * 100).toInt()
                    .toString() + stringResource(id = R.string.percent),
                primaryIconId = getMoonIconByPhase(forecast.moonPhase)
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(humidity) {
                    top.linkTo(moon.bottom)
                    absoluteLeft.linkTo(moon.absoluteLeft)
                    absoluteRight.linkTo(moon.absoluteRight)
                },
                title = stringResource(R.string.humidity),
                text = forecast.humidity.toString() + stringResource(id = R.string.percent),
                primaryIconId = R.drawable.ic_baseline_opacity_24
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(sunrise) {
                    top.linkTo(humidity.bottom)
                    absoluteLeft.linkTo(moon.absoluteLeft)
                    absoluteRight.linkTo(moon.absoluteRight)
                },
                title = stringResource(R.string.sunrise_sunset),
                text = getTimeBySeconds(forecast.sunrise) + "/" + getTimeBySeconds(forecast.sunset),
                primaryIconId = R.drawable.weather_sunset_up
            )
        }
    }
}