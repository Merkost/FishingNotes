package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
        pageCount = data?.dailyForecast?.size ?: 0,
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

    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (primary) = createRefs()

        PrimaryWeatherItem(
            modifier = Modifier.constrainAs(primary) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            temperature = forecast.temperature.max,
            weather = forecast.weather.first(),
            temperatureUnit = temperatureUnit
        )
    }
}










