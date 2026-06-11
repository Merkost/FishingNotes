package com.mobileprism.fishing.ui.home.weather

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobileprism.fishing.ui.utils.placeholder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WeatherSource
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.domain.entity.weather.Hourly
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.viewmodels.WeatherViewModel
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toDayOfWeek
import com.mobileprism.fishing.utils.time.toDayOfWeekAndDate
import com.mobileprism.fishing.utils.time.toTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    place: UserMapMarker? = null,
    viewModel: WeatherViewModel = koinViewModel(),
    upPress: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.setInitialPlace(place)
    }
    val permissionsController = rememberPermissionsController()
    var locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)

    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val currentLocationTitle = stringResource(Res.string.current_location)

    ObserveCurrentLocation(
        locationPermissionGranted = locationPermissionGranted,
        currentLocationTitle = currentLocationTitle,
        onLocationReceived = { lat, lng, title ->
            val newLocation = createCurrentPlaceItem(lat, lng, title)
            viewModel.locationGranted(newLocation)
        }
    )

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val weatherUiState by viewModel.weatherState.collectAsState()
    val weatherSource by viewModel.weatherSource.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val markers by viewModel.markersList.collectAsState()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            shape = modalBottomSheetCorners,
        ) {
            WeatherPlacePickerSheetContent(
                places = markers,
                selectedPlace = selectedPlace,
                onPlaceSelected = { place ->
                    viewModel.setSelectedPlace(place)
                    showBottomSheet = false
                },
            )
        }
    }

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        selectedPlace?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                WeatherLocationIconButton(color = MaterialTheme.colorScheme.onPrimary) {
                                    navController.navigate(
                                        MainDestinations.Map(isAddingNewPlace = false, place = it)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Surface(
                                    modifier = Modifier
                                        .clickable { showBottomSheet = true },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = it.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 200.dp),
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        if (selectedPlace == null) {
                            Text(text = stringResource(Res.string.weather))
                        }
                    }
                )
            }
        ) { innerPadding ->

            if (!locationPermissionGranted && markers.isEmpty()) {
                WeatherNoPlaces(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { navigateToAddNewPlace(navController) }
            } else {
                Crossfade(
                    targetState = weatherUiState,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    when (it) {
                        is BaseViewState.Loading -> {
                            MainWeatherScreen(childModifier = Modifier.placeholder(
                                true,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            ), WeatherForecast(), scrollState, navigateToDaily = {})
                        }
                        is BaseViewState.Success -> {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.refresh() }
                            ) {
                                MainWeatherScreen(
                                    childModifier = Modifier,
                                    forecast = it.data,
                                    scrollState = scrollState,
                                    weatherSource = weatherSource,
                                ) { index ->
                                    navigateToDailyWeatherScreen(
                                        navController = navController,
                                        index = index,
                                        forecastDaily = it.data.daily
                                    )
                                }
                            }
                        }
                        is BaseViewState.Error -> {
                            NoInternetView(
                                modifier = Modifier.fillMaxWidth(),
                                onRetry = { viewModel.retry() }
                            )
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
            text = stringResource(Res.string.no_places_added),
            icon = painterResource(Res.drawable.ic_no_place_on_map)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DefaultButtonOutlined(
                text = stringResource(Res.string.new_place_text),
                onClick = onAddNewPlace
            )
        }
    }
}

@Composable
fun MainWeatherScreen(
    childModifier: Modifier = Modifier,
    forecast: WeatherForecast,
    scrollState: ScrollState,
    weatherSource: WeatherSource? = null,
    navigateToDaily: (Int) -> Unit,

    ) {
    val weatherPrefs: WeatherPreferences = koinInject()
    val userPrefs: UserPreferences = koinInject()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    val is12hTimeFormat by userPrefs.use12hTimeFormat.collectAsState(initial = false)


    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        if (weatherSource == WeatherSource.STALE_FALLBACK) {
            Text(
                text = stringResource(Res.string.cached_data),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        CurrentWeather(
            childModifier = childModifier,
            forecast = forecast,
            pressureUnit = pressureUnit,
            temperatureUnit = temperatureUnit,
            windSpeedUnit = windSpeedUnit,
            is12hTimeFormat = is12hTimeFormat,
        )

        if (forecast.daily.all { it.date != 0L }) {
            PressureChartItem(
                childModifier = childModifier,
                forecast = forecast.daily,
                pressureUnit = pressureUnit,
            )
            PrecipitationChartItem(
                childModifier = childModifier,
                forecast = forecast.daily,
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
    is12hTimeFormat: Boolean,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            PrimaryWeatherItemView(
                childModifier = childModifier,
                temperature = forecast.hourly.first().temperature,
                weather = forecast.hourly.first().weather.first(),
                textTint = MaterialTheme.colorScheme.onPrimary,
                iconTint = MaterialTheme.colorScheme.onPrimary,
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
                is12hTimeFormat = is12hTimeFormat,
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
    is12hTimeFormat: Boolean,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(forecastHourly.size) { index ->
            HourlyWeatherItem(
                childModifier = childModifier,
                forecast = forecastHourly[index],
                timeTitle = if (index == 0) {
                    stringResource(Res.string.now)
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
    color: Color = MaterialTheme.colorScheme.onPrimary,
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
                painter = painterResource(getWeatherIconByName(forecast.weather.first().icon)),
                contentDescription = null,
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
                painter = painterResource(Res.drawable.ic_baseline_navigation_24),
                contentDescription = null,
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onDailyWeatherClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                WeatherHeaderText(
                    modifier = childModifier,
                    text = forecast.date.toDateTextMonth()
                )
                SecondaryText(
                    modifier = childModifier,
                    text = forecast.date.toDayOfWeek()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Weather icon
            Image(
                modifier = childModifier.size(42.dp),
                painter = painterResource(
                    getWeatherIconByName(forecast.weather.first().icon)
                ),
                contentDescription = null,
            )

            // Precipitation
            if (forecast.probabilityOfPrecipitation >= 0.2f) {
                Image(
                    modifier = childModifier
                        .size(24.dp)
                        .padding(start = 8.dp),
                    painter = painterResource(
                        Res.drawable.ic_baseline_umbrella_24
                    ),
                    contentDescription = null,
                )
                SecondaryText(
                    modifier = childModifier.padding(start = 4.dp),
                    text = (forecast.probabilityOfPrecipitation * 100).toInt()
                        .toString() + stringResource(Res.string.percent)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Temperature
            WeatherPrimaryText(
                modifier = childModifier,
                text = temperatureUnit.getTemperature(forecast.temperature.day),
            )
            WeatherPrimaryText(
                modifier = Modifier.padding(start = 2.dp),
                text = stringResource(temperatureUnit.stringRes),
                textColor = MaterialTheme.customColors.secondaryTextColor
            )
        }
        HorizontalDivider()
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
            text = stringResource(Res.string.pressure) + ", " + stringResource(pressureUnit.stringRes)
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
        HorizontalDivider()
    }
}

@Composable
fun PressureChart(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
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

    val color = MaterialTheme.colorScheme.tertiary
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        textAlign = TextAlign.Center
    )

    Canvas(modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 14.dp, top = 32.dp)) {
        val xbounds = Pair(0f, xTarget)
        val ybounds = getBounds(yValues.value)
        val scaleX = size.width / (xbounds.second - xbounds.first)
        val scaleY = size.height / (ybounds.second - ybounds.first)
        val yMove = ybounds.first * scaleY

        val linesList = mutableListOf<Point>()

        (0..min(yValues.value.size - 1, x.value.toInt())).forEach { index ->
            val pointX = index * scaleX
            val pointY = size.height - (yValues.value[index] * scaleY) + yMove - 52f

            drawCircle(
                color = color,
                center = Offset(x = pointX, y = pointY),
                radius = 12f
            )

            val pressureText = pressureUnit.getPressureFromHpa(weather[index].pressure)
            val pressureResult = textMeasurer.measure(pressureText, textStyle)
            drawText(
                pressureResult,
                topLeft = Offset(pointX - pressureResult.size.width / 2f, pointY - 48f - pressureResult.size.height / 2f)
            )

            val dateText = weather[index].date.toDayOfWeekAndDate()
            val dateResult = textMeasurer.measure(dateText, textStyle)
            drawText(
                dateResult,
                topLeft = Offset(pointX - dateResult.size.width / 2f, size.height - dateResult.size.height)
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
fun PrecipitationChartItem(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    forecast: List<Daily>,
) {
    Column(
        modifier = modifier
    ) {
        WeatherHeaderText(
            modifier = Modifier
                .padding(8.dp)
                .then(childModifier),
            text = stringResource(Res.string.precipitation) + ", " + stringResource(Res.string.percent)
        )
        PrecipitationChart(
            Modifier
                .horizontalScroll(rememberScrollState())
                .width(500.dp)
                .height(120.dp)
                .padding(top = 16.dp),
            childModifier = childModifier,
            receivedWeather = forecast,
        )
        HorizontalDivider()
    }
}

@Composable
fun PrecipitationChart(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    receivedWeather: List<Daily>
) {
    val weather: List<Daily> by remember {
        mutableStateOf(receivedWeather)
    }

    val x = remember { Animatable(0f) }
    val yValues = remember(weather) { mutableStateOf(getPrecipitationList(weather)) }
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

    val barColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        textAlign = TextAlign.Center
    )

    Canvas(modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 14.dp, top = 32.dp)) {
        val slotCount = yValues.value.size
        if (slotCount == 0) return@Canvas
        val slotWidth = size.width / slotCount
        val barWidth = slotWidth * 0.5f
        val maxBarHeight = size.height - 52f

        (0..min(yValues.value.size - 1, x.value.toInt())).forEach { index ->
            val centerX = index * slotWidth + slotWidth / 2f
            val value = yValues.value[index]
            val barHeight = (value / 100f) * maxBarHeight

            val barLeft = centerX - barWidth / 2f
            val barTop = size.height - 52f - barHeight

            if (barHeight > 0f) {
                drawRect(
                    color = barColor.copy(alpha = 0.6f),
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
                drawRect(
                    color = barColor,
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    style = Stroke(2f)
                )
            }

            val percentText = "${value}%"
            val percentResult = textMeasurer.measure(percentText, textStyle)
            drawText(
                percentResult,
                topLeft = Offset(centerX - percentResult.size.width / 2f, barTop - 12f - percentResult.size.height)
            )

            val dateText = weather[index].date.toDayOfWeekAndDate()
            val dateResult = textMeasurer.measure(dateText, textStyle)
            drawText(
                dateResult,
                topLeft = Offset(centerX - dateResult.size.width / 2f, size.height - dateResult.size.height)
            )
        }
    }
}

@Composable
fun CurrentWeatherValuesView(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    forecast: Hourly
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Pressure column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SecondaryText(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(Res.string.pressure),
                textColor = textColor
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.ic_gauge),
                    contentDescription = stringResource(Res.string.pressure),
                    tint = iconColor
                )
                PrimaryText(
                    modifier = childModifier.padding(start = 2.dp),
                    text = pressureUnit.getPressureFromHpa(
                        forecast.pressure
                    ) + " " + stringResource(pressureUnit.stringRes),
                    textColor = textColor
                )
            }
        }

        // Humidity column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SecondaryText(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(Res.string.humidity),
                textColor = textColor
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.ic_baseline_opacity_24),
                    contentDescription = stringResource(Res.string.humidity),
                    tint = iconColor
                )
                PrimaryText(
                    modifier = childModifier.padding(start = 2.dp),
                    text = forecast.humidity.toString() + " " + stringResource(Res.string.percent),
                    textColor = textColor
                )
            }
        }

        // Precipitation column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SecondaryText(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(Res.string.precipitation),
                textColor = textColor
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.ic_baseline_umbrella_24),
                    contentDescription = stringResource(Res.string.precipitation),
                    tint = iconColor
                )
                PrimaryText(
                    modifier = childModifier.padding(start = 2.dp),
                    text = (forecast.probabilityOfPrecipitation * 100).toInt().toString()
                            + " " + stringResource(Res.string.percent),
                    textColor = textColor
                )
            }
        }
    }
}
