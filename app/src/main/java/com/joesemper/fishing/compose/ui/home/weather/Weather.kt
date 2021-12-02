package com.joesemper.fishing.compose.ui.home.weather

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.DefaultButtonOutlined
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.compose.ui.home.map.LocationState
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.getCurrentLocationFlow
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.theme.primaryDarkColor
import com.joesemper.fishing.compose.ui.theme.primaryWhiteColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryWhiteColor
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.getDateBySecondsTextMonth
import com.joesemper.fishing.utils.getDayOfWeekAndDate
import com.joesemper.fishing.utils.getDayOfWeekBySeconds
import com.joesemper.fishing.utils.getTimeBySeconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import kotlin.math.min

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@Composable
fun Weather(
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: WeatherViewModel = getViewModel()
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val selectedPlace = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        getCurrentLocationFlow(context, permissionsState).collect { locationState ->
            if (locationState is LocationState.LocationGranted) {
                viewModel.markersList.value.add(
                    index = 0,
                    element = createCurrentPlaceItem(locationState.location, context)
                )
                selectedPlace.value = viewModel.markersList.value.first()
            }
        }
    }

    LaunchedEffect(selectedPlace.value) {
        selectedPlace.value?.let { place ->
            viewModel.getWeather(place.latitude, place.longitude)
        }
    }

    val scrollState = rememberScrollState()
    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C.name)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val elevation =
                animateDpAsState(targetValue = if (scrollState.value > 0) 4.dp else 0.dp)
            if (checkPermission(context) && viewModel.markersList.value.isNotEmpty()) {
                selectedPlace.value = viewModel.markersList.value.first()
            }

            TopAppBar(
                elevation = elevation.value,
                backgroundColor = primaryDarkColor
            ) {
                WeatherLocationIcon(color = Color.White)
                selectedPlace.value?.let {
                    WeatherPlaceSelectItem(
                        selectedPlace = it,
                        userPlaces = viewModel.markersList.value,
                        onItemClick = { clickedItem ->
                            selectedPlace.value = clickedItem
                        }
                    )
                }
            }
        }
    ) {
        AnimatedVisibility(viewModel.currentWeather.value != null) {
            val forecast = viewModel.currentWeather.value!!
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {

                CurrentWeather(
                    forecast = forecast,
                    pressureUnit = pressureUnit,
                    temperatureUnit = temperatureUnit
                )

                PressureChartItem(
                    forecast = forecast.daily,
                    pressureUnit = pressureUnit
                )

                forecast.daily.forEach {
                    DailyWeatherItem(forecast = it, temperatureUnit = temperatureUnit)
                }
            }
        }
    }

    AnimatedVisibility(viewModel.currentWeather.value == null) {
        Column(
            modifier = Modifier
                .systemBarsPadding(false)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (checkPermission(context) && viewModel.markersList.value.isEmpty()) {
                SecondaryText(text = "No places yet. \nAdd new place now!")
                WeatherEmptyView(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.CenterHorizontally)
                )
                DefaultButtonOutlined(text = "Add", onClick = {
                    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${true}")
                })
                //TODO: No places yet view
            } else {
                /*WeatherLoading(
                    modifier = Modifier
                        .size(500.dp)
                        .align(Alignment.CenterHorizontally)
                )*/
                WeatherEmptyView(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.CenterHorizontally)
                )
                // Spacer(modifier = Modifier.size())
            }
        }
    }
}

@Composable
fun NoPlacesView() {
    TODO("Not yet implemented")
}

@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
    forecast: WeatherForecast,
    temperatureUnit: String,
    pressureUnit: String,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        color = primaryDarkColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            PrimaryWeatherItem(
                forecast = forecast.hourly.first(),
                temperatureUnit = temperatureUnit
            )

            HourlyWeather(
                forecastHourly = forecast.hourly,
                forecastDaily = forecast.daily.first(),
                temperatureUnit = temperatureUnit,
                pressureUnit = pressureUnit
            )
        }
    }
}

@Composable
fun HourlyWeather(
    modifier: Modifier = Modifier,
    forecastHourly: List<Hourly>,
    forecastDaily: Daily,
    temperatureUnit: String,
    pressureUnit: String
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CurrentWeatherItem(
                modifier = Modifier.padding(end = 16.dp),
                forecastHourly = forecastHourly.first(),
                forecastDaily = forecastDaily,
                pressureUnit = pressureUnit
            )
        }
        items(forecastHourly.size) { index ->
            HourlyWeatherItem(
                forecast = forecastHourly[index],
                timeTitle = if (index == 0) {
                    stringResource(R.string.now)
                } else {
                    getTimeBySeconds(forecastHourly[index].date)
                },
                temperatureUnit = temperatureUnit
            )
        }
    }
}

@Composable
fun HourlyWeatherItem(
    modifier: Modifier = Modifier,
    timeTitle: String,
    forecast: Hourly,
    temperatureUnit: String
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SecondaryText(
            text = timeTitle,
            textColor = secondaryWhiteColor
        )
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = getWeatherIconByName(forecast.weather.first().icon)),
            contentDescription = "",
            colorFilter = ColorFilter.tint(color = primaryWhiteColor)
        )
        PrimaryText(
            text = getTemperature(
                forecast.temperature,
                TemperatureValues.valueOf(temperatureUnit)
            ) +
                    getTemperatureFromUnit(temperatureUnit),
            textColor = primaryWhiteColor
        )
        Row() {
            PrimaryText(
                text = forecast.windSpeed.toInt().toString(),
                textColor = primaryWhiteColor
            )
            Icon(
                modifier = Modifier.rotate(forecast.windDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                contentDescription = "",
                tint = primaryWhiteColor
            )
        }
    }
}

@Composable
fun CurrentWeatherItem(
    modifier: Modifier = Modifier,
    forecastHourly: Hourly,
    forecastDaily: Daily,
    pressureUnit: String
) {
    ConstraintLayout(
        modifier = modifier
            .wrapContentSize()
    ) {
        val (popIcon, popMeaning, pressIcon, pressMeaning, humidityIcon, humidityMeaning) = createRefs()

        Icon(
            modifier = Modifier.constrainAs(popIcon) {
                top.linkTo(parent.top, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            painter = painterResource(id = R.drawable.ic_baseline_umbrella_24),
            contentDescription = "",
            tint = secondaryWhiteColor
        )
        PrimaryText(
            modifier = Modifier.constrainAs(popMeaning) {
                top.linkTo(popIcon.top)
                bottom.linkTo(popIcon.bottom)
                absoluteLeft.linkTo(popIcon.absoluteRight, 4.dp)
            },
            text = (forecastDaily.probabilityOfPrecipitation * 100).toInt().toString()
                    + stringResource(id = R.string.percent),
            textColor = primaryWhiteColor
        )
        Icon(
            modifier = Modifier.constrainAs(pressIcon) {
                top.linkTo(popIcon.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            painter = painterResource(id = R.drawable.ic_gauge),
            contentDescription = "",
            tint = secondaryWhiteColor
        )
        PrimaryText(
            modifier = Modifier.constrainAs(pressMeaning) {
                top.linkTo(pressIcon.top)
                bottom.linkTo(pressIcon.bottom)
                absoluteLeft.linkTo(pressIcon.absoluteRight, 4.dp)
            },
            text = getPressure(forecastHourly.pressure, PressureValues.valueOf(pressureUnit)),
            textColor = primaryWhiteColor
        )
        Icon(
            modifier = Modifier.constrainAs(humidityIcon) {
                top.linkTo(pressIcon.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            painter = painterResource(id = R.drawable.ic_baseline_opacity_24),
            contentDescription = "",
            tint = secondaryWhiteColor
        )
        PrimaryText(
            modifier = Modifier.constrainAs(humidityMeaning) {
                top.linkTo(humidityIcon.top)
                bottom.linkTo(humidityIcon.bottom)
                absoluteLeft.linkTo(humidityIcon.absoluteRight, 4.dp)
            },
            text = forecastHourly.humidity.toString() + stringResource(id = R.string.percent),
            textColor = primaryWhiteColor
        )
    }
}

@Composable
fun DailyWeatherItem(
    modifier: Modifier = Modifier,
    forecast: Daily,
    temperatureUnit: String
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val (date, day, divider, temp, tempUnits, weatherIcon, pop, popIcon) = createRefs()
        val guideline = createGuidelineFromAbsoluteLeft(0.6f)
        WeatherHeaderText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top, 8.dp)
                bottom.linkTo(day.top)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = getDateBySecondsTextMonth(forecast.date)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                top.linkTo(date.bottom)
                bottom.linkTo(parent.bottom, 8.dp)
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
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(tempUnits) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            text = getTemperatureFromUnit(temperatureUnit),
            textColor = secondaryTextColor
        )
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(tempUnits.top)
                bottom.linkTo(tempUnits.bottom)
                absoluteRight.linkTo(tempUnits.absoluteLeft, 2.dp)
            },
            text = getTemperature(
                forecast.temperature.day,
                TemperatureValues.valueOf(temperatureUnit)
            ),
        )
        Image(
            modifier = Modifier
                .size(42.dp)
                .constrainAs(weatherIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteRight.linkTo(guideline, 48.dp)
                },
            painter = painterResource(
                id = getWeatherIconByName(forecast.weather.first().icon)
            ),
            contentDescription = "",
        )

        Image(
            modifier = Modifier
                .size(32.dp)
                .constrainAs(popIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(guideline, 8.dp)
                },
            painter = painterResource(
                id = R.drawable.ic_baseline_umbrella_24
            ),
            contentDescription = "",
        )

        SecondaryText(
            modifier = Modifier.constrainAs(pop) {
                top.linkTo(popIcon.top)
                bottom.linkTo(popIcon.bottom)
                absoluteLeft.linkTo(popIcon.absoluteRight, 4.dp)
            },
            text = (forecast.probabilityOfPrecipitation * 100).toInt()
                .toString() + stringResource(id = R.string.percent)
        )

    }
}

@Composable
fun PressureChartItem(modifier: Modifier = Modifier, forecast: List<Daily>, pressureUnit: String) {
    Column(
        modifier = modifier
    ) {
        WeatherHeaderText(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.pressure) + ", " + pressureUnit
        )
        PressureChart(
            Modifier
                .horizontalScroll(rememberScrollState())
                .width(500.dp)
                .height(120.dp)
                .padding(top = 16.dp),
            weather = forecast,
            pressureUnit = pressureUnit
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Divider()
    }
}

@Composable
fun PressureChart(
    modifier: Modifier = Modifier,
    weather: List<Daily>,
    pressureUnit: String,
) {
    val x = remember { Animatable(0f) }
    val yValues = remember(weather) { mutableStateOf(getPressureList(weather, pressureUnit)) }
    val xTarget = (yValues.value.size - 1).toFloat()
    LaunchedEffect(weather) {
        x.animateTo(
            targetValue = xTarget,
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
        )
    }

    Canvas(modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 18.dp, top = 32.dp)) {
        val xbounds = Pair(0f, xTarget)
        val ybounds = getBounds(yValues.value)
        val scaleX = size.width / (xbounds.second - xbounds.first)
        val scaleY = size.height / (ybounds.second - ybounds.first)
        val yMove = ybounds.first * scaleY

        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 36f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = 0xDE000000.toInt()

        val linesList = mutableListOf<Point>()

        (0..min(yValues.value.size - 1, x.value.toInt())).forEach { index ->
            val pointX = index * scaleX
            val pointY = size.height - (yValues.value[index] * scaleY) + yMove - 52f

            drawCircle(
                color = primaryDarkColor,
                center = Offset(x = pointX, y = pointY),
                radius = 12f
            )

            drawContext.canvas.nativeCanvas.drawText(
                getPressure(
                    weather[index].pressure,
                    PressureValues.valueOf(pressureUnit)
                )/*hPaToMmHg(weather[index].pressure)*/,
                pointX, pointY - 48f, paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                getDayOfWeekAndDate(weather[index].date),
                pointX, size.height, paint
            )

            linesList.add(Point(pointX, pointY))
        }

        linesList.forEachIndexed { index, value ->
            if (index > 0) {
                drawLine(
                    start = Offset(x = linesList[index - 1].x, linesList[index - 1].y),
                    end = Offset(x = value.x, y = value.y),
                    color = primaryDarkColor,
                    strokeWidth = 5F
                )
            }
        }
    }
}
