package com.mobileprism.fishing.ui.home.map

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.*
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.utils.UserHandler
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.MapViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID
import com.mobileprism.fishing.utils.Constants.defaultFabBottomPadding
import com.mobileprism.fishing.utils.displayAppDetailsSettings
import com.mobileprism.fishing.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalLayoutApi::class)
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
    SideEffect {
        viewModel.setPlace(place)
        viewModel.setAddingPlace(addPlaceOnStart)
    }
    val context = LocalContext.current

    val mapUiState by viewModel.mapUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val userPreferences: UserPreferences = get()
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    // FIXME: Opened sheet on start
    val scaffoldState = rememberBottomSheetScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var newPlaceDialog by remember { mutableStateOf(false) }
    var mapLayersSelection by rememberSaveable { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()


    BackPressHandler(
        mapUiState = mapUiState,
        navController = navController,
        onBackPressedCallback = viewModel::resetMapUiState,
        upPress = { (context as MainActivity).finishAffinity() },
    )

    ModalBottomSheetLayout(
        modifier = Modifier,
        sheetState = modalBottomSheetState,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetContent = {
            MapModalBottomSheet(mapPreferences = userPreferences)
        }
    ) {
        MapScaffold(
            modifier = modifier.consumedWindowInsets(WindowInsets.statusBars).background(Color.Red),
            mapUiState = mapUiState,
            scaffoldState = scaffoldState,
            fab = {
                MapFab(
                    viewModel = viewModel,
                    onClick = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> viewModel.setPlaceSelectionMode()
                            MapUiState.PlaceSelectMode -> {
                                newPlaceDialog = true
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
                    onMarkerIconClicked = viewModel::onMarkerClicked
                ) { coroutineScope.launch { scaffoldState.bottomSheetState.collapse() } }
            }
        ) { paddingValues ->
            ConstraintLayout(modifier = Modifier.fillMaxSize().padding(paddingValues).statusBarsPadding()) {
                val (mapLayout, addMarkerFragment, mapMyLocationButton,
                    mapCompassButton, mapLayersButton, zoomInButton, zoomOutButton,
                    mapSettingsButton, mapLayersView, pointer) = createRefs()
                val verticalMyLocationButtonGl = createGuidelineFromAbsoluteRight(56.dp)
                val centerHorizontal = createGuidelineFromBottom(0.5f)

                MapLayout(
                    modifier = Modifier.constrainAs(mapLayout) { centerTo(parent) },
                    cameraPositionState = cameraPositionState
                )

                Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars), contentAlignment = Alignment.TopEnd) {
                    MyLocationButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = viewModel::onMyLocationClick
                    )
                }

                MapLayersButton(
                    modifier = Modifier
                        .constrainAs(mapLayersButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }.windowInsetsPadding(WindowInsets.statusBars)) { mapLayersSelection = true }

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
                    LayersView(
                        viewModel.mapType.collectAsState(),
                        onLayerSelected = viewModel::onLayerSelected
                    ) { mapLayersSelection = false }
                }

                MapSettingsButton(
                    modifier = Modifier
                        .constrainAs(mapSettingsButton) {
                        top.linkTo(mapLayersButton.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }.windowInsetsPadding(WindowInsets.statusBars)) { onMapSettingsClicked(coroutineScope, modalBottomSheetState) }



                CompassButton(
                    modifier = modifier.constrainAs(mapCompassButton) {
                        top.linkTo(mapMyLocationButton.bottom, 16.dp)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    }, mapBearing = viewModel.mapBearing.collectAsState(),
                    onClick = viewModel::resetMapBearing
                )

                if (useZoomButtons) {
                    MapZoomInButton(
                        modifier = Modifier.constrainAs(zoomInButton) {
                            linkTo(parent.top, centerHorizontal, 4.dp, 4.dp, bias = 1f)
                            linkTo(
                                parent.absoluteLeft,
                                parent.absoluteRight,
                                16.dp,
                                16.dp,
                                bias = 1f
                            )
                        }, onClick = {
                            coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                        }
                    )

                    MapZoomOutButton(
                        modifier = Modifier.constrainAs(zoomOutButton) {
                            linkTo(centerHorizontal, parent.bottom, 4.dp, 4.dp, bias = 0f)
                            linkTo(
                                parent.absoluteLeft,
                                parent.absoluteRight,
                                16.dp,
                                16.dp,
                                bias = 1f
                            )
                        }, onClick = {
                            coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                        }
                    )
                }

                AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode,
                    enter = fadeIn(), exit = fadeOut(),
                    modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom, 65.dp)
                        centerHorizontallyTo(parent)
                    }) { PointerIcon(viewModel.placeTileViewNameState.collectAsState().value.pointerState) }

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
                    )
                }

                NewPlaceDialog(dialogState = newPlaceDialog) { newPlaceDialog = false }
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

@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("PotentialBehaviorOverride", "MissingPermission")
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
) {
    val viewModel: MapViewModel = getViewModel()
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val userPreferences: UserPreferences = get()
    val darkTheme = isSystemInDarkTheme()

    val showHiddenPlaces by userPreferences.shouldShowHiddenPlacesOnMap.collectAsState(true)
    val mapType by viewModel.mapType.collectAsState()
    val context = LocalContext.current
    val markers by viewModel.mapMarkers.collectAsState()

    val markersToShow by remember(markers, showHiddenPlaces) {
        mutableStateOf(if (showHiddenPlaces) markers
        else markers.filter { it.visible })
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        )
    }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = permissionsState.allPermissionsGranted,
                //Map styles: https://mapstyle.withgoogle.com
                mapStyleOptions = context.getMapStyleByTheme(darkTheme),
                mapType = MapType.values()[mapType]
            )
        )
    }

    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .zIndex(-1.0f),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId(
                when (darkTheme) {
                    true -> context.resources.getString(R.string.dark_map_id)
                    false -> context.resources.getString(R.string.light_map_id)
                }
            )
        },
        uiSettings = mapUiSettings,
        properties = mapProperties,
        onMapClick = { viewModel.resetMapUiState() },
    ) {

        markersToShow.forEach { myMarker ->
            Marker(
                state = MarkerState(position = LatLng(myMarker.latitude, myMarker.longitude)),
                title = myMarker.title,
                icon = BitmapDescriptorFactory.defaultMarker(
                    getHueFromColor(Color(myMarker.markerColor))
                ),
                tag = myMarker.id,
                onClick = {
                    viewModel.onMarkerClicked(myMarker)
                }
            )
        }

        MapEffect(Unit) {map ->
            launch {
                viewModel.newMapCameraPosition.collectLatest {
                    map.awaitMapLoad()
                    moveCameraToLocation(cameraPositionState, it.first, it.second, it.third)
                }
            }

            launch {
                viewModel.firstCameraPosition.collectLatest {
                    map.awaitMapLoad()
                    it?.let { setCameraPosition(cameraPositionState, it.first, it.second, it.third) }
                }
            }
        }

    }
    /*AndroidView(
        { map },
        modifier = modifier
            .fillMaxSize()
            .zIndex(-1.0f)
    ) { mapView ->
        coroutineScope.launch {
            val googleMap = mapView.awaitMap()



            googleMap.clear()

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

        }
    }*/



    LaunchedEffect(Unit, permissionsState.allPermissionsGranted) {
        checkLocationPermissions(context)
        mapProperties =
            mapProperties.copy(isMyLocationEnabled = permissionsState.allPermissionsGranted)
    }



    LaunchedEffect(mapType) {
        mapProperties = mapProperties.copy(mapType = MapType.values()[mapType])
    }
/*
    DisposableEffect(map) {
        isMapVisible = true
        viewModel.getLastLocation()

        onDispose { viewModel.saveLastCameraPosition() }
    }*/
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    onCloseCallback: () -> Unit,
) {
    val context = LocalContext.current
    val userPreferences: UserPreferences = get()
    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    if (permissionsState.allPermissionsGranted.not()) {
        GrantLocationPermissionsDialog(
            onDismiss = { onCloseCallback() },
            onNegativeClick = { onCloseCallback() },
            onPositiveClick = {
                if (permissionsState.shouldShowRationale) {
                    context.displayAppDetailsSettings()
                    context.showToast(context.getString(R.string.enable_gps_in_settings))
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
                onCloseCallback()
            },
            onDontAskClick = {
                SnackbarManager.showMessage(R.string.location_dont_ask)
                coroutineScope.launch {
                    userPreferences.saveLocationPermissionStatus(false)
                }
                onCloseCallback()
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun MapFab(
    viewModel: MapViewModel,
    userSettings: UserPreferences,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
) {
    val state by viewModel.mapUiState.collectAsState()
    val useFastFabAdd by userSettings.useFabFastAdd.collectAsState(false)

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
                .windowInsetsPadding(WindowInsets.navigationBars),
                //.padding(bottom = paddingBottom.value, top = paddingTop.value),
            onClick = onClick,
            onLongPress = {
                if (state == MapUiState.NormalMode && useFastFabAdd) {
                    if (!checkLocationPermissions(context)) {
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
        if (it.id != CURRENT_PLACE_ITEM_ID) {
            navController.navigate(
                MainDestinations.NEW_CATCH_ROUTE,
                Arguments.PLACE to it
            )
        } else {
            // TODO: Нельзя добавить улов на текущее местоположение
        }

    }
}

private fun onMarkerDetailsClick(navController: NavController, marker: UserMapMarker) {
    navController.navigate(MainDestinations.PLACE_ROUTE, Arguments.PLACE to marker)
}