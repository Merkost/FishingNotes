package com.joesemper.fishing.compose.ui.home.map

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
import com.google.android.gms.tasks.Task
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.CameraPosition
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.MainActivity
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.utils.isCoordinatesFar
import com.joesemper.fishing.utils.showToast
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
}

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    object BottomSheetInfoMode : MapUiState()
    //object BottomSheetFullyExpanded : MapUiState()
}

const val DEFAULT_ZOOM = 15f

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
            /*CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            )*/
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

fun setCameraBearing(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .zoom(zoom)
                    .target(location)
                    .bearing(0f)
                    .build()
            )
        )
    }
}

fun setCameraPosition(
    coroutineScope: CoroutineScope,
    map: MapView,
    location: LatLng,
    zoom: Float = DEFAULT_ZOOM
) {
    coroutineScope.launch {
        val googleMap = map.awaitMap()
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            )
        )
    }
}

fun checkPermission(context: Context): Boolean {
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


@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
fun getCurrentLocationFlow(
    context: Context,
    permissionsState: MultiplePermissionsState,
) = callbackFlow {

    var previousCoordinates = LatLng(0.0, 0.0)

    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    checkPermission(context)

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnSuccessListener { task ->
            if (task != null) {
                val newCoordinates = LatLng(task.latitude, task.longitude)
                if (isCoordinatesFar(previousCoordinates, newCoordinates)) {
                    try {
                        trySend(
                            LocationState.LocationGranted(
                                location = newCoordinates
                            )
                        )
                        previousCoordinates = newCoordinates
                    } catch (e: Exception) {
                        Log.d("MAP", "GPS is off")



                        /*Toast.makeText(context, R.string.cant_get_current_location, Toast.LENGTH_SHORT)
                            .show()*/

                    }
                }
            }


        }
    } else {
        trySend(LocationState.NoPermission)
    }
    awaitClose { }
}

fun checkGPSEnabled(context: Context) {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER).not()) {
        SnackbarManager.showMessage(R.string.gps_is_off)
        turnOnGPS(context)
    } else SnackbarManager.showMessage(R.string.unable_to_get_location)
}

private fun turnOnGPS(context: Context) {
    val request = LocationRequest.create().apply {
        interval = 8000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
    val client: SettingsClient = LocationServices.getSettingsClient(context as MainActivity)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            // Location settings are not satisfied, but this can be fixed
            // by showing the user a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                exception.startResolutionForResult(
                    context as MainActivity,
                    /*REQUEST_CHECK_SETTINGS*/12345
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error.
            }
        }
    }.addOnSuccessListener {
        //here GPS is On
    }
}

@ExperimentalPermissionsApi
fun getCurrentLocation(
    context: Context,
    permissionsState: MultiplePermissionsState,
) = runBlocking {
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    checkPermission(context)

    val result = mutableStateOf(LatLng(0.0, 0.0))

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnSuccessListener { task ->

            try {
                result.value = LatLng(task.latitude, task.longitude)

            } catch (e: Exception) {
                Log.d("MAP", "Unable to get location")
                Toast.makeText(context, R.string.cant_get_current_location, Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
    result
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
    val context = LocalContext.current
    val exitString = stringResource(R.string.app_exit_message)
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
                        showToast(context, exitString)
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
    val mapView: MapView = remember(isSystemInDarkTheme()) {
        MapView(
            context,
            when (isDarkMode) {
                true -> {
                    GoogleMapOptions().mapId(context.resources.getString(R.string.dark_map_id))
                }
                false -> {
                    GoogleMapOptions().mapId(context.resources.getString(R.string.light_map_id))
                }
            }
        )
    }
        .apply { id = R.id.map }


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

fun convertDistance(distanceInMeters: Double): String {
    return when (distanceInMeters.toInt()) {
        in 0..999 -> distanceInMeters.toInt().toString() + " m"
        in 1001..9999 -> DistanceFormat.df.format(distanceInMeters / 1000f).toString() + " km"
        else -> distanceInMeters.div(1000).toInt().toString() + " km"
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