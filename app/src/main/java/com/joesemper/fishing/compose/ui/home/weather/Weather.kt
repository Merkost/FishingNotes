package com.joesemper.fishing.compose.ui.home.weather

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.compose.ui.home.map.LocationState
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.getCurrentLocationFlow
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.theme.primaryDarkColor
import com.joesemper.fishing.compose.ui.theme.primaryWhiteColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Daily
import com.joesemper.fishing.model.entity.weather.Hourly
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.*
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


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val elevation =
                animateDpAsState(targetValue = if (scrollState.value > 0) 4.dp else 0.dp)
            if (checkPermission(context) && viewModel.markersList.value.isNotEmpty())
                selectedPlace.value = viewModel.markersList.value.first()

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
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            viewModel.currentWeather.value?.let { forecast ->

                CurrentWeather(forecast = forecast)

                HourlyWeather(forecast = forecast.hourly)

                forecast.daily.forEach {
                    DailyWeatherItem(forecast = it)
                }
            }
        }


    }
}

@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
    forecast: WeatherForecast,
) {
    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(370.dp),
        color = primaryDarkColor
    ) {
        ConstraintLayout {
            val (primary, temp, wind, pressure, humidity, pressureTitle, divider) = createRefs()

            val guideline = createGuidelineFromStart(0.5f)

            PrimaryWeatherParameterMeaning(
                modifier = Modifier.constrainAs(primary) {
                    top.linkTo(parent.top, 2.dp)
                    absoluteRight.linkTo(guideline, 16.dp)
                },
                icon = getWeatherIconByName(forecast.hourly.first().weather.first().icon),
                text = forecast.hourly.first().weather.first().description.replaceFirstChar { it.uppercase() }
            )

            WeatherTemperatureMeaning(
                modifier = Modifier.constrainAs(temp) {
                    top.linkTo(primary.top)
                    absoluteLeft.linkTo(guideline, 16.dp)
                },
                temperature = forecast.hourly.first().temperature.toInt()
                    .toString(),
                minTemperature = forecast.daily.first().temperature.min.toInt()
                    .toString(),
                maxTemperature = forecast.daily.first().temperature.max.toInt()
                    .toString()
            )
            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(wind) {
                    top.linkTo(primary.bottom)
                    absoluteRight.linkTo(guideline, 32.dp)
                },
                title = stringResource(id = R.string.wind),
                text = String.format("%.1f", forecast.hourly.first().windSpeed)
                        + " ${stringResource(R.string.wind_speed_units)}",
                primaryIconId = R.drawable.weather_windy,
                iconId = R.drawable.ic_arrow_up,
                iconRotation = forecast.hourly.first().windDeg,
                lightTint = true
            )

            WeatherParameterMeaning(
                modifier = Modifier.constrainAs(humidity) {
                    top.linkTo(wind.top)
                    bottom.linkTo(wind.bottom)
                    absoluteLeft.linkTo(guideline, 32.dp)
                },
                title = stringResource(id = R.string.humidity),
                text = forecast.hourly.first().humidity.toString()
                        + stringResource(R.string.percent),
                primaryIconId = R.drawable.ic_baseline_opacity_24,
                lightTint = true
            )

            /*Divider(modifier = Modifier.fillMaxWidth().size(1.dp).constrainAs(divider) {
                  top.linkTo(humidity.bottom)
                bottom.linkTo(pressureTitle.top)
            }, color = Color.Black)*/

            WeatherHeaderText(
                modifier = Modifier.constrainAs(pressureTitle) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    top.linkTo(wind.bottom, 16.dp)
                },
                color = primaryWhiteColor,
                text = stringResource(id = R.string.pressure) + ", " + pressureUnit + ":"
            )

            BarChartExample(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .width(500.dp)
                    .height(160.dp)
                    .padding(top = 4.dp)
                    .constrainAs(pressure) {
                        top.linkTo(pressureTitle.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                weather = forecast.daily,
                pressureUnit = pressureUnit
            )
        }
    }
}

@Composable
fun HourlyWeather(
    modifier: Modifier = Modifier,
    forecast: List<Hourly>
) {
    Column(modifier = modifier) {
        WeatherHeaderText(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.hourly)
        )
        LazyRow() {
            items(forecast.size) { index ->
                HourlyWeatherItem(forecast = forecast[index])
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Divider()
    }

}


@Composable
fun HourlyWeatherItem(
    modifier: Modifier = Modifier,
    forecast: Hourly
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SecondaryText(text = getTimeBySeconds(forecast.date))
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = getWeatherIconByName(forecast.weather.first().icon)),
            contentDescription = ""
        )
        PrimaryText(
            text = forecast.temperature.toInt().toString() + stringResource(id = R.string.celsius)
        )
        Row() {
            PrimaryText(text = forecast.windSpeed.toInt().toString())
            Icon(
                modifier = Modifier.rotate(forecast.windDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun DailyWeatherItem(
    modifier: Modifier = Modifier,
    forecast: Daily
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val (date, day, divider, temp, tempUnits, weatherIcon, pop) = createRefs()
        val guideline = createGuidelineFromAbsoluteLeft(0.5f)
        WeatherHeaderText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
            },
            text = getDateBySecondsTextMonth(forecast.date)
        )
        SecondaryText(
            modifier = Modifier.constrainAs(day) {
                top.linkTo(date.bottom, 2.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
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
            text = stringResource(id = R.string.celsius),
            textColor = secondaryTextColor
        )
        WeatherPrimaryText(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(tempUnits.top)
                bottom.linkTo(tempUnits.bottom)
                absoluteRight.linkTo(tempUnits.absoluteLeft, 2.dp)
            },
            text = forecast.temperature.day.toInt().toString(),
        )
        Image(
            modifier = Modifier
                .size(42.dp)
                .constrainAs(weatherIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(guideline)
                },
            painter = painterResource(
                id = getWeatherIconByName(forecast.weather.first().icon)
            ),
            contentDescription = "",
        )
        if (forecast.probabilityOfPrecipitation > 0.3) {
            SecondaryText(
                modifier = Modifier.constrainAs(pop) {
                    top.linkTo(weatherIcon.top)
                    bottom.linkTo(weatherIcon.bottom)
                    absoluteLeft.linkTo(weatherIcon.absoluteRight, 2.dp)
                },
                text = (forecast.probabilityOfPrecipitation * 100).toInt()
                    .toString() + stringResource(
                    id = R.string.percent
                )
            )
        }
    }
}

@Composable
private fun BarChartExample(
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

    Canvas(modifier = modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp, top = 32.dp)) {
        val xbounds = Pair(0f, xTarget)
        val ybounds = getBounds(yValues.value)
        val scaleX = size.width / (xbounds.second - xbounds.first)
        val scaleY = size.height / (ybounds.second - ybounds.first)
        val yMove = ybounds.first * scaleY

        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 42f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = 0xFFFFFFFF.toInt()

        val linesList = mutableListOf<Point>()

        (0..min(yValues.value.size - 1, x.value.toInt())).forEach { index ->
            val pointX = index * scaleX
            val pointY = size.height - (yValues.value[index] * scaleY) + yMove - 52f

            drawCircle(
                color = Color.White,
                center = Offset(x = pointX, y = pointY),
                radius = 12f
            )

            drawContext.canvas.nativeCanvas.drawText(
                getPressure(weather[index].pressure, PressureValues.valueOf(pressureUnit))/*hPaToMmHg(weather[index].pressure)*/,
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
                    color = Color.White,
                    strokeWidth = 5F
                )
            }
        }
    }
}

private fun getPressureList(
    forecast: List<Daily>,
    pressureUnit: String
): List<Int> {
    return forecast.map { getPressureInt(it.pressure, PressureValues.valueOf(pressureUnit)) }
}

private fun getBounds(list: List<Int>): Pair<Int, Int> {
    var min = Int.MAX_VALUE
    var max = -Int.MAX_VALUE
    list.forEach {
        min = min.coerceAtMost(it)
        max = max.coerceAtLeast(it)
    }
    return Pair(min, max)
}

data class Point(
    val x: Float,
    val y: Float
)




































