package com.joesemper.fishing.compose.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.google.android.gms.tasks.Task
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
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

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MapViewContainer(
    map: MapView,
) {
    val viewModel: MapViewModel = getViewModel()

    val markers = viewModel.getAllMarkers().collectAsState()

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val markerBottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    val placeSelectMode = remember {
        mutableStateOf(false)
    }

    val lastKnownLocation = remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }

    val currentMarker = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    fun updateLastKnownLocation(task: Task<Location>) {
        if (task.isSuccessful) {
            lastKnownLocation.value = LatLng(task.result.latitude, task.result.longitude)
            coroutineScope.launch {
                val googleMap = map.awaitMap()
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        lastKnownLocation.value,
                        DEFAULT_ZOOM
                    )
                )
            }
        }
    }

    fun enableMyLocationOnMap() {
        coroutineScope.launch {
            val googleMap = map.awaitMap()
            googleMap.isMyLocationEnabled = true
        }
    }

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener { task ->
            updateLastKnownLocation(task)
            enableMyLocationOnMap()
        }
    }

    fun setNewMarkerOnMap() {
        coroutineScope.launch {
            val googleMap = map.awaitMap()
            val lat = googleMap.cameraPosition.target.latitude
            val lon = googleMap.cameraPosition.target.longitude
            googleMap.cameraPosition.target
            viewModel.addNewMarker(
                RawMapMarker(
                    title = "Test Marker",
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }

    fun moveCameraToLocation(location: LatLng) {
        coroutineScope.launch {
            val googleMap = map.awaitMap()
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    DEFAULT_ZOOM
                )
            )
        }
    }

    fun moveCameraToCurrentLocation() {
        coroutineScope.launch {
            val googleMap = map.awaitMap()
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    lastKnownLocation.value,
                    DEFAULT_ZOOM
                )
            )
        }
    }

    fun onFabClick() {
        if (placeSelectMode.value) {
            coroutineScope.launch {
//                markerBottomSheetScaffoldState.bottomSheetState.expand()
            }
        } else {
            moveCameraToCurrentLocation()
        }
        placeSelectMode.value = !placeSelectMode.value
    }

    BottomSheetScaffold(
        scaffoldState = markerBottomSheetScaffoldState,
        sheetContent = {
            BottomSheetMarkerDialog(currentMarker.value)
        },
        sheetPeekHeight = 0.dp,
    ) {
        Scaffold(floatingActionButton = {
            FubAddNewPlace(
                placeSelectMode = placeSelectMode.value,
                onClick = { onFabClick() })
        }, floatingActionButtonPosition = FabPosition.End) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (permissionDialog, mapLayout, pointer) = createRefs()

                PermissionDialog(modifier = Modifier.constrainAs(permissionDialog) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                }, permissionsState = permissionsState)

                GoogleMapLayout(
                    modifier = Modifier.constrainAs(mapLayout) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    map = map,
                    coroutineScope = coroutineScope,
                    markers = markers.value,
                    onMarkerClick = { marker ->
                        currentMarker.value = markers.value.first { it.id == marker.tag }
                        coroutineScope.launch {
                            markerBottomSheetScaffoldState.bottomSheetState.expand()
                            moveCameraToLocation(marker.position)
                        }

                    }
                )

                if (placeSelectMode.value) {
                    PointerIcon(modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    })
                }
            }
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

@ExperimentalMaterialApi
@Composable
fun BottomSheetMarkerDialog(marker: UserMapMarker?) {
    marker?.let {

        ConstraintLayout() {
            val (locationIcon, title, description, newCatchButton, navigateButton, detailsButton) = createRefs()

            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = "Marker",
                tint = secondaryFigmaColor,
                modifier = Modifier
                    .size(32.dp)
                    .constrainAs(locationIcon) {
                        absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                        top.linkTo(parent.top, 8.dp)
                    }
            )

            Text(
                text = marker.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(locationIcon.top)
                    absoluteLeft.linkTo(locationIcon.absoluteRight, 8.dp)
                }
            )

            Text(
                text = if (marker.description.isNullOrEmpty()) {
                    "No description"
                } else {
                    marker.description!!
                },
                style = MaterialTheme.typography.body1,
                modifier = Modifier.constrainAs(description) {
                    absoluteLeft.linkTo(title.absoluteLeft)
                    top.linkTo(title.bottom, 4.dp)
                }
            )

            IconButton(
                image = painterResource(id = R.drawable.ic_add_catch),
                name = stringResource(id = R.string.add_new_catch),
                click = { /*TODO*/ },
                modifier = Modifier.constrainAs(newCatchButton) {
                    absoluteLeft.linkTo(locationIcon.absoluteLeft)
                    top.linkTo(description.bottom, 8.dp)
                    bottom.linkTo(parent.bottom, 8.dp)
                }
            )

            IconButton(
                image = painterResource(id = R.drawable.ic_baseline_navigation_24),
                name = stringResource(id = R.string.navigate),
                click = { /*TODO*/ },
                modifier = Modifier.constrainAs(navigateButton) {
                    absoluteLeft.linkTo(newCatchButton.absoluteRight, 8.dp)
                    top.linkTo(newCatchButton.top)
                }
            )

            IconButton(
                image = painterResource(id = R.drawable.ic_baseline_shortcut_24),
                name = stringResource(id = R.string.details),
                click = { /*TODO*/ },
                modifier = Modifier.constrainAs(detailsButton) {
                    absoluteLeft.linkTo(navigateButton.absoluteRight, 8.dp)
                    top.linkTo(navigateButton.top)
                }
            )
        }
    }
}

@Composable
fun GoogleMapLayout(
    modifier: Modifier,
    map: MapView,
    coroutineScope: CoroutineScope,
    markers: List<UserMapMarker>,
    onMarkerClick: (marker: Marker) -> Unit
) {
    AndroidView(
        { map },
        modifier = modifier.zIndex(-1.0f)
    ) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            markers.forEach {
                val position = LatLng(it.latitude, it.longitude)
                val marker = googleMap
                    .addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(it.title)
                    )
                marker.tag = it.id
            }
        }
    }

    LaunchedEffect(map) {
        val googleMap = map.awaitMap()
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
            true
        }
    }
}

@Composable
fun PointerIcon(modifier: Modifier) {
    Icon(
        modifier = modifier
            .height(64.dp)
            .width(64.dp),
        painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
        contentDescription = "pointer",
        tint = secondaryFigmaColor,
    )
}

@Composable
fun FubAddNewPlace(placeSelectMode: Boolean, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(
                id = if (placeSelectMode) R.drawable.ic_baseline_check_24
                else R.drawable.ic_baseline_add_location_24
            ),
            contentDescription = "Add new location",
            tint = Color.White,
        )
    }
}

@ExperimentalPermissionsApi
@Composable
fun PermissionDialog(modifier: Modifier, permissionsState: MultiplePermissionsState) {
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            Card(
                modifier = modifier
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

@Composable
fun IconButton(image: Painter, name: String, click: () -> Unit, modifier: Modifier) {
    OutlinedButton(
        onClick = click,
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp),
        content = {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(image, name, modifier = Modifier.size(25.dp))
                Text(name, modifier = Modifier.padding(start = 10.dp))
            }
        })
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

const val DEFAULT_ZOOM = 15f



