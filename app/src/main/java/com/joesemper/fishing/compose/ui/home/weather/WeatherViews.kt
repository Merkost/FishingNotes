package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.compose.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.home.views.BigText
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SecondaryText
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.Temperature
import com.joesemper.fishing.model.entity.weather.Weather
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.time.calculateDaylightTime
import com.joesemper.fishing.utils.time.toTime
import org.koin.androidx.compose.get

@Composable
fun PrimaryWeatherItemView(
    modifier: Modifier = Modifier,
    weather: Weather,
    temperature: Float,
    textTint: Color = MaterialTheme.colors.primaryVariant,
    iconTint: Color = Color.Unspecified,
    temperatureUnit: String
) {

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (temp, icon, description) = createRefs()

        createHorizontalChain(icon, temp, chainStyle = ChainStyle.Spread)

        Icon(
            modifier = Modifier
                .size(64.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top, 8.dp)
                    absoluteRight.linkTo(temp.absoluteLeft)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
            painter = painterResource(id = getWeatherIconByName(weather.icon)),
            contentDescription = stringResource(id = R.string.weather),
            tint = iconTint
        )

        PrimaryText(
            modifier = Modifier
                .width(150.dp)
                .constrainAs(description) {
                    top.linkTo(icon.bottom, 4.dp)
                    absoluteLeft.linkTo(icon.absoluteLeft)
                    absoluteRight.linkTo(icon.absoluteRight)
                },
            text = weather.description.replaceFirstChar { it.uppercase() },
            textColor = textTint,
            textAlign = TextAlign.Center
        )

        BigText(
            modifier = Modifier
                .constrainAs(temp) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(icon.absoluteRight)
                    absoluteRight.linkTo(parent.absoluteRight)
                    bottom.linkTo(parent.bottom, 8.dp)
                },
            text = getTemperature(
                temperature,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureNameFromUnit(temperatureUnit),
            textColor = textTint
        )

    }
}

@Composable
fun WeatherPlaceSelectItem(
    modifier: Modifier = Modifier,
    selectedPlace: UserMapMarker,
    userPlaces: List<UserMapMarker>,
    onItemClick: (UserMapMarker) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isExpanded.value = !isExpanded.value
            },
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherAppBarText(
                text = selectedPlace.title ?: "Не удалось определить местоположение",
                textColor = Color.White
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, "", tint = Color.White)
            WeatherDropdownMenu(
                userPlaces = userPlaces,
                isExpanded = isExpanded,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun WeatherDropdownMenu(
    userPlaces: List<UserMapMarker>,
    isExpanded: MutableState<Boolean>,
    onItemClick: (UserMapMarker) -> Unit
) {

    DropdownMenu(
        modifier = Modifier.requiredWidthIn(250.dp, 300.dp),
        expanded = isExpanded.value,
        onDismissRequest = {
            isExpanded.value = !isExpanded.value
        }) {
        userPlaces.forEachIndexed { index, userMapMarker ->
            DropdownMenuItem(onClick = {
                onItemClick(userMapMarker)
                isExpanded.value = !isExpanded.value
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp,
                        Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(
                            id =
                            if (index == 0) {
                                R.drawable.ic_baseline_my_location_24
                            } else {
                                R.drawable.ic_baseline_location_on_24
                            }
                        ),
                        tint = MaterialTheme.colors.secondary,
                        contentDescription = "Location icon",
                        modifier = Modifier.padding(2.dp)
                    )
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = userMapMarker.title
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherAppBarText(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String
) {
    Text(
        modifier = modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        softWrap = true,
        text = text,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun WeatherLocationIcon(
    color: Color = MaterialTheme.colors.onSurface
) {
    Icon(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
        tint = color,
        contentDescription = ""
    )
}

@Composable
fun WeatherHeaderText(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.body1,
        color = color
    )
}

@Composable
fun WeatherPrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        fontSize = 20.sp
    )
}

@Composable
fun WeatherLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun DayTemperatureView(
    modifier: Modifier = Modifier,
    temperature: Temperature,
    temperatureUnit: String
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (morn, day, eve, night, mornMeaning, dayMeaning, eveMeaning, nightMeaning) = createRefs()
        createHorizontalChain(morn, day, eve, night, chainStyle = ChainStyle.Spread)
        SecondaryText(
            modifier = Modifier.constrainAs(morn) {
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(day.absoluteLeft)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(R.string.morning)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                absoluteLeft.linkTo(morn.absoluteRight)
                absoluteRight.linkTo(eve.absoluteLeft)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(R.string.day)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(eve) {
                absoluteLeft.linkTo(day.absoluteRight)
                absoluteRight.linkTo(night.absoluteLeft)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(R.string.Evening)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(night) {
                absoluteLeft.linkTo(eve.absoluteRight)
                absoluteRight.linkTo(parent.absoluteLeft, 16.dp)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(R.string.night)
        )
        PrimaryText(
            modifier = Modifier.constrainAs(mornMeaning) {
                absoluteLeft.linkTo(morn.absoluteLeft)
                absoluteRight.linkTo(morn.absoluteRight)
                top.linkTo(morn.bottom, 4.dp)
            },
            text = getTemperature(
                temperature.morning,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureNameFromUnit(temperatureUnit)
        )
        PrimaryText(
            modifier = Modifier.constrainAs(dayMeaning) {
                absoluteLeft.linkTo(day.absoluteLeft)
                absoluteRight.linkTo(day.absoluteRight)
                top.linkTo(day.bottom, 4.dp)
            },
            text = getTemperature(
                temperature.day,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureNameFromUnit(temperatureUnit),
        )
        PrimaryText(
            modifier = Modifier.constrainAs(eveMeaning) {
                absoluteLeft.linkTo(eve.absoluteLeft)
                absoluteRight.linkTo(eve.absoluteRight)
                top.linkTo(eve.bottom, 4.dp)
            },
            text = getTemperature(
                temperature.evening,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureNameFromUnit(temperatureUnit),
        )
        PrimaryText(
            modifier = Modifier.constrainAs(nightMeaning) {
                absoluteLeft.linkTo(night.absoluteLeft)
                absoluteRight.linkTo(night.absoluteRight)
                top.linkTo(night.bottom, 4.dp)
            },
            text = getTemperature(
                temperature.night,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureNameFromUnit(temperatureUnit),
        )
    }
}

@Composable
fun SunriseSunsetView(
    modifier: Modifier = Modifier,
    sunrise: Long,
    sunset: Long,
) {

    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (sunriseValue, sunsetValue, sunriseIcon, sunsetIcon, day, dayValue) = createRefs()

        createHorizontalChain(sunriseIcon, day, sunsetIcon, chainStyle = ChainStyle.Spread)

        Image(
            modifier = Modifier.constrainAs(sunriseIcon) {
                absoluteLeft.linkTo(parent.absoluteLeft, 32.dp)
                absoluteRight.linkTo(day.absoluteLeft)
                top.linkTo(parent.top, 8.dp)
            },
            painter = painterResource(id = R.drawable.ic_sunrise_morning_svgrepo_com),
            contentDescription = stringResource(
                id = R.string.sunrise_sunset
            )
        )

        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                absoluteLeft.linkTo(sunriseIcon.absoluteRight)
                absoluteRight.linkTo(sunsetIcon.absoluteLeft)
                top.linkTo(sunriseIcon.top)
                bottom.linkTo(sunriseIcon.bottom)
            },
            text = stringResource(R.string.daylight_hours)
        )

        Image(
            modifier = Modifier.constrainAs(sunsetIcon) {
                absoluteLeft.linkTo(parent.absoluteLeft, 32.dp)
                absoluteRight.linkTo(day.absoluteLeft)
                top.linkTo(parent.top, 8.dp)
            },
            painter = painterResource(id = R.drawable.ic_sunset_svgrepo_com),
            contentDescription = stringResource(
                id = R.string.sunrise_sunset
            )
        )

        PrimaryText(
            modifier = Modifier.constrainAs(sunriseValue) {
                top.linkTo(sunriseIcon.bottom, 8.dp)
                absoluteLeft.linkTo(sunriseIcon.absoluteLeft)
                absoluteRight.linkTo(sunriseIcon.absoluteRight)
            },
            text = sunrise.toTime(is12hTimeFormat)
        )

        PrimaryText(
            modifier = Modifier.constrainAs(dayValue) {
                top.linkTo(sunriseIcon.bottom, 8.dp)
                absoluteLeft.linkTo(day.absoluteLeft)
                absoluteRight.linkTo(day.absoluteRight)
            },
            text = calculateDaylightTime(
                context = LocalContext.current,
                sunrise = sunrise,
                sunset = sunset
            )
        )

        PrimaryText(
            modifier = Modifier.constrainAs(sunsetValue) {
                top.linkTo(sunsetIcon.bottom, 8.dp)
                absoluteLeft.linkTo(sunsetIcon.absoluteLeft)
                absoluteRight.linkTo(sunsetIcon.absoluteRight)
            },
            text = sunset.toTime(is12hTimeFormat)
        )

    }
}

@Composable
fun DailyWeatherValuesView(
    modifier: Modifier = Modifier,
    forecast: Daily,
    pressureUnit: PressureValues
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (
            pressIcon, pressValue, pressText, windIcon, windValue, windText, windDeg,
            humidIcon, humidValue, humidText, popIcon, popValue, popText,
        ) = createRefs()

        val guideline = createGuidelineFromAbsoluteLeft(0.5f)

        SecondaryText(
            modifier = Modifier.constrainAs(pressText) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(id = R.string.pressure)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(windText) {
                absoluteLeft.linkTo(guideline)
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(parent.top, 8.dp)
            },
            text = stringResource(id = R.string.wind)
        )

        SecondaryText(
            modifier = Modifier.constrainAs(humidText) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline)
                top.linkTo(pressIcon.bottom, 24.dp)
            },
            text = stringResource(id = R.string.humidity)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(popText) {
                absoluteLeft.linkTo(guideline)
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(humidText.top)
            },
            text = stringResource(id = R.string.precipitation)
        )

        Image(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(pressIcon) {
                    top.linkTo(pressText.bottom, 4.dp)
                    absoluteLeft.linkTo(pressText.absoluteLeft)
                    absoluteRight.linkTo(pressValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_gauge),
            contentDescription = stringResource(id = R.string.pressure),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primaryVariant)
        )
        PrimaryText(
            modifier = Modifier.constrainAs(pressValue) {
                top.linkTo(pressIcon.top)
                bottom.linkTo(pressIcon.bottom)
                absoluteLeft.linkTo(pressIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(pressText.absoluteRight)
            },
            text = pressureUnit.getPressure(
                forecast.pressure) + " " + stringResource(pressureUnit.stringRes),
        )

        Image(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(windIcon) {
                    top.linkTo(pressText.bottom, 4.dp)
                    absoluteLeft.linkTo(windText.absoluteLeft)
                    absoluteRight.linkTo(windValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_wind),
            contentDescription = stringResource(id = R.string.wind),
        )
        PrimaryText(
            modifier = Modifier.constrainAs(windValue) {
                top.linkTo(windIcon.top)
                bottom.linkTo(windIcon.bottom)
                absoluteLeft.linkTo(windIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(windDeg.absoluteLeft, 2.dp)
            },
            text = forecast.windSpeed.toInt()
                .toString() + " " + stringResource(id = R.string.wind_speed_units)
        )
        Icon(
            modifier = Modifier
                .constrainAs(windDeg) {
                    top.linkTo(windIcon.top)
                    bottom.linkTo(windIcon.bottom)
                    absoluteLeft.linkTo(windValue.absoluteRight, 4.dp)
                    absoluteRight.linkTo(windText.absoluteRight)
                }
                .rotate(forecast.windDeg.toFloat()),
            painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
            contentDescription = stringResource(id = R.string.wind),
        )

        Image(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(humidIcon) {
                    top.linkTo(humidText.bottom, 4.dp)
                    absoluteLeft.linkTo(humidText.absoluteLeft)
                    absoluteRight.linkTo(humidValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_opacity_24),
            contentDescription = stringResource(id = R.string.humidity),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primaryVariant)
        )
        PrimaryText(
            modifier = Modifier.constrainAs(humidValue) {
                top.linkTo(humidIcon.top)
                bottom.linkTo(humidIcon.bottom)
                absoluteLeft.linkTo(humidIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(humidText.absoluteRight)
            },
            text = forecast.humidity.toString() + " " + stringResource(id = R.string.percent)
        )

        Image(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(popIcon) {
                    top.linkTo(popText.bottom, 4.dp)
                    absoluteLeft.linkTo(popText.absoluteLeft)
                    absoluteRight.linkTo(popValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_umbrella_24),
            contentDescription = stringResource(id = R.string.precipitation),
        )
        PrimaryText(
            modifier = Modifier.constrainAs(popValue) {
                top.linkTo(popIcon.top)
                bottom.linkTo(popIcon.bottom)
                absoluteLeft.linkTo(popIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(popText.absoluteRight)
            },
            text = (forecast.probabilityOfPrecipitation * 100).toInt().toString()
                    + " " + stringResource(id = R.string.percent)
        )
    }
}

@Composable
fun MoonPhaseView(
    modifier: Modifier = Modifier,
    moonPhase: Float
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        SecondaryText(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = stringResource(id = R.string.moon_phase)
        )
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 4.dp),
            painter = painterResource(id = getMoonIconByPhase(moonPhase)),
            contentDescription = stringResource(id = R.string.moon_phase)
        )
        PrimaryText(
            text = (moonPhase * 100).toInt().toString()
                    + " " + stringResource(id = R.string.percent)
        )
    }
}










