package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.DefaultCard
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.compose.ui.home.SecondaryTextSmall
import com.joesemper.fishing.compose.ui.theme.backgroundWhiteColor
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySecondsTextMonth
import com.joesemper.fishing.utils.getTimeBySeconds
import com.joesemper.fishing.utils.hPaToMmHg

@Composable
fun WeatherParameterItem(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int,
    text: String,
    isExpanded: Boolean
) {
    Surface(
        modifier = if (isExpanded) {
            Modifier
                .height(100.dp)
                .width(100.dp)
        } else {
            Modifier
                .height(100.dp)
                .wrapContentWidth()
        },


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
    text: String? = null,
    iconRotation: Int = 0,
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
                        .rotate(iconRotation.toFloat()),
                    tint = secondaryFigmaTextColor,
                    painter = painterResource(id = icon),
                    contentDescription = ""
                )
            }
            text?.let {
                WeatherText(text = text)
            }

        }
    }
}

@Composable
fun WeatherText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(vertical = 4.dp),
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.subtitle1
    )
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

@Composable
fun PrimaryWeatherParameterMeaning(modifier: Modifier = Modifier, icon: Int, text: String) {
    Column(
        modifier = modifier
            .height(100.dp)
            .width(100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.weather)
        )
        SecondaryTextSmall(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherParameterMeaning(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    primaryIconId: Int,
    iconId: Int? = null,
    iconRotation: Int = 0,
) {
    ConstraintLayout(
        modifier = modifier
            .height(50.dp)
            .width(150.dp),
    ) {
        val (icon, header, meaning, unit) = createRefs()
        Icon(
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
            painter = painterResource(id = primaryIconId),
            contentDescription = stringResource(id = R.string.temperature),
            tint = secondaryFigmaTextColor,

            )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(header) {
                    top.linkTo(icon.top)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
            text = title
        )
        PrimaryText(
            modifier = Modifier
                .constrainAs(meaning) {
                    absoluteLeft.linkTo(header.absoluteLeft)
                    bottom.linkTo(icon.bottom)
                },
            text = text
        )
        iconId?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation.toFloat())
                    .constrainAs(unit) {
                        top.linkTo(meaning.top)
                        bottom.linkTo(meaning.bottom)
                        absoluteLeft.linkTo(meaning.absoluteRight, 4.dp)
                    },
                painter = painterResource(id = it),
                contentDescription = stringResource(id = R.string.temperature),
                tint = secondaryFigmaTextColor
            )
        }

    }
}

@Composable
fun WeatherWindMeaning(
    modifier: Modifier = Modifier,
    text: String,
    iconId: Int,
    iconRotation: Int = 0,
) {
    Row(
        modifier = modifier
            .height(50.dp)
            .width(150.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = modifier
                .size(32.dp)
                .padding(4.dp)
                .rotate(iconRotation.toFloat()),
            painter = painterResource(id = iconId),
            contentDescription = stringResource(id = R.string.temperature),
            tint = secondaryFigmaTextColor
        )
        PrimaryText(
            text = text
        )
    }
}
























