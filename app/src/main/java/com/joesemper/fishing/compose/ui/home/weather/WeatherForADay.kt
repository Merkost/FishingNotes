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
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySecondsTextMonth
import com.joesemper.fishing.utils.getTimeBySeconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherForADay(weather: WeatherForecast) {

    LazyColumn() {
        items(weather.hourly.size) { index ->
            HourlyWeatherItem(forecast = weather.hourly[index])
        }
    }
}

@Composable
fun HourlyWeatherItem(forecast: Hourly) {
    DefaultCard(modifier = Modifier
        .clickable {

        }) {
        ConstraintLayout(
            modifier = Modifier.padding(8.dp)
        ) {
            val (time, date, divider, weather, temp, wind, pressure, precipitation) = createRefs()
            PrimaryText(
                modifier = Modifier.constrainAs(time) {
                    top.linkTo(parent.top, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                },
                text = getTimeBySeconds(forecast.date)
            )
            SecondaryText(
                modifier = Modifier.constrainAs(date) {
                    top.linkTo(parent.top, 4.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                text = getDateBySecondsTextMonth(forecast.date)
            )
            Divider(
                modifier = Modifier.constrainAs(divider) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(time.bottom)
                })
            PrimaryWeatherParameterMeaning(
                modifier = Modifier.constrainAs(weather) {
                    top.linkTo(divider.bottom, 4.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                icon = getWeatherIconByName(forecast.weather.first().icon),
                text = forecast.weather.first().description
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(temp) {
                    top.linkTo(divider.bottom, 4.dp)
                    absoluteLeft.linkTo(weather.absoluteRight)
                },
                title = stringResource(id = R.string.temperature),
                text = forecast.temperature.toInt()
                    .toString() + stringResource(id = R.string.celsius),
                primaryIconId = R.drawable.ic_thermometer,
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(wind) {
                    top.linkTo(temp.bottom)
                    absoluteLeft.linkTo(weather.absoluteRight)
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
                    top.linkTo(divider.bottom, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight)
                    absoluteLeft.linkTo(temp.absoluteRight)
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
        }
        }
    }
