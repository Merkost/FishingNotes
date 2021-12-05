package com.joesemper.fishing.compose.ui.home.map

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.utils.getCameraPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
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
    addPlaceOnStart: Boolean = false
) {
    var addingPlace by remember { mutableStateOf(addPlaceOnStart) }

    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val userPreferences: UserPreferences = get()

    val scaffoldState = rememberBottomSheetScaffoldState()
    val dialogAddPlaceIsShowing = remember { mutableStateOf(false) }

    val mapLayersSelection = rememberSaveable { mutableStateOf(false) }
    val mapType = rememberSaveable { mutableStateOf(MapTypes.roadmap) }

    var mapUiState: MapUiState by remember {
        if (addPlaceOnStart) mutableStateOf(MapUiState.PlaceSelectMode)
        else mutableStateOf(viewModel.mapUiState)
    }

    var cameraMoveState: CameraMoveState by remember {
        mutableStateOf(CameraMoveState.MoveFinish)
    }

    val pointerState: MutableState<PointerState> = remember {
        mutableStateOf(PointerState.HideMarker)
    }

    val currentCameraPosition = remember {
        mutableStateOf(Pair(LatLng(0.0, 0.0), 20f))
    }

    LaunchedEffect(mapUiState) {
        viewModel.mapUiState = mapUiState
        when (mapUiState) {
            MapUiState.NormalMode -> {
                scaffoldState.bottomSheetState.collapse()
                viewModel.currentMarker.value = null
                addingPlace = false
            }
            MapUiState.BottomSheetInfoMode -> {
                addingPlace = false
                scaffoldState.bottomSheetState.expand()
            }
            MapUiState.PlaceSelectMode -> {

            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (!addingPlace) {
            mapUiState = when (scaffoldState.bottomSheetState.currentValue) {
                BottomSheetValue.Collapsed -> MapUiState.NormalMode
                BottomSheetValue.Expanded -> MapUiState.BottomSheetInfoMode
            }
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
                },
            onLongPress = {
                when (mapUiState) {
                    MapUiState.NormalMode -> {
                        viewModel.lastKnownLocation.value?.let {
                            viewModel.addNewMarker(
                                RawMapMarker(
                                    "Без названия",
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                )
                            )
                        }
                    }
                }
            })
        },
        bottomSheet = {
            MarkerInfoDialog(viewModel.currentMarker.value) { marker ->
                coroutineScope.launch {
                    scaffoldState.bottomSheetState.collapse()
                }
                onMarkerDetailsClick(navController, marker)
            }
        }
    ) {
      
        val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(false)
        if (shouldShowPermissions) LocationPermissionDialog(userPreferences = userPreferences)
        
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (mapLayout, addMarkerFragment, mapMyLocationButton, mapLayersButton,
                mapLayersView, pointer) = createRefs()
            val verticalMyLocationButtonGl = createGuidelineFromAbsoluteRight(56.dp)

            MapLayout(
                modifier = Modifier.constrainAs(mapLayout) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                onMarkerClick = {
                    viewModel.currentMarker.value = it
                    mapUiState = MapUiState.BottomSheetInfoMode
                },
                cameraMoveCallback = { state -> cameraMoveState = state },
                currentCameraPosition = currentCameraPosition,
                mapType = mapType
            )

            MapLayersButton(
                modifier = Modifier.constrainAs(mapLayersButton) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
                layersSelectionMode = mapLayersSelection,
            )

            if (mapLayersSelection.value) Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable { mapLayersSelection.value = false }, color = Color.White
            ) { }
            AnimatedVisibility(mapLayersSelection.value,
                enter = expandIn(expandFrom = Alignment.TopStart) + fadeIn(),
                exit = shrinkOut(
                    shrinkTowards = Alignment.TopStart,
                    animationSpec = tween(380)
                )
                        + fadeOut(animationSpec = tween(280)),
                modifier = Modifier.constrainAs(mapLayersView) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                }) {
                LayersView(mapLayersSelection, mapType)
            }

            AnimatedVisibility(
                modifier = modifier.constrainAs(mapMyLocationButton) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                },
                visible = (viewModel.lastKnownLocation.value != null)
            ) {
                MyLocationButton(
                    onClick = {
                        viewModel.lastKnownLocation.value?.let {
                            viewModel.lastMapCameraPosition.value = getCameraPosition(it)
                        }
                    }
                )
            }

            AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode,
                modifier = Modifier.constrainAs(pointer) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom, 65.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                }) {
                PointerIcon(pointerState)
            }

            AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection.value,
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
                    cameraMoveState = cameraMoveState,
                    currentCameraPosition = currentCameraPosition,
                    pointerState = pointerState
                )
            }

            if (dialogAddPlaceIsShowing.value)
                Dialog(onDismissRequest = { dialogAddPlaceIsShowing.value = false }) {
                    NewPlaceDialog(
                        currentCameraPosition = currentCameraPosition,
                        dialogState = dialogAddPlaceIsShowing,
                        chosenPlace = viewModel.chosenPlace
                    )
                }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MapLayout(
    modifier: Modifier = Modifier,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    cameraMoveCallback: (state: CameraMoveState) -> Unit,
    currentCameraPosition: MutableState<Pair<LatLng, Float>>,
    mapType: MutableState<Int>
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val markers = viewModel.getAllMarkers().collectAsState()
    val map = rememberMapViewWithLifecycle()
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

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
                    val markerColor = Color(it.markerColor)
                    val hue = getHue(markerColor.red, markerColor.green, markerColor.blue)
                    val marker = googleMap
                        .addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(it.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))
                        )
                    marker.tag = it.id
                }
                googleMap.setOnCameraMoveStartedListener {
                    cameraMoveCallback(CameraMoveState.MoveStart)
                }
                googleMap.setOnCameraIdleListener {
                    cameraMoveCallback(CameraMoveState.MoveFinish)
                    currentCameraPosition.value =
                        Pair(googleMap.cameraPosition.target, googleMap.cameraPosition.zoom)
                }
                googleMap.setOnMarkerClickListener { marker ->
                    onMarkerClick(markers.value.first { it.id == marker.tag })
                    viewModel.lastMapCameraPosition.value = Pair(marker.position, DEFAULT_ZOOM)
                    true
                }

                //Map styles: https://mapstyle.withgoogle.com
                if (darkTheme) googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_night)
                )
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

    LaunchedEffect(mapType.value) {
        val googleMap = map.awaitMap()
        googleMap.mapType = mapType.value
    }

    DisposableEffect(map) {
        onDispose {
            viewModel.lastMapCameraPosition.value = currentCameraPosition.value
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    var isDialogOpen by remember {
        mutableStateOf(true)
    }
    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isDialogOpen) {
                GrantLocationPermissionsDialog(
                    onDismiss = {
                        isDialogOpen = false
                    },
                    onNegativeClick = {
                        isDialogOpen = false
                    },
                    onPositiveClick = {
                        isDialogOpen = false
                        permissionsState.launchMultiplePermissionRequest()
                    },
                    onDontAskClick = {
                        SnackbarManager.showMessage(R.string.location_dont_ask)
                        coroutineScope.launch {
                            userPreferences.saveLocationPermissionStatus(false)
                        }
                    }
                )
            }
        },
        permissionsNotAvailableContent = { SnackbarManager.showMessage(R.string.location_permission_denied) })
    { checkPermission(context) }
}

@ExperimentalMaterialApi
@Composable
fun MapFab(state: MapUiState, onClick: () -> Unit, onLongPress: () -> Unit,) {
    val fabImg = remember { mutableStateOf(R.drawable.ic_baseline_add_location_24) }
    val defaultBottomPadding: Dp = 128.dp
    val context = LocalContext.current
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

    /*FloatingActionButton(
        modifier = Modifier
            .animateContentSize()
            .padding(bottom = paddingBottom.value, top = paddingTop.value),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = fabImg.value),
            contentDescription = stringResource(R.string.new_place),
            tint = Color.White,
        )
    }*/

    FishingFab(
        modifier = Modifier
            .animateContentSize()
            .padding(bottom = paddingBottom.value, top = paddingTop.value),
        onClick = onClick,
        onLongPress = {
            Toast.makeText(context, "Adding place on your location", Toast.LENGTH_SHORT).show()
            onLongPress()
        }
    ) {
        Icon(
            painter = painterResource(id = fabImg.value),
            contentDescription = stringResource(R.string.new_place),
            tint = Color.White,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishingFab(onClick: () -> Unit,
               onLongPress: () -> Unit,
               modifier: Modifier = Modifier,
               interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
               shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
               backgroundColor: Color = MaterialTheme.colors.secondary,
               contentColor: Color = contentColorFor(backgroundColor),
               elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
               content: @Composable () -> Unit)
{
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




















