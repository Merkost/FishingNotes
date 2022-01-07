package com.joesemper.fishing.compose.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SubtitleText
import com.joesemper.fishing.compose.ui.home.weather.WindSpeedValues
import com.joesemper.fishing.compose.ui.resources
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.compose.ui.utils.currentFraction
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.CurrentWeatherFree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MarkerInfoDialog(
    marker: UserMapMarker?,
    lastKnownLocation: MutableState<LatLng?>,
    mapBearing: MutableState<Float>,
    modifier: Modifier = Modifier,
    navController: NavController,
    scaffoldState: BottomSheetScaffoldState,
    upPress: (UserMapMarker) -> Unit,
    onWeatherIconClicked: (UserMapMarker) -> Unit,
    onMarkerIconClicked: (UserMapMarker) -> Unit,
) {
    val context = LocalContext.current

    val viewModel: MapViewModel = getViewModel()
    val weatherPreferences: WeatherPreferences = get()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context, resources().configuration.locale)

    val windUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    /*val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)

    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)*/

    var address: String? by remember { mutableStateOf(null) }
    var distance: String? by remember { mutableStateOf(null) }
    val fishActivity: Int? by remember { viewModel.fishActivity }
    val currentWeather: CurrentWeatherFree? by remember { viewModel.currentWeather }

    /*val weather = marker?.let {
        if (connectionState is ConnectionState.Available) {
            val result by viewModel.getWeather(it.latitude, it.longitude)
                .collectAsState(RetrofitWrapper.Success<WeatherForecast?>(null))

            when (result) {
                is RetrofitWrapper.Success<*> -> {
                    return@let mutableStateOf((result as RetrofitWrapper.Success<WeatherForecast?>).data)
                }
                else -> return@let null
            }
        } else {
            null
        }
    }*/

    marker?.let {
        LaunchedEffect(marker) {
            coroutineScope.launch(Dispatchers.Default) {
                address = null
                delay(800)
                try {
                    val position = geocoder.getFromLocation(marker.latitude, marker.longitude, 1)
                    position?.first()?.apply {
                        address = if (!subAdminArea.isNullOrBlank()) {
                            subAdminArea.replaceFirstChar { it.uppercase() }
                        } else if (!adminArea.isNullOrBlank()) {
                            adminArea.replaceFirstChar { it.uppercase() }
                        } else "-"
                    }
                } catch (e: Throwable) {
                    address = "Нет соединения с сервером"
                }
            }
        }

        LaunchedEffect(marker) {
            viewModel.fishActivity.value = null
            viewModel.getFishActivity(marker.latitude, marker.longitude)
        }

        LaunchedEffect(marker) {
            viewModel.currentWeather.value = null
            viewModel.getCurrentWeather(marker.latitude, marker.longitude)
        }

        LaunchedEffect(marker, viewModel.lastKnownLocation.value) {
            coroutineScope.launch(Dispatchers.Default) {
                distance = null
                lastKnownLocation.value?.let {
                    distance = convertDistance(
                        SphericalUtil.computeDistanceBetween(
                            com.google.android.gms.maps.model.LatLng(
                                marker.latitude,
                                marker.longitude
                            ),
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                        )
                    )
                }
            }
        }
    }

    val paddingDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 6).dp)
    val cornersDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 16).dp)
    val elevationDp = animateDpAsState(((1f - scaffoldState.currentFraction) * 6).dp)

    Card(
        shape = RoundedCornerShape(cornersDp.value),
        elevation = elevationDp.value,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(paddingDp.value),
    ) {

        viewModel.currentMarker.value?.let { marker ->

            AnimatedVisibility(
                scaffoldState.currentFraction == 0f,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp)
                        /*.noRippleClickable(
                            onClick = onDescriptionClick,
                            enabled = scaffoldState.bottomSheetState.isCollapsed
                        )*/
                ) {
                    val (locationIcon, title, area, distanceTo,
                        fish, divider, weather) = createRefs()

                    val horizontalLine = createGuidelineFromAbsoluteLeft(0.5f)


                    Box(modifier = Modifier
                        .size(64.dp).padding(16.dp)

                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            top.linkTo(parent.top)
                        }) {
                        IconButton(onClick = { onMarkerIconClicked(marker) }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                                contentDescription = "Marker",
                                tint = Color(marker.markerColor)
                            )
                        }

                    }

                    PrimaryText(
                        modifier = Modifier
                            .constrainAs(title) {
                                top.linkTo(locationIcon.top)
                                linkTo(locationIcon.end, parent.end, 0.dp, 32.dp, 0f)
                                bottom.linkTo(locationIcon.bottom)
                            },
                        text = when {
                            marker.title.isNotEmpty() -> marker.title
                            else -> stringResource(R.string.no_name_place)
                        } + "",
                    )

                    //Area name
                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(area) {
                                top.linkTo(title.bottom, 4.dp)
                                linkTo(locationIcon.end, parent.end, 0.dp, 80.dp, 0f)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        text = address ?: "",
                        textColor = if (address == null) Color.LightGray else secondaryTextColor
                    )

                    //Distance
                    Row(
                        modifier = Modifier
                            .constrainAs(distanceTo) {
                                top.linkTo(area.top)
                                bottom.linkTo(area.bottom)
                                linkTo(area.absoluteRight, parent.absoluteRight, 0.dp, 16.dp, 1f)
                            }/*.width(80.dp)*/
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SubtitleText(
                            /*overflow = TextOverflow.Ellipsis,*/
                            text = distance ?: "",
                            textColor = if (distance == null) Color.LightGray else secondaryTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }

                    //Fish activity
                    Row(
                        modifier = Modifier
                            .constrainAs(fish) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(parent.absoluteLeft, horizontalLine, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fish),
                            contentDescription = "Marker",
                            modifier = Modifier.size(45.dp).padding(6.dp),
                            tint = if (fishActivity == null) Color.LightGray else MaterialTheme.colors.primary
                        )
                        SubtitleText(
                            text = if (fishActivity != null) fishActivity.toString() + "%" else "",
                            textColor = if (fishActivity == null) Color.LightGray else secondaryTextColor
                        )
                    }

                    //Divider
                    Divider(
                        modifier = Modifier
                            .constrainAs(divider) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(horizontalLine, horizontalLine, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }.height(20.dp)
                            .width(1.dp),
                        color = Color.Gray,
                    )

                    //Weather
                    Row(
                        modifier = Modifier
                            .constrainAs(weather) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(horizontalLine, parent.absoluteRight, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onWeatherIconClicked(marker) }) {
                            Icon(painterResource(R.drawable.ic_baseline_navigation_24), "",
                                modifier = Modifier.rotate(currentWeather?.wind_degrees?.let {
                                    it.minus(mapBearing.value) } ?: mapBearing.value),
                                tint = if (fishActivity == null) Color.LightGray else MaterialTheme.colors.primaryVariant
                            )
                        }

                        currentWeather?.let {
                            SubtitleText(
                                text = windUnit.getWindSpeed(currentWeather!!.wind_speed) + " " +
                                        stringResource(windUnit.stringRes)
                            )
                        }


                    }
                }
            }

        }

    }
    /*AnimatedVisibility(
        scaffoldState.bottomSheetState.progress.fraction != 0f,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
        *//*paddingDp.value == 0.dp
            && !scaffoldState.bottomSheetState.isAnimationRunning*//*
        *//*scaffoldState.currentFraction != 0.0f*//*
    ) {
        Spacer(modifier = Modifier.fillMaxSize())
    }*/

}





