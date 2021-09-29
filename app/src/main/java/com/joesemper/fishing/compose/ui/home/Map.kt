package com.joesemper.fishing.compose.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.unit.Dp
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
import com.joesemper.fishing.domain.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mapView = rememberMapViewWithLifecycle()

    val viewModel: MapViewModel = getViewModel()

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val bottomSheetPlaceState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    var placeSelectMode: Boolean by remember {
        mutableStateOf(false)
    }

    val lastKnownLocation = remember {
        getCurrentLocation(context = context, permissionsState = permissionsState)
    }

    val currentMarker = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = bottomSheetPlaceState,
        sheetContent = {
            BottomSheetMarkerDialog(currentMarker.value)
        },
        drawerGesturesEnabled = true,
        sheetPeekHeight = 0.dp,
        floatingActionButton = {
            FubOnMap(
                placeSelectMode = placeSelectMode,
                state = bottomSheetPlaceState,
                onClick = {
                    when {
                        placeSelectMode -> {
                            coroutineScope.launch {
                                Toast.makeText(
                                    context,
                                    "Open add new place \nPlace select mode off",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            placeSelectMode = !placeSelectMode
                        }
                        else -> {
                            when {
                                bottomSheetPlaceState.bottomSheetState.isExpanded -> {
                                    coroutineScope.launch {
                                        Toast.makeText(
                                            context,
                                            "Open add new catch",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                else -> {
                                    placeSelectMode = !placeSelectMode
                                    moveCameraToLocation(
                                        coroutineScope = coroutineScope,
                                        map = mapView,
                                        location = lastKnownLocation.value
                                    )
                                    coroutineScope.launch {
                                        Toast.makeText(
                                            context,
                                            "Place select mode on",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                })
        },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (permissionDialog, mapLayout, pointer, bottomSheet) = createRefs()

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
                map = mapView,
                permissionsState = permissionsState,
                viewModel = viewModel,
                onMarkerClick = { marker ->
                    currentMarker.value = marker
                    coroutineScope.launch {
                        bottomSheetPlaceState.bottomSheetState.expand()
                        moveCameraToLocation(
                            location = LatLng(marker.latitude, marker.longitude),
                            coroutineScope = coroutineScope,
                            map = mapView
                        )
                    }

                }
            )

            if (placeSelectMode) {
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


@ExperimentalMaterialApi
@Composable
fun BottomSheetMarkerDialog(marker: UserMapMarker?) {
    Spacer(modifier = Modifier.size(1.dp))
    marker?.let {

        ConstraintLayout() {
            val (locationIcon, title, description, navigateButton, detailsButton) = createRefs()

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
                image = painterResource(id = R.drawable.ic_baseline_navigation_24),
                name = stringResource(id = R.string.navigate),
                click = { /*TODO*/ },
                modifier = Modifier.constrainAs(navigateButton) {
                    absoluteLeft.linkTo(locationIcon.absoluteLeft)
                    top.linkTo(description.bottom, 8.dp)
                    bottom.linkTo(parent.bottom, 8.dp)
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

@ExperimentalPermissionsApi
@Composable
fun GoogleMapLayout(
    modifier: Modifier,
    map: MapView,
    viewModel: MapViewModel,
    permissionsState: MultiplePermissionsState,
    onMarkerClick: (marker: UserMapMarker) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val markers = viewModel.getAllMarkers().collectAsState()

    AndroidView(
        { map },
        modifier = modifier.zIndex(-1.0f)
    ) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()
            markers.value.forEach {
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
        checkPermission(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        googleMap.setOnMarkerClickListener { marker ->
            val mapMarker = markers.value.first { it.id == marker.tag }
            onMarkerClick(mapMarker)
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

@ExperimentalMaterialApi
@Composable
fun FubOnMap(state: BottomSheetScaffoldState, placeSelectMode: Boolean, onClick: () -> Unit) {

    val fabImg = painterResource(
        id =
        when {
            placeSelectMode -> R.drawable.ic_baseline_check_24
            state.bottomSheetState.isExpanded -> R.drawable.ic_add_catch
            else -> R.drawable.ic_baseline_add_location_24
        }
    )

    val padding: Dp by animateDpAsState(
        targetValue =
        when {
            state.bottomSheetState.isExpanded -> 0.dp
            state.bottomSheetState.isCollapsed -> 128.dp
            else -> 128.dp
        }
    )
    FloatingActionButton(
        modifier = Modifier
            .padding(
                bottom = padding
            ),
        onClick = onClick
    ) {
        Icon(
            painter = fabImg,
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

@ExperimentalPermissionsApi
fun getCurrentLocation(context: Context, permissionsState: MultiplePermissionsState) = runBlocking {
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    checkPermission(context)

    val result = mutableStateOf(LatLng(0.0, 0.0))

    if (permissionsState.allPermissionsGranted) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result.value = LatLng(task.result.latitude, task.result.longitude)
            }
        }
    }
    result
}

fun moveCameraToLocation(coroutineScope: CoroutineScope, map: MapView, location: LatLng) {
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

fun checkPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
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



