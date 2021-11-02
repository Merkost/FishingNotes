package com.joesemper.fishing.compose.ui.home.map

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.DefaultCard
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    addPlaceOnStart: Boolean = false
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState()
    val dialogAddPlaceIsShowing = remember { mutableStateOf(false) }

    var mapUiState: MapUiState by remember {
        if (addPlaceOnStart) mutableStateOf(MapUiState.PlaceSelectMode)
        else mutableStateOf(viewModel.mapUiState)
    }

    LaunchedEffect(mapUiState) {
        viewModel.mapUiState = mapUiState

        when (mapUiState) {
            MapUiState.NormalMode -> {
                scaffoldState.bottomSheetState.collapse()
                viewModel.currentMarker.value = null
            }
            MapUiState.BottomSheetInfoMode -> {
                scaffoldState.bottomSheetState.expand()
            }
            MapUiState.PlaceSelectMode -> {

            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        mapUiState = when (scaffoldState.bottomSheetState.currentValue) {
            BottomSheetValue.Collapsed -> MapUiState.NormalMode
            BottomSheetValue.Expanded -> MapUiState.BottomSheetInfoMode
        }
    }

    BackPressHandler(mapUiState = mapUiState) {
        mapUiState = MapUiState.NormalMode
    }

    MapScaffold(
        scaffoldState = scaffoldState,
        fab = {
            MapFab(
                state = mapUiState,
                onClick = {
                    when (mapUiState) {
                        MapUiState.NormalMode -> {
                            mapUiState = MapUiState.PlaceSelectMode
                        }
                        MapUiState.PlaceSelectMode -> {
                            dialogAddPlaceIsShowing.value = true
                            mapUiState = MapUiState.NormalMode
                        }
                        MapUiState.BottomSheetInfoMode -> {
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.collapse()
                            }
                            onAddNewCatchClick(navController, viewModel)
                        }
                    }
                })
        },
        bottomSheet = {
            BottomSheetMarkerInfoDialog(viewModel.currentMarker.value) { marker ->
                coroutineScope.launch {
                    scaffoldState.bottomSheetState.collapse()
                }
                onMarkerDetailsClick(navController, marker)
            }
        }
    ) {

        LocationPermissionDialog()

        MapLayout(
            onMarkerClick = {
                viewModel.currentMarker.value = it
                mapUiState = MapUiState.BottomSheetInfoMode
            },
            cameraMoveCallback = {

            }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    cameraMoveCallback: (state: CameraMoveState) -> Unit

) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val markers = viewModel.getAllMarkers().collectAsState()
    val map = rememberMapViewWithLifecycle()
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    var lastCameraPosition by remember() {
        mutableStateOf(Pair(LatLng(0.0, 0.0), DEFAULT_ZOOM))
    }

    var isMapVisible by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(visible = isMapVisible) {
        AndroidView(
            { map },
            modifier = modifier
                .fillMaxSize()
                .zIndex(-1.0f)
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
                googleMap.setOnCameraMoveStartedListener {
                    cameraMoveCallback(CameraMoveState.MoveStart)
                }
                googleMap.setOnCameraIdleListener {
                    cameraMoveCallback(CameraMoveState.MoveFinish)
                    lastCameraPosition =
                        Pair(googleMap.cameraPosition.target, googleMap.cameraPosition.zoom)
                }
                googleMap.setOnMarkerClickListener { marker ->
                    onMarkerClick(markers.value.first { it.id == marker.tag })
                    viewModel.lastMapCameraPosition.value = Pair(marker.position, DEFAULT_ZOOM)
                    true
                }
                googleMap.uiSettings.isMyLocationButtonEnabled = false

                getCurrentLocationFlow(context, permissionsState).collect { state ->
                    if (state is LocationState.LocationGranted) {
                        viewModel.lastKnownLocation.value = state.location
                        if (viewModel.firstLaunchLocation.value) {
                            viewModel.lastMapCameraPosition.value =
                                Pair(state.location, DEFAULT_ZOOM)
                            viewModel.firstLaunchLocation.value = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(map, permissionsState.allPermissionsGranted) {
        val googleMap = map.awaitMap()
        viewModel.lastMapCameraPosition.value?.let {
            setCameraPosition(this, map, it.first, it.second)
            viewModel.lastMapCameraPosition.value = null
        }
        checkPermission(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        isMapVisible = true
    }

    LaunchedEffect(viewModel.lastMapCameraPosition.value) {
        viewModel.lastMapCameraPosition.value?.let {
            moveCameraToLocation(this, map, it.first, it.second)
        }
    }

    DisposableEffect(map) {
        onDispose {
            viewModel.lastMapCameraPosition.value = lastCameraPosition
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDialogOpen by remember {
        mutableStateOf(true)
    }
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isDialogOpen) {
                GrantPermissionsDialog(
                    onDismiss = {
                        isDialogOpen = false
                    },
                    onNegativeClick = {
                        isDialogOpen = false
                    },
                    onPositiveClick = {
                        isDialogOpen = false
                        permissionsState.launchMultiplePermissionRequest()
                    }
                )
            }
        },
        permissionsNotAvailableContent = {
            Toast.makeText(
                context,
                "Your current location is not available",
                Toast.LENGTH_SHORT
            ).show()
        })
    { checkPermission(context) }
}

@ExperimentalMaterialApi
@Composable
fun MapFab(state: MapUiState, onClick: () -> Unit) {
    val fabImg = remember { mutableStateOf(R.drawable.ic_baseline_add_location_24) }
    val defaultBottomPadding: Dp = 128.dp
    val paddingBottom = remember { mutableStateOf(defaultBottomPadding) } //128
    val paddingTop = remember { mutableStateOf(0.dp) }

    when (state) {
        MapUiState.NormalMode -> {
            fabImg.value = R.drawable.ic_baseline_add_location_24
            paddingBottom.value = defaultBottomPadding
            paddingTop.value = 0.dp
        }
        MapUiState.BottomSheetInfoMode -> {
            fabImg.value = R.drawable.ic_add_catch
            paddingBottom.value = 8.dp
            paddingTop.value = 16.dp
        }
        MapUiState.PlaceSelectMode -> {
            fabImg.value = R.drawable.ic_baseline_check_24
            paddingBottom.value = defaultBottomPadding
            paddingTop.value = 0.dp
        }
    }

    FloatingActionButton(
        modifier = Modifier
            .animateContentSize()
            .padding(bottom = paddingBottom.value, top = paddingTop.value),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = fabImg.value),
            contentDescription = "Add new location",
            tint = Color.White,
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun BottomSheetMarkerInfoDialog(
    marker: UserMapMarker?,
    onDescriptionClick: (UserMapMarker) -> Unit
) {

    Spacer(modifier = Modifier.size(6.dp))
    DefaultCard() {
        marker?.let {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()

            ) {
                val (line, locationIcon, title, description, navigateButton, detailsButton) = createRefs()
                BottomSheetLine(modifier = Modifier.constrainAs(line) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(parent.top, 1.dp)
                })

                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "Marker",
                    tint = secondaryFigmaColor,
                    modifier = Modifier
                        .size(32.dp)
                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                            top.linkTo(title.top)
                            bottom.linkTo(title.bottom)
                        }
                )

                Text(
                    text = marker.title,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(end = 56.dp)
                        .constrainAs(title) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteLeft.linkTo(locationIcon.absoluteRight, 8.dp)
                        }
                )

                Text(
                    text = if (marker.description.isEmpty()) {
                        "No description"
                    } else {
                        marker.description
                    },
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.constrainAs(description) {
                        absoluteLeft.linkTo(title.absoluteLeft)
                        top.linkTo(title.bottom, 4.dp)
                    }
                )

                Button(modifier = Modifier.constrainAs(detailsButton) {
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    top.linkTo(description.bottom, 8.dp)
                    bottom.linkTo(parent.bottom, 16.dp)
                },
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        onDescriptionClick(marker)
                    }
                ) {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_shortcut_24),
                            "",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            stringResource(id = R.string.details),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                OutlinedButton(modifier = Modifier.constrainAs(navigateButton) {
                    absoluteRight.linkTo(detailsButton.absoluteLeft, 8.dp)
                    top.linkTo(detailsButton.top)
                    bottom.linkTo(detailsButton.bottom)
                }, shape = RoundedCornerShape(24.dp), onClick = { /*TODO*/ }) {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_navigation_24),
                            "",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            stringResource(id = R.string.navigate),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

    }
}

private fun onAddNewCatchClick(navController: NavController, viewModel: MapViewModel) {
    val marker: UserMapMarker? = viewModel.currentMarker.value
    marker?.let {
        navController.navigate(
            MainDestinations.NEW_CATCH_ROUTE,
            Arguments.PLACE to it
        )
    }
}

private fun onMarkerDetailsClick(navController: NavController, marker: UserMapMarker) {
    navController.navigate(MainDestinations.PLACE_ROUTE, Arguments.PLACE to marker)
}




















