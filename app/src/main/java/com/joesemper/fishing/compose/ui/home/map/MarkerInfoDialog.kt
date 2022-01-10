package com.joesemper.fishing.compose.ui.home.map

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.joesemper.fishing.R
import com.joesemper.fishing.model.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SubtitleText
import com.joesemper.fishing.compose.ui.home.weather.WindSpeedValues
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.resources
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
    receivedMarker: UserMapMarker?,
    lastKnownLocation: MutableState<LatLng?>,
    mapBearing: MutableState<Float>,
    modifier: Modifier = Modifier,
    navController: NavController,
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

    val cant_recognize_place = stringResource(R.string.cant_recognize_place)

    receivedMarker?.let {
        LaunchedEffect(receivedMarker) {
            coroutineScope.launch(Dispatchers.Default) {
                address = null
                delay(800)
                try {
                    val position = geocoder.getFromLocation(receivedMarker.latitude, receivedMarker.longitude, 1)
                    position?.first()?.apply {
                        address = if (!subAdminArea.isNullOrBlank()) {
                            subAdminArea.replaceFirstChar { it.uppercase() }
                        } else if (!adminArea.isNullOrBlank()) {
                            adminArea.replaceFirstChar { it.uppercase() }
                        } else if (!countryName.isNullOrBlank())
                            countryName.replaceFirstChar { it.uppercase() }
                        else "-"
                    }
                } catch (e: Throwable) {
                    //TODO: Ошибка в океане!
                    address = cant_recognize_place
                }
            }
        }

        LaunchedEffect(receivedMarker) {
            viewModel.fishActivity.value = null
            viewModel.getFishActivity(receivedMarker.latitude, receivedMarker.longitude)
        }

        LaunchedEffect(receivedMarker) {
            viewModel.currentWeather.value = null
            viewModel.getCurrentWeather(receivedMarker.latitude, receivedMarker.longitude)
        }

        LaunchedEffect(receivedMarker, viewModel.lastKnownLocation.value) {
            coroutineScope.launch(Dispatchers.Default) {
                distance = null
                lastKnownLocation.value?.let {
                    distance = convertDistance(
                        SphericalUtil.computeDistanceBetween(
                            com.google.android.gms.maps.model.LatLng(
                                receivedMarker.latitude,
                                receivedMarker.longitude
                            ),
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                        )
                    )
                }
            }
        }
    }

    val paddingDp = 8.dp
    val cornersDp = 16.dp
    val elevationDp = 6.dp

    viewModel.currentMarker.value?.let { marker ->
    Card(
        shape = RoundedCornerShape(cornersDp),
        elevation = elevationDp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(paddingDp),
        onClick = { onMarkerClicked(marker, navController) }
    ) {
            AnimatedVisibility(
                true,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500)),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        /*.noRippleClickable(
                            onClick = onDescriptionClick,
                            enabled = scaffoldState.bottomSheetState.isCollapsed
                        )*/
                ) {
                    val (locationIcon, title, area, distanceTo,
                        fish, divider, weather) = createRefs()

                    val horizontalLine = createGuidelineFromAbsoluteLeft(0.5f)
                    val verticalFabLine = createGuidelineFromAbsoluteRight(60.dp)

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
                                linkTo(locationIcon.end, verticalFabLine, 0.dp, 0.dp, 0f)
                                bottom.linkTo(locationIcon.bottom)
                                width = Dimension.fillToConstraints
                            },
                        text = when {
                            marker.title.isNotEmpty() -> marker.title
                            else -> stringResource(R.string.no_name_place)
                        } + "",
                        maxLines = 2,
                    )

                    //Area name
                    SubtitleText(
                        modifier = Modifier
                            .constrainAs(area) {
                                top.linkTo(title.bottom, 4.dp)
                                linkTo(title.start, title.end, 0.dp, 80.dp, 0f)

                            }
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        text = address ?: "",
                    )

                    //Distance
                    Row(
                        modifier = Modifier
                            .constrainAs(distanceTo) {
                                top.linkTo(area.top)
                                bottom.linkTo(area.bottom)
                                linkTo(area.absoluteRight, parent.absoluteRight, 8.dp, 16.dp, 1f)

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
                        SubtitleText(
                            text = distance ?: "",
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp,
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
                        )
                    }

                    //Divider
                    Divider(
                        modifier = Modifier
                            .constrainAs(divider) {
                                top.linkTo(area.bottom, 4.dp)
                                linkTo(horizontalLine, horizontalLine, 0.dp, 0.dp, 0.5f)
                                bottom.linkTo(parent.bottom)
                            }.height(20.dp).width(1.dp),
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
                                animationSpec =
                                tween(durationMillis = 300, easing = LinearOutSlowInEasing)),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onWeatherIconClicked(marker, navController) }) {
                            Icon(painterResource(R.drawable.ic_baseline_navigation_24), "",
                                modifier = Modifier.rotate(currentWeather?.wind_degrees?.let {
                                    it.minus(mapBearing.value) } ?: mapBearing.value),
                                tint = if (fishActivity == null) Color.LightGray else MaterialTheme.colors.primaryVariant
                            )
                        }

                        currentWeather?.let {
                            SubtitleText(
                                text = windUnit.getWindSpeed(it.wind_speed) + " " +
                                        stringResource(windUnit.stringRes)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun onMarkerClicked(marker: UserMapMarker, navController: NavController) {
    navController.navigate(
        MainDestinations.PLACE_ROUTE,
        Arguments.PLACE to marker
    )
}

fun onWeatherIconClicked(marker: UserMapMarker, navController: NavController) {
    navController.navigate(
        "${MainDestinations.HOME_ROUTE}/${MainDestinations.WEATHER_ROUTE}",
        Arguments.PLACE to marker
    )
}





