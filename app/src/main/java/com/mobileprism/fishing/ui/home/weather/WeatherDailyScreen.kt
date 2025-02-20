package com.mobileprism.fishing.ui.home.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.time.toDayOfWeekAndDate
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun WeatherDailyScreen(
    upPress: () -> Unit,
    data: DailyWeatherData?
) {

    val pagerState =
        androidx.compose.foundation.pager.rememberPagerState(initialPage = data?.selectedDay ?: 0) {
            data!!.dailyForecast.size
        }

    Scaffold(
        topBar = {
            DefaultAppBar(
                onNavClick = { upPress() },
                title = stringResource(id = R.string.weather)
            )
        },
    ) {
        AnimatedVisibility(visible = data != null) {
            Column(
                modifier = Modifier.padding(it).fillMaxSize().navigationBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, false)) {
                    WeatherDaysTabs(forecast = data!!.dailyForecast, pagerState = pagerState)
                    WeatherTabsContent(forecast = data.dailyForecast, pagerState = pagerState)
                }
                BannerAdvertView(adId = stringResource(R.string.weather_daily_admob_banner_id))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherDaysTabs(forecast: List<Daily>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.primary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
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
                        color = MaterialTheme.colors.primary
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherTabsContent(
    modifier: Modifier = Modifier,
    forecast: List<Daily>,
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        DailyWeatherScreen(forecast = forecast[page])
    }
}

@Composable
fun DailyWeatherScreen(
    modifier: Modifier = Modifier,
    forecast: Daily
) {
    val weatherPrefs: WeatherPreferences = koinInject()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState(0), enabled = true),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        PrimaryWeatherItemView(
            temperature = forecast.temperature.max,
            temperatureUnit = temperatureUnit,
            weather = forecast.weather.first()
        )

        DayTemperatureView(
            temperature = forecast.temperature,
            temperatureUnit = temperatureUnit
        )

        Divider(
            modifier = Modifier.fillMaxWidth()
        )

        SunriseSunsetView(
            sunrise = forecast.sunrise,
            sunset = forecast.sunset
        )

        MoonPhaseView(
            moonPhase = forecast.moonPhase
        )

        Divider(
            modifier = Modifier.fillMaxWidth()
        )

        DailyWeatherValuesView(
            forecast = forecast,
            pressureUnit = pressureUnit,
            windSpeedUnit = windSpeedUnit
        )

        Spacer(modifier = Modifier.size(Constants.bottomBannerPadding))
    }
}










