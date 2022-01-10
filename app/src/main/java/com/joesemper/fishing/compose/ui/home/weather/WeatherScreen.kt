package com.joesemper.fishing.compose.ui.home.weather

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.model.datastore.UserPreferences
import com.joesemper.fishing.model.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.map.LocationState
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.getCurrentLocationFlow
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.home.views.DefaultButtonOutlined
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SecondaryText
import com.joesemper.fishing.compose.ui.home.views.SupportText
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.theme.primaryWhiteColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.domain.viewstates.ErrorType
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.joesemper.fishing.utils.time.toDayOfWeek
import com.joesemper.fishing.utils.time.toDayOfWeekAndDate
import com.joesemper.fishing.utils.time.toTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import kotlin.math.min

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    place: UserMapMarker? = null,
    upPress: () -> Unit,
) {
    val viewModel: WeatherViewModel = getViewModel()
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val selectedPlace = remember {
        mutableStateOf<UserMapMarker?>(place)
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        checkPermission(context)
        if (permissionsState.allPermissionsGranted) {
            getCurrentLocationFlow(context, permissionsState).collect { locationState ->
                if (locationState is LocationState.LocationGranted) {

                    //TODO: check if currentPlaceItem already exists!!

                    viewModel.markersList.value.add(
                        index = 0,
                        element = createCurrentPlaceItem(locationState.location, context)
                    )
                    if (selectedPlace.value == null) {
                        selectedPlace.value = viewModel.markersList.value.first()
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedPlace.value) {
        selectedPlace.value?.let { place ->
            viewModel.getWeather(place.latitude, place.longitude)
        }
    }

    val scrollState = rememberScrollState()

    val weatherState by viewModel.weatherState.collectAsState()

    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

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
                backgroundColor = MaterialTheme.colors.primary
            ) {
                WeatherLocationIconButton(color = MaterialTheme.colors.onPrimary) {
                    selectedPlace.value?.let {
                        navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                            Arguments.PLACE to it)
                    }
                }
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

        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(weatherState is RetrofitWrapper.Success/*viewModel.currentWeather.value != null*/) {
                val forecast = viewModel.currentWeather.value!!
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {

                    CurrentWeather(
                        forecast = forecast,
                        pressureUnit = pressureUnit,
                        temperatureUnit = temperatureUnit,
                        windSpeedUnit = windSpeedUnit,
                    )

                    PressureChartItem(
                        forecast = forecast.daily,
                        pressureUnit = pressureUnit,
                    )

                    forecast.daily.forEachIndexed { index, daily ->
                        DailyWeatherItem(
                            forecast = daily,
                            temperatureUnit = temperatureUnit,
                            onDailyWeatherClick = {
                                navigateToDailyWeatherScreen(
                                    navController = navController,
                                    index = index,
                                    forecastDaily = forecast.daily
                                )
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(weatherState is RetrofitWrapper.Loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (checkPermission(context) && viewModel.markersList.value.isEmpty()) {
                        SecondaryText(text = "No places yet. \nAdd new place now!")
                        WeatherLoading(
                            modifier = Modifier
                                .size(250.dp)
                            /*.align(Alignment.CenterHorizontally)*/
                        )
                        DefaultButtonOutlined(text = "Add", onClick = {
                            navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${true}")
                        })
                    } else {
                        WeatherLoading(
                            modifier = Modifier
                                .size(300.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(weatherState is RetrofitWrapper.Error) {
                if (weatherState is RetrofitWrapper.Error) {
                    val errorType = (weatherState as RetrofitWrapper.Error).errorType
                    when (errorType) {
                        is ErrorType.NetworkError -> {
                            NoInternetView(Modifier.fillMaxWidth())
                        }
                        is ErrorType.OtherError -> {
                            //TODO: OtherErrorView()
                            NoInternetView(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

    }


}

@Composable
fun NoInternetView(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = modifier
        )
        SupportText(text = "Can't connect to the server!")
    }

}

@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
    forecast: WeatherForecast,
    temperatureUnit: TemperatureValues,
    pressureUnit: PressureValues,
    windSpeedUnit: WindSpeedValues,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp),
        color = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            PrimaryWeatherItemView(
                temperature = forecast.hourly.first().temperature,
                weather = forecast.hourly.first().weather.first(),
                textTint = primaryWhiteColor,
                iconTint = primaryWhiteColor,
                temperatureUnit = temperatureUnit
            )

            CurrentWeatherValuesView(
                forecast = forecast.hourly.first(),
                pressureUnit = pressureUnit,
            )

            HourlyWeather(
                forecastHourly = forecast.hourly,
                temperatureUnit = temperatureUnit,
                windSpeedUnit = windSpeedUnit,
            )
        }
    }
}

@Composable
fun HourlyWeather(
    modifier: Modifier = Modifier,
    forecastHourly: List<Hourly>,
    temperatureUnit: TemperatureValues,
    windSpeedUnit: WindSpeedValues,
) {
    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forecastHourly.size) { index ->
            HourlyWeatherItem(
                forecast = forecastHourly[index],
                timeTitle = if (index == 0) {
                    stringResource(R.string.now)
                } else {
                    forecastHourly[index].date.toTime(is12hTimeFormat)
                },
                temperatureUnit = temperatureUnit,
                windSpeedUnit = windSpeedUnit,
            )
        }
    }
}

@Composable
fun HourlyWeatherItem(
    modifier: Modifier = Modifier,
    timeTitle: String,
    forecast: Hourly,
    temperatureUnit: TemperatureValues,
    windSpeedUnit: WindSpeedValues
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecondaryText(
            text = timeTitle,
            textColor = MaterialTheme.colors.onPrimary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = getWeatherIconByName(forecast.weather.first().icon)),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onPrimary)
            )
            PrimaryText(
                text = temperatureUnit.getTemperature(
                    forecast.temperature) + stringResource(temperatureUnit.stringRes),
                textColor = MaterialTheme.colors.onPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryText(
                text = windSpeedUnit.getWindSpeedInt(forecast.windSpeed.toDouble())
                        + " " + stringResource(windSpeedUnit.stringRes),
                textColor = MaterialTheme.colors.onPrimary
            )
            Icon(
                modifier = Modifier
                    .rotate(forecast.windDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                contentDescription = "",
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }
}

@Composable
fun DailyWeatherItem(
    modifier: Modifier = Modifier,
    forecast: Daily,
    temperatureUnit: TemperatureValues,
    onDailyWeatherClick: () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onDailyWeatherClick() }
    ) {
        val (date, day, divider, temp, tempUnits, weatherIcon, pop, popIcon) = createRefs()
        val guideline = createGuidelineFromAbsoluteLeft(0.6f)
        WeatherHeaderText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top, 8.dp)
                bottom.linkTo(day.top)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = forecast.date.toDateTextMonth()
        )
        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                top.linkTo(date.bottom)
                bottom.linkTo(parent.bottom, 8.dp)
            },
            text = forecast.date.toDayOfWeek()
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
            text = stringResource(temperatureUnit.stringRes),
            textColor = secondaryTextColor
        )
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(tempUnits.top)
                bottom.linkTo(tempUnits.bottom)
                absoluteRight.linkTo(tempUnits.absoluteLeft, 2.dp)
            },
            text = temperatureUnit.getTemperature(forecast.temperature.day),
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
        if (forecast.probabilityOfPrecipitation >= 0.2f) {
            Image(
                modifier = Modifier
                    .size(24.dp)
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
}

@Composable
fun PressureChartItem(
    modifier: Modifier = Modifier,
    forecast: List<Daily>,
    pressureUnit: PressureValues,
) {
    Column(
        modifier = modifier
    ) {
        WeatherHeaderText(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.pressure) + ", " + stringResource(pressureUnit.stringRes)
        )
        PressureChart(
            Modifier
                .horizontalScroll(rememberScrollState())
                .width(500.dp)
                .height(120.dp)
                .padding(top = 16.dp),
            weather = forecast,
            pressureUnit = pressureUnit,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Divider()
    }
}

@Composable
fun PressureChart(
    modifier: Modifier = Modifier,
    weather: List<Daily>,
    pressureUnit: PressureValues
) {
    val x = remember { Animatable(0f) }
    val yValues = remember(weather) { mutableStateOf(getPressureList(weather, pressureUnit)) }
    val xTarget = (yValues.value.size - 1).toFloat()
    LaunchedEffect(weather) {
        x.animateTo(
            targetValue = xTarget,
            animationSpec = tween(
                durationMillis = 100,
                easing = CubicBezierEasing(0f, 0f, 0f, 1f)
            ),
        )

    }

    val color = MaterialTheme.colors.primaryVariant


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
                color = color,
                center = Offset(x = pointX, y = pointY),
                radius = 12f
            )

            drawContext.canvas.nativeCanvas.drawText(
                pressureUnit.getPressure(
                    weather[index].pressure),
                pointX, pointY - 48f, paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                weather[index].date.toDayOfWeekAndDate(),
                pointX, size.height, paint
            )

            linesList.add(Point(pointX, pointY))
        }

        linesList.forEachIndexed { index, value ->
            if (index > 0) {
                drawLine(
                    start = Offset(x = linesList[index - 1].x, linesList[index - 1].y),
                    end = Offset(x = value.x, y = value.y),
                    color = color,
                    strokeWidth = 5F
                )
            }
        }
    }
}

@Composable
fun CurrentWeatherValuesView(
    modifier: Modifier = Modifier,
    forecast: Hourly,
    pressureUnit: PressureValues,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (
            pressIcon, pressValue, pressText, humidIcon, humidValue,
            humidText, popIcon, popValue, popText, divider
        ) = createRefs()

        createHorizontalChain(pressText, humidText, popText, chainStyle = ChainStyle.Spread)

        SecondaryText(
            modifier = Modifier.constrainAs(pressText) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(humidText.absoluteLeft)
                top.linkTo(parent.top, 4.dp)
            },
            text = stringResource(id = R.string.pressure),
            textColor = MaterialTheme.colors.onPrimary
        )

        SecondaryText(
            modifier = Modifier.constrainAs(humidText) {
                absoluteLeft.linkTo(pressText.absoluteRight)
                absoluteRight.linkTo(popText.absoluteLeft)
                top.linkTo(pressText.top)
            },
            text = stringResource(id = R.string.humidity),
            textColor = MaterialTheme.colors.onPrimary
        )
        SecondaryText(
            modifier = Modifier.constrainAs(popText) {
                absoluteLeft.linkTo(humidText.absoluteRight)
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(pressText.top)
            },
            text = stringResource(id = R.string.precipitation),
            textColor = MaterialTheme.colors.onPrimary
        )

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(pressIcon) {
                    top.linkTo(pressText.bottom, 4.dp)
                    absoluteLeft.linkTo(pressText.absoluteLeft)
                    absoluteRight.linkTo(pressValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_gauge),
            contentDescription = stringResource(id = R.string.pressure),
            tint = MaterialTheme.colors.onPrimary
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
            textColor = MaterialTheme.colors.onPrimary
        )

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(humidIcon) {
                    top.linkTo(humidText.bottom, 4.dp)
                    absoluteLeft.linkTo(humidText.absoluteLeft)
                    absoluteRight.linkTo(humidValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_opacity_24),
            contentDescription = stringResource(id = R.string.humidity),
            tint = MaterialTheme.colors.onPrimary
        )
        PrimaryText(
            modifier = Modifier.constrainAs(humidValue) {
                top.linkTo(humidIcon.top)
                bottom.linkTo(humidIcon.bottom)
                absoluteLeft.linkTo(humidIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(humidText.absoluteRight)
            },
            text = forecast.humidity.toString() + " " + stringResource(id = R.string.percent),
            textColor = MaterialTheme.colors.onPrimary
        )

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(popIcon) {
                    top.linkTo(popText.bottom, 4.dp)
                    absoluteLeft.linkTo(popText.absoluteLeft)
                    absoluteRight.linkTo(popValue.absoluteLeft, 2.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_umbrella_24),
            contentDescription = stringResource(id = R.string.precipitation),
            tint = MaterialTheme.colors.onPrimary
        )
        PrimaryText(
            modifier = Modifier.constrainAs(popValue) {
                top.linkTo(popIcon.top)
                bottom.linkTo(popIcon.bottom)
                absoluteLeft.linkTo(popIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(popText.absoluteRight)
            },
            text = (forecast.probabilityOfPrecipitation * 100).toInt().toString()
                    + " " + stringResource(id = R.string.percent),
            textColor = MaterialTheme.colors.onPrimary
        )

    }
}
