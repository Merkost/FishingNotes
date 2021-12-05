package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.pager.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.DefaultAppBar
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.utils.getDayOfWeekAndDate
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

    val pagerState = rememberPagerState(
        initialPage = data?.selectedDay ?: 0
    )

    Scaffold(
        topBar = {
            DefaultAppBar(
                onNavClick = { upPress() },
                title = stringResource(id = R.string.weather)
            )
        }
    ) {
        AnimatedVisibility(visible = data != null) {
            Column() {
                WeatherDaysTabs(forecast = data!!.dailyForecast, pagerState = pagerState)
                WeatherTabsContent(forecast = data.dailyForecast, pagerState = pagerState)
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
                        text = getDayOfWeekAndDate(weather.date),
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
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C.name)

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (primary, temperature, sunriseSunset, weather, moonPhase) = createRefs()

        PrimaryWeatherItemView(
            modifier = Modifier.constrainAs(primary) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            temperature = forecast.temperature.max,
            weather = forecast.weather.first(),
            temperatureUnit = temperatureUnit
        )

        DayTemperatureView(
            modifier = Modifier.constrainAs(temperature) {
                top.linkTo(primary.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            temperature = forecast.temperature,
            temperatureUnit = temperatureUnit
        )

        SunriseSunsetView(
            modifier = Modifier.constrainAs(sunriseSunset) {
                top.linkTo(temperature.bottom, 24.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            sunrise = forecast.sunrise,
            sunset = forecast.sunset
        )

        MoonPhaseView(
            modifier = Modifier.constrainAs(moonPhase) {
                top.linkTo(sunriseSunset.bottom, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            moonPhase = forecast.moonPhase
        )

        DailyWeatherValuesView(
            modifier = Modifier.constrainAs(weather) {
                top.linkTo(moonPhase.bottom, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            forecast = forecast,
            pressureUnit = pressureUnit
        )

    }
}










