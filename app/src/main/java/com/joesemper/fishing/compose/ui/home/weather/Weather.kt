package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.compose.ui.home.map.LocationState
import com.joesemper.fishing.compose.ui.home.map.getCurrentLocationFlow
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.theme.primaryColor
import com.joesemper.fishing.compose.ui.theme.primaryFigmaTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySecondsTextMonth
import com.joesemper.fishing.utils.getDayOfWeekBySeconds
import com.joesemper.fishing.utils.getTimeBySeconds
import com.joesemper.fishing.utils.hPaToMmHg
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.getViewModel

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: WeatherViewModel = getViewModel()
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val userPlaces = remember {
        mutableStateOf<MutableList<UserMapMarker>>(mutableListOf())
    }

    val currentLocation = stringResource(R.string.current_location)

    val selectedPlace = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    val currentWeather = remember {
        mutableStateOf<WeatherForecast?>(null)
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        getCurrentLocationFlow(context, permissionsState).collect { locationState ->
            if (locationState is LocationState.LocationGranted) {
                userPlaces.value.add(
                    index = 0,
                    element = createCurrentPlaceItem(locationState.location, context)
                )
                selectedPlace.value = userPlaces.value.first()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.getAllMarkers().collect { places ->
            userPlaces.value.addAll(places as List<UserMapMarker>)
        }
    }

    LaunchedEffect(selectedPlace.value) {
        selectedPlace.value?.let { place ->
            viewModel.getWeather(place.latitude, place.longitude).collect {
                currentWeather.value = it
            }
        }
    }

    val scrollState = rememberLazyListState()

    val topBarColor = animateColorAsState(
        targetValue = if (scrollState.firstVisibleItemScrollOffset > 0) {
            Color.White
        } else {
            primaryColor
        },
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    val iconColor = animateColorAsState(
        targetValue = if (scrollState.firstVisibleItemScrollOffset > 0) {
            secondaryFigmaColor
        } else {
            Color.White
        },
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    val topBarElevation = animateDpAsState(
        targetValue = if (scrollState.firstVisibleItemScrollOffset > 0) {
            4.dp
        } else {
            0.dp
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                elevation = topBarElevation.value,
                backgroundColor = topBarColor.value
            ) {
                WeatherLocationIcon(color = iconColor.value)
                selectedPlace.value?.let {
                    WeatherPlaceSelectItem(
                        selectedPlace = it,
                        userPlaces = userPlaces.value,
                        scrollState = scrollState,
                        onItemClick = { clickedItem ->
                            selectedPlace.value = clickedItem
                        }
                    )
                }
            }
        }
    ) {
        LazyColumn(
            state = scrollState
        ) {
            currentWeather.value?.let { forecast ->

                item {

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        color = primaryColor
                    ) {

                        ConstraintLayout {
                            val (primary, temp, wind, pressure, humidity) = createRefs()
                            PrimaryWeatherParameterMeaning(
                                modifier = Modifier.constrainAs(primary) {
                                    top.linkTo(parent.top, 4.dp)
                                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                                    absoluteRight.linkTo(temp.absoluteLeft)
                                },
                                icon = getWeatherIconByName(forecast.hourly.first().weather.first().icon),
                                text = forecast.hourly.first().weather.first().description
                            )

                            WeatherTemperatureMeaning(
                                modifier = Modifier.constrainAs(temp) {
                                    top.linkTo(primary.top)
                                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                                    absoluteLeft.linkTo(primary.absoluteRight)
                                },
                                temperature = forecast.hourly.first().temperature.toInt()
                                    .toString(),
                                minTemperature = forecast.daily.first().temperature.min.toInt()
                                    .toString(),
                                maxTemperature = forecast.daily.first().temperature.max.toInt()
                                    .toString()
                            )
                            WeatherParameterMeaning(
                                modifier = Modifier.constrainAs(wind) {
                                    top.linkTo(primary.bottom, 4.dp)
                                    bottom.linkTo(parent.bottom)
                                    absoluteLeft.linkTo(parent.absoluteLeft, 2.dp)
                                },
                                title = stringResource(id = R.string.wind),
                                text = forecast.hourly.first().windSpeed.toString()
                                        + stringResource(R.string.wind_speed_units),
                                primaryIconId = R.drawable.weather_windy,
                                iconId = R.drawable.ic_arrow_up,
                                iconRotation = forecast.hourly.first().windDeg,
                                lightTint = true
                            )
                            WeatherParameterMeaning(
                                modifier = Modifier.constrainAs(pressure) {
                                    top.linkTo(wind.top)
                                    bottom.linkTo(wind.bottom)
                                    absoluteRight.linkTo(humidity.absoluteLeft)
                                    absoluteLeft.linkTo(wind.absoluteRight)
                                },
                                title = stringResource(id = R.string.pressure),
                                text = hPaToMmHg(forecast.hourly.first().pressure).toString() + " "
                                        + stringResource(R.string.pressure_units),
                                primaryIconId = R.drawable.ic_gauge,
                                lightTint = true
                            )
                            WeatherParameterMeaning(
                                modifier = Modifier.constrainAs(humidity) {
                                    top.linkTo(wind.top)
                                    bottom.linkTo(wind.bottom)
                                    absoluteRight.linkTo(parent.absoluteRight, 2.dp)
                                },
                                title = stringResource(id = R.string.humidity),
                                text = forecast.hourly.first().humidity.toString()
                                        + stringResource(R.string.percent),
                                primaryIconId = R.drawable.ic_baseline_opacity_24,
                                lightTint = true
                            )
                        }

                    }
                }

                item {
                    Column {
                        WeatherHeaderText(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.hourly)
                        )
                        LazyRow() {
                            items(forecast.hourly.size) { index ->
                                HourlyItem(forecast = forecast.hourly[index])
                            }
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Divider()
                    }
                }

                items(forecast.daily.size) { index ->
                    DailyItem(forecast = forecast.daily[index])
                }

            }
        }


    }
}

@Composable
fun HourlyItem(
    modifier: Modifier = Modifier,
    forecast: Hourly
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SecondaryText(text = getTimeBySeconds(forecast.date))
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = getWeatherIconByName(forecast.weather.first().icon)),
            contentDescription = ""
        )
        PrimaryText(
            text = forecast.temperature.toInt().toString() + stringResource(id = R.string.celsius)
        )
        Row() {
            PrimaryText(text = forecast.windSpeed.toInt().toString())
            Icon(
                modifier = Modifier.rotate(forecast.windDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = ""
            )
        }
        PrimaryText(text = hPaToMmHg(forecast.pressure).toString())
    }
}

@Composable
fun DailyItem(
    modifier: Modifier = Modifier,
    forecast: Daily
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val (date, day, divider, temp, tempUnits, weatherIcon, pressure, pressureUnits) = createRefs()
        val guideline = createGuidelineFromAbsoluteLeft(0.4f)
        WeatherHeaderText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = getDateBySecondsTextMonth(forecast.date)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                top.linkTo(date.bottom, 2.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = getDayOfWeekBySeconds(forecast.date)
        )
        Divider(
            modifier = Modifier.constrainAs(divider) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                bottom.linkTo(parent.bottom)
            }
        )
        SecondaryText(
            modifier = Modifier.constrainAs(pressureUnits) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 8.dp)
            },
            text = stringResource(id = R.string.pressure_units),
        )
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(pressure) {
                top.linkTo(pressureUnits.top)
                bottom.linkTo(pressureUnits.bottom)
                absoluteRight.linkTo(pressureUnits.absoluteLeft, 2.dp)
            },
            text = hPaToMmHg(forecast.pressure).toString(),
        )

        WeatherPrimaryText(
            modifier = Modifier.constrainAs(tempUnits) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(pressure.absoluteLeft, 16.dp)
            },
            text = stringResource(id = R.string.celsius),
            textColor = secondaryFigmaTextColor
        )
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(tempUnits.top)
                bottom.linkTo(tempUnits.bottom)
                absoluteRight.linkTo(tempUnits.absoluteLeft, 2.dp)
            },
            text = forecast.temperature.day.toInt().toString(),
        )
        Icon(
            modifier = Modifier
                .size(42.dp)
                .constrainAs(weatherIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(guideline)
                },
            painter = painterResource(
                id = getWeatherIconByName(forecast.weather.first().icon)
            ),
            contentDescription = "",
            tint = primaryFigmaTextColor
        )


    }
}

@Composable
fun WeatherHeaderText(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.body1,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun WeatherPrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = primaryFigmaTextColor
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        fontSize = 24.sp
    )
}



































