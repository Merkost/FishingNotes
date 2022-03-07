package com.mobileprism.fishing.ui.home.map

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.maps.android.ktx.awaitMap
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.viewmodels.MapViewModel
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.defaultFabBottomPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    addPlaceOnStart: Boolean = false,
    place: UserMapMarker?,
    upPress: () -> Unit,
) {
    val viewModel: MapViewModel = getViewModel()
    viewModel.setPlace(place)
    viewModel.setAddingPlace(addPlaceOnStart)
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current

    val map = rememberMapViewWithLifecycle()
    val mapUiState by viewModel.mapUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val userPreferences: UserPreferences = get()
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    val scaffoldState = rememberBottomSheetScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val dialogAddPlaceIsShowing = remember { mutableStateOf(false) }
    var mapLayersSelection by rememberSaveable { mutableStateOf(false) }

    val pointerState: MutableState<PointerState> = remember {
        mutableStateOf(PointerState.HideMarker)
    }

    val currentLocationFlow = remember(permissionsState.allPermissionsGranted) {
        getCurrentLocationFlow(context, permissionsState)
    }

    LaunchedEffect(currentLocationFlow) {
        currentLocationFlow.collect { currentLocationState ->
            when (currentLocationState) {
                is LocationState.LocationGranted -> {
                    viewModel.locationGranted(currentLocationState.location)
                }
                is LocationState.GpsNotEnabled -> {
                    checkGPSEnabled(context) //{ currentLocationFlow.collectAsState() }
                }
                LocationState.LocationNotGranted -> {}
                LocationState.NoPermission -> {}
            }
        }
    }

    BackPressHandler(
        mapUiState = mapUiState,
        navController = navController,
        onBackPressedCallback = viewModel::resetMapUiState
    )

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetContent = {
            MapModalBottomSheet(mapPreferences = userPreferences)
        }
    ) {
        MapScaffold(
            mapUiState = mapUiState,
            scaffoldState = scaffoldState,
            fab = {
                MapFab(
                    state = mapUiState,
                    onClick = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> viewModel.setPlaceSelectionMode()
                            MapUiState.PlaceSelectMode -> {
                                dialogAddPlaceIsShowing.value = true
                                viewModel.resetMapUiState()
                            }
                            MapUiState.BottomSheetInfoMode -> {
                                onAddNewCatchClick(navController, viewModel)
                            }
                        }
                    },
                    onLongPress = { viewModel.quickAddPlace(name = context.getString(R.string.no_name_place)) },
                    userSettings = userPreferences,
                )
            },
            bottomSheet = {
                MarkerInfoDialog(
                    navController = navController,
                    onMarkerIconClicked = {
                        viewModel.onMarkerClicked(
                            it,
                            LatLng(it.latitude, it.longitude)
                        )
                    }
                )
            }
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (mapLayout, addMarkerFragment, mapMyLocationButton,
                    mapCompassButton, mapLayersButton, zoomInButton, zoomOutButton,
                    mapSettingsButton, mapLayersView, pointer) = createRefs()
                val verticalMyLocationButtonGl = createGuidelineFromAbsoluteRight(56.dp)
                val centerHorizontal = createGuidelineFromBottom(0.5f)

                MapLayout(
                    modifier = Modifier.constrainAs(mapLayout) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    map = map,
                )

                MapLayersButton(
                    modifier = Modifier.constrainAs(mapLayersButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }
                ) { mapLayersSelection = true }

                if (mapLayersSelection)
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .zIndex(4f)
                        .clickable { mapLayersSelection = false }) {}
                AnimatedVisibility(
                    mapLayersSelection,
                    enter = expandIn(
                        expandFrom = Alignment.TopStart,
                        animationSpec = tween(380)
                    ) + fadeIn(animationSpec = tween(480)),
                    exit = shrinkOut(
                        shrinkTowards = Alignment.TopStart,
                        animationSpec = tween(380)
                    ) + fadeOut(animationSpec = tween(480)),
                    modifier = Modifier
                        .constrainAs(mapLayersView) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                        }
                        .zIndex(5f)
                ) {
                    LayersView(viewModel.mapType.collectAsState(),
                    onLayerSelected = viewModel::onLayerSelected) {
                        mapLayersSelection = false
                    }
                }

                MapSettingsButton(
                    modifier = Modifier.constrainAs(mapSettingsButton) {
                        top.linkTo(mapLayersButton.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }) { onMapSettingsClicked(coroutineScope, modalBottomSheetState) }

                MyLocationButton(
                    modifier = modifier.constrainAs(mapMyLocationButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    },
                    userPreferences = userPreferences,
                    onClick = viewModel::onMyLocationClick
                )

                CompassButton(
                    modifier = modifier.constrainAs(mapCompassButton) {
                    top.linkTo(mapMyLocationButton.bottom, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                }, mapBearing = viewModel.mapBearing.collectAsState(),
                    onClick = viewModel::resetMapBearing)

                if (useZoomButtons) {
                    MapZoomInButton(
                        modifier = Modifier.constrainAs(zoomInButton) {
                            linkTo(parent.top, centerHorizontal, 4.dp, 4.dp, 1f)
                            linkTo(parent.absoluteLeft, parent.absoluteRight, 16.dp, 16.dp, 1f)
                        }, onClick = viewModel::onZoomInClick
                    )

                    MapZoomOutButton(
                        modifier = Modifier.constrainAs(zoomOutButton) {
                            linkTo(centerHorizontal, parent.bottom, 4.dp, 4.dp, 0f)
                            linkTo(parent.absoluteLeft, parent.absoluteRight, 16.dp, 16.dp, 1f)
                        }, onClick = viewModel::onZoomOutClick
                    )
                }

                AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode,
                    enter = fadeIn(), exit = fadeOut(),
                    modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom, 65.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }) {
                    PointerIcon(pointerState)
                }

                AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.constrainAs(addMarkerFragment) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(mapLayersButton.absoluteRight, 8.dp)
                        absoluteRight.linkTo(verticalMyLocationButtonGl, 8.dp)
                    }
                ) {
                    PlaceTileView(
                        modifier = Modifier.wrapContentSize(),
                        currentCameraPosition = viewModel.currentCameraPosition.collectAsState(),
                        pointerState = pointerState
                    )
                }

                if (dialogAddPlaceIsShowing.value)
                    Dialog(onDismissRequest = { dialogAddPlaceIsShowing.value = false }) {
                        NewPlaceDialog(
                            currentCameraPosition = viewModel.currentCameraPosition.collectAsState(),
                            dialogState = dialogAddPlaceIsShowing,
                            chosenPlace = viewModel.chosenPlace
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun onMapSettingsClicked(
    coroutineScope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    coroutineScope.launch {
        Firebase.analytics.logEvent("map_settings", null)
        modalBottomSheetState.show()
    }
}

@SuppressLint("PotentialBehaviorOverride")
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    map: MapView,
) {
    val viewModel: MapViewModel = getViewModel()
    val userPreferences: UserPreferences = get()
    val coroutineScope = rememberCoroutineScope()
    val darkTheme = isSystemInDarkTheme()

    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val mapType by viewModel.mapType.collectAsState()
    val context = LocalContext.current
    val markers by viewModel.mapMarkers.collectAsState()

    val markersToShow by remember(markers, showHiddenPlaces) {
        mutableStateOf(if (showHiddenPlaces) markers
        else markers.filter { it.visible })
    }

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    var isMapVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isMapVisible,
        enter = fadeIn(), exit = fadeOut()
    ) {
        AndroidView(
            { map },
            modifier = modifier
                .fillMaxSize()
                .zIndex(-1.0f)
        ) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()

                //Map styles: https://mapstyle.withgoogle.com
                if (darkTheme) {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style_fishing_night
                        )
                    )
                } else {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style_fishing
                        )
                    )
                }

                googleMap.clear()
                markersToShow.forEach {
                    val position = LatLng(it.latitude, it.longitude)
                    val markerColor = Color(it.markerColor)
                    val hue = getHue(
                        red = markerColor.red,
                        green = markerColor.green,
                        blue = markerColor.blue
                    )
                    val marker = googleMap
                        .addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(it.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))

                        )
                    marker?.tag = it.id
                }
                googleMap.setOnCameraMoveStartedListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveStart)
                }
                googleMap.setOnCameraMoveListener {
                    viewModel.onCameraMove(
                        googleMap.cameraPosition.target,
                        googleMap.cameraPosition.zoom,
                        googleMap.cameraPosition.bearing
                    )
                }
                googleMap.setOnCameraIdleListener {
                    viewModel.setCameraMoveState(CameraMoveState.MoveFinish)
                    viewModel.saveLastCameraPosition()
                }
                googleMap.setOnMarkerClickListener { marker ->
                    viewModel.onMarkerClicked(
                        markers.firstOrNull { it.id == marker.tag },
                        marker.position
                    )
                    true
                }
                googleMap.setOnMapClickListener {
                    viewModel.resetMapUiState()
                    return@setOnMapClickListener
                }

                googleMap.uiSettings.isCompassEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }

    LaunchedEffect(map, permissionsState.allPermissionsGranted) {
        val googleMap = map.awaitMap()
        checkPermission(context)
        googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        isMapVisible = true
    }

    LaunchedEffect(Unit) {
        viewModel.newMapCameraPosition.collectLatest {
            moveCameraToLocation(this, map, it.first, it.second, it.third)
        }
    }

    LaunchedEffect(mapType) {
        val googleMap = map.awaitMap()
        googleMap.mapType = mapType
    }

    DisposableEffect(map, ) {
        viewModel.lastMapCameraPosition.value?.let {
            setCameraPosition(coroutineScope, map, it.first, it.second)
        } ?: viewModel.getFirstLaunchLocation()

        onDispose {
            viewModel.saveLastCameraPosition()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onCloseCallback: () -> Unit = { },
) {
    val context = LocalContext.current
    var isDialogOpen by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isDialogOpen) {
                GrantLocationPermissionsDialog(
                    onDismiss = {
                        isDialogOpen = false
                        onCloseCallback()
                    },
                    onNegativeClick = {
                        isDialogOpen = false
                        onCloseCallback()
                    },
                    onPositiveClick = {
                        isDialogOpen = false
                        permissionsState.launchMultiplePermissionRequest()
                        onCloseCallback()
                    },
                    onDontAskClick = {
                        isDialogOpen = false
                        SnackbarManager.showMessage(R.string.location_dont_ask)
                        coroutineScope.launch {
                            userPreferences.saveLocationPermissionStatus(false)
                        }
                        onCloseCallback()
                    }
                )
            }
        },
        permissionsNotAvailableContent = { onCloseCallback(); SnackbarManager.showMessage(R.string.location_permission_denied) })
    { checkPermission(context); }
}

@ExperimentalMaterialApi
@Composable
fun MapFab(
    state: MapUiState,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    userSettings: UserPreferences,
) {
    val useFastFabAdd by userSettings.useFabFastAdd.collectAsState(false)
    val fabImg = remember { mutableStateOf(R.drawable.ic_baseline_add_location_24) }

    val context = LocalContext.current

    val paddingBottom = animateDpAsState(
        when (state) {
            MapUiState.NormalMode -> {
                defaultFabBottomPadding
            }
            MapUiState.BottomSheetInfoMode -> {
                34.dp
            }
            MapUiState.PlaceSelectMode -> {
                defaultFabBottomPadding
            }
        }
    )

    val paddingTop = animateDpAsState(
        when (state) {
            MapUiState.NormalMode -> {
                0.dp
            }
            MapUiState.BottomSheetInfoMode -> {
                26.dp
            }
            MapUiState.PlaceSelectMode -> {
                0.dp
            }
        }
    )

    val adding_place = stringResource(R.string.adding_place_on_current_location)
    val permissions_required = stringResource(R.string.location_permissions_required)
    AnimatedVisibility(
        true,
        exit = fadeOut(),
        enter = fadeIn()
    ) {

        FishingFab(
            modifier = Modifier
                .animateContentSize()
                .padding(bottom = paddingBottom.value, top = paddingTop.value),
            onClick = onClick,
            onLongPress = {
                if (state == MapUiState.NormalMode && useFastFabAdd) {
                    if (!checkPermission(context)) {
                        Toast.makeText(context, adding_place, Toast.LENGTH_SHORT).show()
                        onLongPress()
                    } else Toast.makeText(context, permissions_required, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        ) {
            AnimatedVisibility(state is MapUiState.NormalMode) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_location_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            AnimatedVisibility(state is MapUiState.BottomSheetInfoMode) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_catch),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            AnimatedVisibility(state is MapUiState.PlaceSelectMode) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_check_24),
                    contentDescription = stringResource(R.string.new_place),
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishingFab(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor.copy(alpha = 1f),
        elevation = elevation.elevation(interactionSource).value,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(MaterialTheme.typography.button) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = FabSize, minHeight = FabSize)
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            enabled = true,
                            role = Role.Button,
                            onClick = onClick,
                            onDoubleClick = { },
                            onLongClick = onLongPress
                        ),
                    contentAlignment = Alignment.Center
                ) { content() }
            }
        }
    }
}

private val FabSize = 56.dp

private fun onAddNewCatchClick(navController: NavController, viewModel: MapViewModel) {
    viewModel.currentMarker.value?.let {
        navController.navigate(
            MainDestinations.NEW_CATCH_ROUTE,
            Arguments.PLACE to it
        )
    }
}

private fun onMarkerDetailsClick(navController: NavController, marker: UserMapMarker) {
    navController.navigate(MainDestinations.PLACE_ROUTE, Arguments.PLACE to marker)
}