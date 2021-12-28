package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.BannerAdvertView
import com.joesemper.fishing.compose.ui.home.views.DefaultAppBar
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.utils.Constants
import com.joesemper.fishing.utils.time.toDayOfWeekAndDate
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun WeatherDaily(
    upPress: () -> Unit,
    data: DailyWeatherData?
) {

    val pagerState = rememberPagerState(initialPage = data?.selectedDay ?: 0)

    BottomSheetScaffold(
        topBar = {
            DefaultAppBar(
                onNavClick = { upPress() },
                title = stringResource(id = R.string.weather)
            )
        },
        sheetContent = {
            BannerAdvertView(adId = stringResource(R.string.weather_daily_admob_banner_id))
        },
        sheetShape = RectangleShape,
        sheetGesturesEnabled = false,
        sheetPeekHeight = 0.dp
    ) {
        AnimatedVisibility(visible = data != null) {
            Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween) {
                Column(
                ) {
                    WeatherDaysTabs(forecast = data!!.dailyForecast, pagerState = pagerState)
                    WeatherTabsContent(forecast = data.dailyForecast, pagerState = pagerState)

                }

            }

        }
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
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

@ExperimentalPagerApi
@Composable
fun WeatherTabsContent(
    modifier: Modifier = Modifier,
    forecast: List<Daily>,
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        count = forecast.size,
        modifier = modifier.fillMaxSize()
    ) { page ->
        DailyWeatherScreen(forecast = forecast[page])
    }
}

@Composable
fun DailyWeatherScreen(
    modifier: Modifier = Modifier,
    forecast: Daily
) {
    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(0), enabled = true),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        PrimaryWeatherItemView(
            temperature = forecast.temperature.max,
            weather = forecast.weather.first(),
            temperatureUnit = temperatureUnit
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
            pressureUnit = pressureUnit
        )

        Spacer(modifier = Modifier.size(Constants.bottomBannerPadding))
    }
}










