package com.mobileprism.fishing.ui.home.weather

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.home.advertising.AdIds
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.SectionCard
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.utils.time.toDayOfWeekAndDate
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WeatherDailyScreen(
    upPress: () -> Unit,
    data: DailyWeatherData?
) {
    if (data == null) return

    val pagerState =
        androidx.compose.foundation.pager.rememberPagerState(initialPage = data.selectedDay) {
            data.dailyForecast.size
        }

    val weatherPrefs: WeatherPreferences = koinInject()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.weather),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = upPress,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, false)) {
                WeatherDaysTabs(forecast = data.dailyForecast, pagerState = pagerState)
                HorizontalPager(
                    state = pagerState,
                ) { page ->
                    DailyWeatherScreen(
                        forecast = data.dailyForecast[page],
                        pressureUnit = pressureUnit,
                        temperatureUnit = temperatureUnit,
                        windSpeedUnit = windSpeedUnit,
                    )
                }
            }
            BannerAdvertView(adId = AdIds.weatherDailyBanner)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherDaysTabs(forecast: List<Daily>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
            )
        }) {
        forecast.forEachIndexed { index, weather ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = weather.date.toDayOfWeekAndDate(),
                        color = if (pagerState.currentPage == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
            )
        }
    }
}

@Composable
fun DailyWeatherScreen(
    modifier: Modifier = Modifier,
    forecast: Daily,
    pressureUnit: PressureValues,
    temperatureUnit: TemperatureValues,
    windSpeedUnit: WindSpeedValues,
) {
    val weather = forecast.weather.firstOrNull()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
    ) {
        if (weather != null) {
            PrimaryWeatherItemView(
                temperature = forecast.temperature.max,
                temperatureUnit = temperatureUnit,
                weather = weather
            )
        }

        SectionCard(
            icon = painterResource(Res.drawable.weather_sunny),
            title = stringResource(Res.string.day),
        ) {
            DayTemperatureView(
                temperature = forecast.temperature,
                temperatureUnit = temperatureUnit
            )
        }

        SectionCard(
            icon = painterResource(Res.drawable.ic_sunrise_morning_svgrepo_com),
            title = stringResource(Res.string.sunrise_sunset),
        ) {
            SunriseSunsetView(
                sunrise = forecast.sunrise,
                sunset = forecast.sunset
            )
            MoonPhaseView(moonPhase = forecast.moonPhase)
        }

        SectionCard(
            icon = painterResource(Res.drawable.ic_gauge),
            title = stringResource(Res.string.weather),
        ) {
            DailyWeatherValuesView(
                forecast = forecast,
                pressureUnit = pressureUnit,
                windSpeedUnit = windSpeedUnit
            )
        }
    }
}
