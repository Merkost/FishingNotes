package com.joesemper.fishing.compose.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.launch

@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MapViewContainer(mapView)
    }
}

@ExperimentalPermissionsApi
@Composable
fun MapViewContainer(
    map: MapView,
) {
    Box(contentAlignment = Alignment.Center) {
        val context = LocalContext.current

        val coroutineScope = rememberCoroutineScope()

        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

//    if (permissionsState.permissionRequested) {
        PermissionDialog(permissionsState)
//    }

        val lastKnownLocation = remember {
            mutableStateOf(LatLng(0.0, 0.0))
        }

        if (permissionsState.allPermissionsGranted) {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    lastKnownLocation.value = LatLng(task.result.latitude, task.result.longitude)
                }
            }
        }

        AndroidView(
            { map },
            modifier = Modifier.zIndex(-0.1f)
        ) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()
                googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation.value, 5f))
            }
        }

        LaunchedEffect(map) {
            val googleMap = map.awaitMap()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation.value, 5f))
        }

        ConstraintLayout(modifier = Modifier.matchParentSize()) {
            val (fab, pointer) = createRefs()

            val placeSelectMode = remember {
                mutableStateOf(false)
            }

            fun moveCameraToCurrentLocation() {
                coroutineScope.launch {
                    val googleMap = map.awaitMap()
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            lastKnownLocation.value,
                            10f
                        )
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (placeSelectMode.value) {
                        coroutineScope.launch {
                            val googleMap = map.awaitMap()
                            val lat = googleMap.cameraPosition.target.latitude
                            val lon = googleMap.cameraPosition.target.longitude
                            googleMap.cameraPosition.target
                            googleMap.addMarker(MarkerOptions().position(LatLng(lat, lon)))
                        }
                    } else {
                        moveCameraToCurrentLocation()
                    }

                    placeSelectMode.value = !placeSelectMode.value
                }, modifier = Modifier.constrainAs(fab) {
                    bottom.linkTo(parent.bottom, margin = 24.dp)
                    absoluteRight.linkTo(parent.end, margin = 24.dp)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (placeSelectMode.value) R.drawable.ic_baseline_check_24
                        else R.drawable.ic_baseline_add_location_24
                    ),
                    contentDescription = "Add new location",
                    tint = Color.White,
                )
            }

            if (placeSelectMode.value) {
                Icon(
                    modifier = Modifier
                        .constrainAs(pointer) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        }
                        .height(64.dp)
                        .width(64.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "pointer",
                    tint = secondaryFigmaColor,
                )
            }
        }

        fun checkPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        }

    }

}

@ExperimentalPermissionsApi
@Composable
fun PermissionDialog(permissionsState: MultiplePermissionsState) {
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
                    .zIndex(1.0f)

            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(8.dp)
                ) {
                    Text("The location is important for this app. \nPlease grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                            Text("Ok!")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

        },
        permissionsNotAvailableContent = {
            Text("The location is not available")
        }) {
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

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

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
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

val locationPermissionsList = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)



