package com.mobileprism.fishing.ui.home.weather

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import com.mobileprism.fishing.ui.viewmodels.WeatherViewModel
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import com.mobileprism.fishing.ui.components.state.ScreenStateContent
import com.mobileprism.fishing.ui.components.state.EmptyState
import com.mobileprism.fishing.ui.components.state.ErrorStateNoInternet
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toDayOfWeek
import com.mobileprism.fishing.utils.time.toDayOfWeekAndDate
import com.mobileprism.fishing.utils.time.toTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.mobileprism.fishing.ui.theme.Spacing

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
            AppTopBar(
                title = selectedPlace?.title ?: stringResource(Res.string.weather),
                navigationIcon = if (selectedPlace != null) Icons.AutoMirrored.Filled.ArrowBack else null,
                onNavigationClick = if (selectedPlace != null) {
                    {
                        navController.navigate(
                            MainDestinations.Map(isAddingNewPlace = false, place = selectedPlace!!)
                        )
                    }
                } else null,
                actions = {
                    if (selectedPlace != null) {
                        LocationPickerChip(
                            label = selectedPlace!!.title,
                            contentDescription = selectedPlace!!.title,
                            onClick = { showBottomSheet = true },
                        )
                    }
                },
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
            ScreenStateContent(
                state = weatherUiState,
                modifier = Modifier.padding(innerPadding),
                loading = { WeatherSkeleton() },
                error = { ErrorStateNoInternet(onRetry = { viewModel.retry() }) },
                isEmpty = { it.hourly.isEmpty() && it.daily.isEmpty() },
                empty = {
                    EmptyState(
                        illustration = painterResource(Res.drawable.ic_no_place_on_map),
                        title = stringResource(Res.string.no_weather_data),
                    )
                },
            ) { forecast ->
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() }
                ) {
                    MainWeatherScreen(
                        forecast = forecast,
                        scrollState = scrollState,
                        weatherSource = weatherSource,
                    ) { index ->
                        navigateToDailyWeatherScreen(
                            navController = navController,
                            index = index,
                            forecastDaily = forecast.daily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherNoPlaces(modifier: Modifier = Modifier, onAddNewPlace: () -> Unit) {
    EmptyState(
        modifier = modifier,
        illustration = painterResource(Res.drawable.ic_no_place_on_map),
        title = stringResource(Res.string.no_places_added),
        action = {
            AppButton(
                text = stringResource(Res.string.new_place_text),
                onClick = onAddNewPlace,
                style = AppButtonStyle.Outlined,
            )
        },
    )
}

@Composable
fun MainWeatherScreen(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (weatherSource == WeatherSource.STALE_FALLBACK) {
            InlineBannerCard(
                tone = BannerTone.Info,
                icon = Icons.Default.Info,
                title = stringResource(Res.string.cached_data),
            )
        }

        CurrentWeather(
            forecast = forecast,
            pressureUnit = pressureUnit,
            temperatureUnit = temperatureUnit,
            windSpeedUnit = windSpeedUnit,
            is12hTimeFormat = is12hTimeFormat,
        )

        if (forecast.daily.all { it.date != 0L } && forecast.daily.isNotEmpty()) {
            PressureChartItem(
                forecast = forecast.daily,
                pressureUnit = pressureUnit,
            )
            PrecipitationChartItem(
                forecast = forecast.daily,
            )
        }

        if (forecast.daily.isNotEmpty()) {
            SectionCard(
                icon = painterResource(Res.drawable.calendar_week),
                title = stringResource(Res.string.weather),
            ) {
                forecast.daily.forEachIndexed { index, daily ->
                    WeatherDailyForecastRow(
                        date = daily.date.toDateTextMonth() + " " + daily.date.toDayOfWeek(),
                        icon = getWeatherIconByName(daily.weather.firstOrNull()?.icon ?: ""),
                        temperature = temperatureText(temperatureUnit, daily.temperature.day),
                        precipitation = if (isHeavyPrecipitation(daily.probabilityOfPrecipitation)) {
                            percentText(probabilityToPercent(daily.probabilityOfPrecipitation))
                        } else null,
                        onClick = { navigateToDaily(index) },
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
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
                .padding(vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            val currentHour = forecast.hourly.firstOrNull()
            val currentWeather = currentHour?.weather?.firstOrNull()

            if (currentHour != null && currentWeather != null) {
                PrimaryWeatherItemView(
                    temperature = currentHour.temperature,
                    weather = currentWeather,
                    textTint = MaterialTheme.colorScheme.onPrimary,
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    temperatureUnit = temperatureUnit
                )

                val currentMetrics: List<@Composable () -> Unit> = listOf(
                    {
                        WeatherMetric(
                            label = stringResource(Res.string.pressure),
                            icon = Res.drawable.ic_gauge,
                            value = pressureText(pressureUnit, currentHour.pressure),
                            iconTint = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    {
                        WeatherMetric(
                            label = stringResource(Res.string.humidity),
                            icon = Res.drawable.ic_baseline_opacity_24,
                            value = percentText(currentHour.humidity),
                            iconTint = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    {
                        WeatherMetric(
                            label = stringResource(Res.string.precipitation),
                            icon = Res.drawable.ic_baseline_umbrella_24,
                            value = percentText(probabilityToPercent(currentHour.probabilityOfPrecipitation)),
                            iconTint = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                )
                WeatherStatGrid(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
                    metrics = currentMetrics,
                )
            }

            HourlyWeather(
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
        AppText(
            text = timeTitle,
            style = AppTextStyle.Body,
            color = color,
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(getWeatherIconByName(forecast.weather.firstOrNull()?.icon ?: "")),
                contentDescription = null,
            )
            AppText(
                text = temperatureText(temperatureUnit, forecast.temperature),
                style = AppTextStyle.Title,
                color = color
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = windSpeedText(windSpeedUnit, forecast.windSpeed.toDouble()),
                style = AppTextStyle.Title,
                color = color
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
fun PressureChartItem(
    modifier: Modifier = Modifier,
    pressureUnit: PressureValues,
    forecast: List<Daily>,
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_gauge),
        title = labelWithUnit(Res.string.pressure, pressureUnit.stringRes),
    ) {
        WeatherTrendChart(
            modifier = Modifier.fillMaxWidth(),
            height = 140.dp,
            points = forecast.mapIndexed { index, daily ->
                WeatherChartPoint(
                    label = daily.date.toDayOfWeekAndDate(),
                    value = getPressureList(forecast, pressureUnit)[index].toDouble(),
                )
            },
            formatValue = { pressureUnit.getPressureFromHpa(it.toInt()) },
        )
    }
}

@Composable
fun PrecipitationChartItem(
    modifier: Modifier = Modifier,
    forecast: List<Daily>,
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_baseline_umbrella_24),
        title = labelWithUnit(Res.string.precipitation, Res.string.percent),
    ) {
        WeatherTrendChart(
            modifier = Modifier.fillMaxWidth(),
            height = 140.dp,
            points = forecast.map { daily ->
                val percent = probabilityToPercent(daily.probabilityOfPrecipitation)
                WeatherChartPoint(
                    label = daily.date.toDayOfWeekAndDate(),
                    value = percent.toDouble(),
                )
            },
            formatValue = { "${it.toInt()}%" },
        )
    }
}
