package com.mobileprism.fishing.ui.home.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.TIME_TO_EXIT
import com.mobileprism.fishing.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

sealed class GeocoderResult {
    class Success(val placeName: String) : GeocoderResult()
    object NoNamePlace : GeocoderResult()
    object Failed : GeocoderResult()
    object InProgress : GeocoderResult()
}

data class PlaceTileState(
    val geocoderResult: GeocoderResult = GeocoderResult.InProgress,
    val pointerState: PointerState = PointerState.ShowMarker,
)

fun getHueFromColor(color: Color) = getHue(
    red = color.red,
    green = color.green,
    blue = color.blue
)

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

suspend fun moveCameraToLocation(
    cameraPositionState: CameraPositionState,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = 0f
) {
    cameraPositionState.animate(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .zoom(zoom)
                .target(location)
                .bearing(bearing)
                .build()
        )
    )
}

suspend fun setCameraPosition(
    cameraPositionState: CameraPositionState,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM,
    bearing: Float = DEFAULT_BEARING
) {
    cameraPositionState.move(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .zoom(zoom)
                .target(location)
                .bearing(bearing)
                .build()
        )
    )
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
    upPress: () -> Unit,
) {
    val context = LocalContext.current
    var lastPressed by remember { mutableStateOf(0L) }

    BackHandler(true) {
        when (mapUiState) {
            MapUiState.NormalMode -> {
                if (navController.navigateUp()) {
                    return@BackHandler
                } else {
                    val currentMillis = System.currentTimeMillis()
                    if (currentMillis - lastPressed < TIME_TO_EXIT) {
                        upPress()
                    } else {
                        context.applicationContext.showToast(
                            context.getString(R.string.app_exit_message)
                        )
                    }
                    lastPressed = currentMillis
                }
            }
            else -> onBackPressedCallback()
        }
    }
}

object DistanceFormat {
    val df = DecimalFormat("#.#")
}

fun Context.convertDistance(distanceInMeters: Double): String {
    return when (distanceInMeters.toInt()) {
        in 0..999 -> distanceInMeters.toInt().toString() + " ${getString(R.string.m)}"
        in 1001..9999 -> DistanceFormat.df.format(distanceInMeters / 1000f)
            .toString() + " ${getString(R.string.km)}"
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

fun Context.getMapStyleByTheme(darkTheme: Boolean): MapStyleOptions {
    return if (darkTheme) {
        MapStyleOptions.loadRawResourceStyle(
            this,
            R.raw.map_style_fishing_night
        )
    } else {
        MapStyleOptions.loadRawResourceStyle(
            this,
            R.raw.map_style_fishing
        )
    }
}