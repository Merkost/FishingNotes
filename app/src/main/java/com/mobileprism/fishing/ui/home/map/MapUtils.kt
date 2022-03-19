package com.mobileprism.fishing.ui.home.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.tasks.Task
import com.google.maps.android.ktx.awaitMap
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.utils.isCoordinatesFar
import com.mobileprism.fishing.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object MapTypes {
    const val none = GoogleMap.MAP_TYPE_NONE
    const val roadmap = GoogleMap.MAP_TYPE_NORMAL
    const val satellite = GoogleMap.MAP_TYPE_SATELLITE
    const val hybrid = GoogleMap.MAP_TYPE_HYBRID
    const val terrain = GoogleMap.MAP_TYPE_TERRAIN
}

fun getHue(red: Float, green: Float, blue: Float): Float {
    val min = min(min(red, green), blue)
    val max = max(max(red, green), blue)
    val c = max - min
    if (min == max) {
        return 0f
    }
    var hue = 0f
    when (max) {
        red -> {
            val segment = (green - blue) / c;
            var shift = 0 / 60;       // R° / (360° / hex sides)
            if (segment < 0) {          // hue > 180, full rotation
                shift = 360 / 60;         // R° / (360° / hex sides)
            }
            hue = segment + shift;
        }
        green -> {
            val segment = (blue - red) / c;
            val shift = 120 / 60;     // G° / (360° / hex sides)
            hue = segment + shift;
        }
        blue -> {
            val segment = (red - green) / c;
            val shift = 240 / 60;     // B° / (360° / hex sides)
            hue = segment + shift;
        }
    }
    return hue * 60; // hue is in [0,6], scale it up
}

sealed class CameraMoveState {
    object MoveStart : CameraMoveState()
    object MoveFinish : CameraMoveState()
}

sealed class PointerState {
    object HideMarker : PointerState()
    object ShowMarker : PointerState()
}

sealed class LocationState() {
    object NoPermission : LocationState()
    class LocationGranted(val location: LatLng) : LocationState()
    object LocationNotGranted : LocationState()
    object GpsNotEnabled : LocationState()
}

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    object BottomSheetInfoMode : MapUiState()
    //object BottomSheetFullyExpanded : MapUiState()
}

const val DEFAULT_ZOOM = 15f
const val DEFAULT_BEARING = 0f

val locationPermissionsList = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun moveCameraToLocation(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = 0f
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.stopAnimation()
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .zoom(zoom)
                    .target(location)
                    .bearing(bearing)
                    .build()
            )
        )
    }
}

fun setCameraPosition(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = DEFAULT_BEARING
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.stopAnimation()
        googleMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .zoom(zoom)
                    .target(location)
                    .bearing(bearing)
                    .build()
            )
        )
    }
}

fun checkLocationPermissions(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
}

fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }



fun startMapsActivityForNavigation(mapMarker: UserMapMarker, context: Context) {
    val uri = String.format(
        Locale.ENGLISH,
        "http://maps.google.com/maps?daddr=%f,%f (%s)",
        mapMarker.latitude,
        mapMarker.longitude,
        mapMarker.title
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: ActivityNotFoundException) {
        try {
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            ContextCompat.startActivity(context, unrestrictedIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.install_maps_app), Toast.LENGTH_LONG)
                .show()
        }
    }
}

@Composable
fun BackPressHandler(
    mapUiState: MapUiState,
    navController: NavController,
    onBackPressedCallback: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    var lastPressed: Long = 0

    BackHandler(onBack = {
        when (mapUiState) {
            MapUiState.NormalMode -> {
                if (navController.navigateUp()) {
                    return@BackHandler
                } else {
                    val currentMillis = System.currentTimeMillis()
                    if (currentMillis - lastPressed < 2000) {
                        (context as MainActivity).finish()
                    } else {
                        showToast(context, context.getString(R.string.app_exit_message))
                    }
                    lastPressed = currentMillis
                }
            }
            else -> onBackPressedCallback()
        }
    })
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = (LocalContext.current as MainActivity)
    val isDarkMode = isSystemInDarkTheme()
    val mapOptions = when (isDarkMode) {
        true -> { GoogleMapOptions().mapId(context.resources.getString(R.string.dark_map_id)) }
        false -> { GoogleMapOptions().mapId(context.resources.getString(R.string.light_map_id)) }
    }
    val mapView: MapView = remember(isDarkMode) {
        MapView(
            context,
            mapOptions
        )
    }.apply { id = R.id.map }


    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}

object DistanceFormat {
    val df = DecimalFormat("#.#")
}

fun Context.convertDistance(distanceInMeters: Double): String {
    return when (distanceInMeters.toInt()) {
        in 0..999 -> distanceInMeters.toInt().toString() + " ${getString(R.string.m)}"
        in 1001..9999 -> DistanceFormat.df.format(distanceInMeters / 1000f).toString() + " ${getString(R.string.km)}"
        else -> distanceInMeters.div(1000).toInt().toString() + " ${getString(R.string.km)}"
    }
}

fun getIconRotationByWeatherIn8H(forecast: WeatherForecast): Float {
    return if (forecast.hourly.first().pressure > forecast.hourly[7].pressure) 180f else 0f
}

fun getIconRotationByWeatherIn16H(forecast: WeatherForecast): Float {
    return if (forecast.hourly[7].pressure > forecast.hourly[15].pressure) 180f else 0f
}

fun getIconTintByWeatherIn8H(forecast: WeatherForecast): Color {
    return if (forecast.hourly.first().pressure > forecast.hourly[7].pressure) Color.Red else Color.Green
}

fun getIconTintByWeatherIn16H(forecast: WeatherForecast): Color {
    return if (forecast.hourly[7].pressure > forecast.hourly[15].pressure) Color.Red else Color.Green
}