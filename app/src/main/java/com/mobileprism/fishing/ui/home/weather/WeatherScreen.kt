package com.mobileprism.fishing.ui.home.weather

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.use_cases.catches.OpenWeatherMapper
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.map.LocationState
import com.mobileprism.fishing.ui.home.map.checkLocationPermissions
import com.mobileprism.fishing.ui.home.map.locationPermissionsList
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.viewmodels.WeatherViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.ui.viewstates.FishingViewState
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toDayOfWeek
import com.mobileprism.fishing.utils.time.toDayOfWeekAndDate
import com.mobileprism.fishing.utils.time.toTime
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
    viewModel: WeatherViewModel = getViewModel(),
    upPress: () -> Unit,
) {
    viewModel.setSelectedPlace(place)
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val locationManager: LocationManager = get()

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        checkLocationPermissions(context)
        if (permissionsState.allPermissionsGranted) {
            locationManager.getCurrentLocationFlow().collect { locationState ->
                if (locationState is LocationState.LocationGranted) {
                    val newLocation = createCurrentPlaceItem(locationState.location, context)
                    viewModel.locationGranted(newLocation)
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    val weatherUiState by viewModel.weatherState.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val elevation =
                animateDpAsState(targetValue = if (scrollState.value > 0) 4.dp else 0.dp)
            if (checkLocationPermissions(context) && viewModel.markersList.isNotEmpty()) {
                viewModel.setSelectedPlace(viewModel.markersList.first())
            }

            TopAppBar(
                elevation = elevation.value,
                backgroundColor = MaterialTheme.colors.primary,
                title = {
                    selectedPlace?.let {

                        WeatherLocationIconButton(color = Color.White) {
                            //if (it.id != CURRENT_PLACE_ITEM_ID) {
                                navController.navigate(
                                    "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                                    Arguments.PLACE to it
                                )
                            //}
                        }

                        WeatherPlaceSelectItem(
                            selectedPlace = it,
                            userPlaces = viewModel.markersList,
                            onItemClick = viewModel::setSelectedPlace
                        )
                    }

                    if (selectedPlace == null) {
                        Text(text = stringResource(id = R.string.weather))
                    }
                }
            )
        }
    ) {

        if (!permissionsState.allPermissionsGranted && viewModel.markersList.isEmpty()) {
            WeatherNoPlaces(Modifier.fillMaxSize()) { navigateToAddNewPlace(navController) }
        } else {
            Crossfade(targetState = weatherUiState) {
                when (it) {
                    is BaseViewState.Loading -> {
                        MainWeatherScreen(childModifier = Modifier.placeholder(
                            true,
                            color = Color.LightGray,
                            shape = CircleShape,
                            highlight = PlaceholderHighlight.shimmer()
                        ), WeatherForecast(), scrollState, navigateToDaily = {})
                    }
                    is BaseViewState.Success -> {
                        MainWeatherScreen(childModifier = Modifier, it.data, scrollState)
                        { index ->
                            navigateToDailyWeatherScreen(
                                navController = navController,
                                index = index,
                                forecastDaily = it.data.daily
                            )
                        }
                    }
                    is BaseViewState.Error -> {
                        NoInternetView(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherNoPlaces(modifier: Modifier = Modifier, onAddNewPlace: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NoContentView(
            text = stringResource(id = R.string.no_places_added),
            icon = painterResource(id = R.drawable.ic_no_place_on_map)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DefaultButtonOutlinedOld(
                text = stringResource(id = R.string.new_place_text),
                onClick = onAddNewPlace
            )
        }
    }
}

@Composable
fun MainWeatherScreen(
    @SuppressLint("ModifierParameter") childModifier: Modifier = Modifier,
    forecast: WeatherForecast,
    scrollState: ScrollState,
    navigateToDaily: (Int) -> Unit,

    ) {
    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)


    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {

        CurrentWeather(
            childModifier = childModifier,
            forecast = forecast,
            pressureUnit = pressureUnit,
            temperatureUnit = temperatureUnit,
            windSpeedUnit = windSpeedUnit,
        )

        if (forecast.daily.all { it.date != 0L }) {
            PressureChartItem(
                childModifier = childModifier,
                forecast = forecast.daily,
                pressureUnit = pressureUnit,
            )
        }

        forecast.daily.forEachIndexed { index, daily ->
            DailyWeatherItem(
                childModifier = childModifier,
                forecast = daily,
                temperatureUnit = temperatureUnit,
                onDailyWeatherClick = {
                    navigateToDaily(index)
                }
            )
        }
    }
}

@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    temperatureUnit: TemperatureValues,
    pressureUnit: PressureValues,
    windSpeedUnit: WindSpeedValues,
    forecast: WeatherForecast,
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
                childModifier = childModifier,
                temperature = forecast.hourly.first().temperature,
                weather = forecast.hourly.first().weather.first(),
                textTint = Color.White,
                iconTint = Color.White,
                temperatureUnit = temperatureUnit
            )

            CurrentWeatherValuesView(
                childModifier = childModifier,
                forecast = forecast.hourly.first(),
                pressureUnit = pressureUnit,
            )

            HourlyWeather(
                childModifier = childModifier,
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
    childModifier: Modifier = Modifier,
    temperatureUnit: TemperatureValues,
    windSpeedUnit: WindSpeedValues,
    forecastHourly: List<Hourly>,
) {
    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
    ) {
        items(forecastHourly.size) { index ->
            HourlyWeatherItem(
                childModifier = childModifier,
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
    childModifier: Modifier = Modifier,
    forecast: Hourly,
    temperatureUnit: TemperatureValues,
    windSpeedUnit: WindSpeedValues,
    color: Color = Color.White,
    timeTitle: String
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecondaryText(
            text = timeTitle,
            textColor = color
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = childModifier.size(32.dp),
                painter = painterResource(id = OpenWeatherMapper.getFishingWeather(forecast.weather.first().icon).iconRes),
                contentDescription = "",
                //colorFilter = ColorFilter.tint(color = color)
            )
            PrimaryText(
                modifier = childModifier,
                text = temperatureUnit.getTemperature(
                    forecast.temperature
                ) + stringResource(temperatureUnit.stringRes),
                textColor = color
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryText(
                modifier = childModifier,
                text = windSpeedUnit.getDefaultWindSpeed(forecast.windSpeed.toDouble())
                        + " " + stringResource(windSpeedUnit.stringRes),
                textColor = color
            )
            Icon(
                modifier = Modifier
                    .rotate(forecast.windDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                contentDescription = "",
                tint = color
            )
        }
    }
}

@Composable
fun DailyWeatherItem(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    temperatureUnit: TemperatureValues,
    onDailyWeatherClick: () -> Unit,
    forecast: Daily,
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
            modifier = childModifier.constrainAs(date) {
                top.linkTo(parent.top, 8.dp)
                bottom.linkTo(day.top)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = forecast.date.toDateTextMonth()
        )
        SecondaryText(
            modifier = childModifier.constrainAs(day) {
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
            textColor = MaterialTheme.customColors.secondaryTextColor
        )
        WeatherPrimaryText(
            modifier = childModifier.constrainAs(temp) {
                top.linkTo(tempUnits.top)
                bottom.linkTo(tempUnits.bottom)
                absoluteRight.linkTo(tempUnits.absoluteLeft, 2.dp)
            },
            text = temperatureUnit.getTemperature(forecast.temperature.day),
        )
        Image(
            modifier = childModifier
                .size(42.dp)
                .constrainAs(weatherIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteRight.linkTo(guideline, 48.dp)
                },
            painter = painterResource(
                id = OpenWeatherMapper.getFishingWeather(forecast.weather.first().icon).iconRes,
            ),
            contentDescription = "",
        )
        if (forecast.probabilityOfPrecipitation >= 0.2f) {
            Image(
                modifier = childModifier
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
                modifier = childModifier.constrainAs(pop) {
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
    childModifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    forecast: List<Daily>,
) {
    Column(
        modifier = modifier
    ) {
        WeatherHeaderText(
            modifier = Modifier
                .padding(8.dp)
                .then(childModifier),
            text = stringResource(id = R.string.pressure) + ", " + stringResource(pressureUnit.stringRes)
        )
        PressureChart(
            Modifier
                .horizontalScroll(rememberScrollState())
                .width(500.dp)
                .height(120.dp)
                .padding(top = 16.dp),
            childModifier = childModifier,
            receivedWeather = forecast,
            pressureUnit = pressureUnit,
        )
        Divider()
    }
}

@Composable
fun PressureChart(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    textColor: Color = MaterialTheme.colors.onSurface,
    receivedWeather: List<Daily>
) {
    val weather: List<Daily> by remember {
        mutableStateOf(
            receivedWeather
        )
    }

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


    Canvas(modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 14.dp, top = 32.dp)) {
        val xbounds = Pair(0f, xTarget)
        val ybounds = getBounds(yValues.value)
        val scaleX = size.width / (xbounds.second - xbounds.first)
        val scaleY = size.height / (ybounds.second - ybounds.first)
        val yMove = ybounds.first * scaleY

        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 36.sp.value
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = textColor.hashCode()

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
                pressureUnit.getPressureFromHpa(
                    weather[index].pressure
                ),
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
    childModifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    iconColor: Color = Color.White,
    textColor: Color = Color.White,
    forecast: Hourly
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
            textColor = textColor
        )

        SecondaryText(
            modifier = Modifier.constrainAs(humidText) {
                absoluteLeft.linkTo(pressText.absoluteRight)
                absoluteRight.linkTo(popText.absoluteLeft)
                top.linkTo(pressText.top)
            },
            text = stringResource(id = R.string.humidity),
            textColor = textColor
        )
        SecondaryText(
            modifier = Modifier.constrainAs(popText) {
                absoluteLeft.linkTo(humidText.absoluteRight)
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(pressText.top)
            },
            text = stringResource(id = R.string.precipitation),
            textColor = textColor
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
            tint = iconColor
        )
        PrimaryText(
            modifier = childModifier.constrainAs(pressValue) {
                top.linkTo(pressIcon.top)
                bottom.linkTo(pressIcon.bottom)
                absoluteLeft.linkTo(pressIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(pressText.absoluteRight)
            },
            text = pressureUnit.getPressureFromHpa(
                forecast.pressure
            ) + " " + stringResource(pressureUnit.stringRes),
            textColor = textColor
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
            tint = iconColor
        )
        PrimaryText(
            modifier = childModifier.constrainAs(humidValue) {
                top.linkTo(humidIcon.top)
                bottom.linkTo(humidIcon.bottom)
                absoluteLeft.linkTo(humidIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(humidText.absoluteRight)
            },
            text = forecast.humidity.toString() + " " + stringResource(id = R.string.percent),
            textColor = textColor
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
            tint = iconColor
        )
        PrimaryText(
            modifier = childModifier.constrainAs(popValue) {
                top.linkTo(popIcon.top)
                bottom.linkTo(popIcon.bottom)
                absoluteLeft.linkTo(popIcon.absoluteRight, 2.dp)
                absoluteRight.linkTo(popText.absoluteRight)
            },
            text = (forecast.probabilityOfPrecipitation * 100).toInt().toString()
                    + " " + stringResource(id = R.string.percent),
            textColor = textColor
        )

    }
}
