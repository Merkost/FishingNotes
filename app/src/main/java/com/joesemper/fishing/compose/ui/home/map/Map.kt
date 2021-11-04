package com.joesemper.fishing.compose.ui.home.map

import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.*
import com.joesemper.fishing.compose.ui.home.MyCard
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.ui.theme.Shapes
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.vponomarenko.compose.shimmer.shimmer
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


sealed class LocationState() {
    object NoPermission : LocationState()
    class LocationGranted(val location: LatLng) : LocationState()
    object LocationNotGranted : LocationState()
}

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun Map(
    modifier: Modifier = Modifier,
    navController: NavController,
    addPlaceOnStart: Boolean = false
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = resources()

    val mapView = viewModel.mapView.value ?: rememberMapViewWithLifecycle().apply {
        viewModel.mapView.value = this
    }

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    // Track if the user doesn't want to see the rationale any more.
    val doNotShowRationale = rememberSaveable { mutableStateOf(false) }
    val arePermissonsGiven =
        rememberSaveable { mutableStateOf(permissionsState.allPermissionsGranted) }

    val scaffoldState = rememberBottomSheetScaffoldState()

    val dialogAddPlaceIsShowing = remember { mutableStateOf(false) }


//    val lastKnownLocation = getCurrentLocation(
//        context = context,
//        permissionsState = permissionsState
//    ).collectAsState(
//        initial = LatLng(0.0, 0.0)
//    )

    val lastKnownLocationState by getCurrentLocationFlow(
        context = context,
        permissionsState = permissionsState
    ).collectAsState(
        initial = //LocationState.LocationNotGranted
        LocationState.LocationGranted(
            location = LatLng(55.753215, 37.622504)
        )
    )

    val currentMarker = remember { mutableStateOf<UserMapMarker?>(null) }

    val mapType = rememberSaveable { mutableStateOf(MapTypes.roadmap) }
    val mapLayersSelection = rememberSaveable { mutableStateOf(false) }

    val currentPosition = remember {
        mutableStateOf<LatLng?>(null)
    }

    var mapUiState: MapUiState by remember {
        if (addPlaceOnStart) mutableStateOf(MapUiState.PlaceSelectMode)
        else mutableStateOf(viewModel.mapUiState)
    }

    var pointerState: MutableState<PointerState> = remember {
        mutableStateOf(PointerState.HideMarker)
    }

    var lastPressed: Long = 0
    BackHandler(onBack = {
        when (mapUiState) {
            MapUiState.NormalMode -> {
                val currentMillis = System.currentTimeMillis()
                if (currentMillis - lastPressed < 2000) {
                    (context as MainActivity).finish()
                } else {
                    showToast(context, "Do it again to close the app")
                }
                lastPressed = currentMillis
            }
            else -> mapUiState = MapUiState.NormalMode
        }
    })

    var cameraMoveState: CameraMoveState by remember {
        mutableStateOf(CameraMoveState.MoveFinish)
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        mapUiState = when (scaffoldState.bottomSheetState.currentValue) {
            BottomSheetValue.Collapsed -> MapUiState.NormalMode
            BottomSheetValue.Expanded -> MapUiState.BottomSheetInfoMode
        }
    }

    LaunchedEffect(mapUiState) {
        viewModel.mapUiState = mapUiState
        when (mapUiState) {
            MapUiState.NormalMode -> {
                scaffoldState.bottomSheetState.collapse()
            }
            MapUiState.BottomSheetInfoMode -> {
                scaffoldState.bottomSheetState.expand()
            }
            MapUiState.PlaceSelectMode -> {

            }
        }
    }

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetShape = Shapes.large,
        sheetContent = {
            BottomSheetMarkerDialog(currentMarker.value) { marker ->
                coroutineScope.launch {
                    scaffoldState.bottomSheetState.collapse()
                    navController.navigate(MainDestinations.PLACE_ROUTE, Arguments.PLACE to marker)
                }
            }
        },
        sheetBackgroundColor = Color.White.copy(0f),
        sheetElevation = 0.dp,
        sheetPeekHeight = 0.dp,
        floatingActionButton = {
            when (lastKnownLocationState) {
                is LocationState.LocationGranted -> {
                    FabOnMap(
                        state = mapUiState,
                        onClick = {
                            when (mapUiState) {
                                MapUiState.NormalMode -> {
                                    SnackbarManager.showMessage(R.string.mode_place_selecting)
                                    mapUiState = MapUiState.PlaceSelectMode
                                }
                                MapUiState.PlaceSelectMode -> {
                                    SnackbarManager.showMessage(R.string.mode_place_selecting_off)
                                    mapView.getMapAsync { googleMap ->
                                        val target = googleMap.cameraPosition.target
                                        currentPosition.value =
                                            LatLng(target.latitude, target.longitude)
                                        dialogAddPlaceIsShowing.value = true
                                    }
                                    mapUiState = MapUiState.NormalMode
                                }
                                MapUiState.BottomSheetInfoMode -> {
                                    val marker: UserMapMarker? = currentMarker.value
                                    marker?.let {
                                        coroutineScope.launch {
                                            scaffoldState.bottomSheetState.collapse()
                                            navController.navigate(
                                                MainDestinations.NEW_CATCH_ROUTE,
                                                Arguments.PLACE to it
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (permissionDialog, mapLayout, addMarkerFragment, mapMyLocationButton, mapLayersButton,
                mapLayersView, pointer) = createRefs()

            //MapLayersButton
            MapLayersButton(
                modifier = Modifier
                    .size(40.dp)
                    .constrainAs(mapLayersButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    },
                layersSelectionMode = mapLayersSelection,
            )

            if (lastKnownLocationState is LocationState.LocationGranted) {
                MyLocationButton(coroutineScope, mapView,
                    (lastKnownLocationState as LocationState.LocationGranted).location,
                    modifier = Modifier
                        .size(40.dp)
                        .constrainAs(mapMyLocationButton) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                        })
            }

            //MyLocationButton


            //DialogOnAddPlace
            if (dialogAddPlaceIsShowing.value)
                Dialog(onDismissRequest = { dialogAddPlaceIsShowing.value = false }) {
                    AddMarkerDialog(currentPosition, dialogAddPlaceIsShowing, viewModel.chosenPlace)
                }

            //LayersSelectionView
            if (mapLayersSelection.value) Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable { mapLayersSelection.value = false }, color = Color.White
            ) { }
            AnimatedVisibility(mapLayersSelection.value,
                enter = expandIn(Alignment.TopStart) + fadeIn(),
                exit = shrinkOut(
                    Alignment.TopStart,
                    animationSpec = tween(380)
                )
                        + fadeOut(animationSpec = tween(280)),
                modifier = Modifier.constrainAs(mapLayersView) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                }) {
                LayersView(mapView, mapLayersSelection, mapType)
            }

            //PlaceName while PlaceSelectMode is active
            AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode && !mapLayersSelection.value,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.constrainAs(addMarkerFragment) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(mapLayersButton.absoluteRight, 8.dp)
                    absoluteRight.linkTo(mapMyLocationButton.absoluteLeft, 8.dp)
                }
            ) {
                DialogOnPlaceChoosing(
                    context, cameraMoveState, mapView, currentPosition,
                    modifier = Modifier.wrapContentSize(), pointerState
                )
            }

            //PointerIcon
            AnimatedVisibility(mapUiState == MapUiState.PlaceSelectMode,
                modifier = Modifier.constrainAs(pointer) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom, 65.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                }) {
                PointerIcon(cameraMoveState = cameraMoveState, pointerState)
            }

            PermissionDialog(modifier = Modifier.constrainAs(permissionDialog) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            }, permissionsState = permissionsState)

            if (lastKnownLocationState is LocationState.LocationGranted) {
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
                            SnackbarManager.showMessage(R.string.mode_place_info)
                            moveCameraToLocation(
                                location = LatLng(marker.latitude, marker.longitude),
                                coroutineScope = coroutineScope,
                                map = mapView
                            )
                            //scaffoldState.bottomSheetState.expand()
                            mapUiState = MapUiState.BottomSheetInfoMode
                        }
                    },
                    cameraMoveCallback = { state -> cameraMoveState = state },
                    lastLocation = (lastKnownLocationState as LocationState.LocationGranted).location
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(mapType: MutableState<Int>, layer: Int, painter: Painter, name: String) {
    val animatedColor by animateColorAsState(
        if (mapType.value == layer) Color.Blue else Color.White,
        animationSpec = tween(300)
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        IconToggleButton(
            onCheckedChange = { if (it) mapType.value = layer },
            checked = mapType.value == layer,
            modifier = if (mapType.value == layer) Modifier
                .size(70.dp)
                .border(
                    width = 2.dp,
                    color = animatedColor,
                    shape = RoundedCornerShape(15.dp)
                ) else Modifier
                .size(70.dp)
                .padding(0.dp)
        ) {
            Image(
                painter, layer.toString(),
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(text = name, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}

@ExperimentalMaterialApi
@Composable
fun AddMarkerDialog(
    currentPosition: MutableState<LatLng?>,
    dialogState: MutableState<Boolean>,
    chosenPlace: MutableState<String?>,
) {
    val viewModel = get<MapViewModel>()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    MyCard(shape = Shapes.large) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.White)
            /*.requiredHeight(250.dp).requiredWidth(300.dp)*/
        ) {
            val (progress, name, locationIcon, title, description, saveButton, cancelButton) = createRefs()

            uiState?.let {
                when (it) {
                    UiState.InProgress -> {
                        Surface(color = Color.Gray, modifier = Modifier
                            .constrainAs(progress) {
                                top.linkTo(parent.top)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(parent.absoluteRight)
                                bottom.linkTo(parent.bottom)
                            }
                            .size(100.dp)) {
                            FishLoading(modifier = Modifier.size(150.dp))
                        }
                    }
                    UiState.Success -> {
                        coroutineScope.launch {
                            dialogState.value = false
                            SnackbarManager.showMessage(R.string.add_place_success)
                        }
                    }
                    else -> {
                    }
                }
            }
            val descriptionValue = remember { mutableStateOf("") }
            val titleValue = remember { mutableStateOf(/*chosenPlace.value ?:*/ "") }
            LaunchedEffect(chosenPlace.value) {
                chosenPlace.value?.let {
                    titleValue.value = it
                }
            }

            Icon(painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = "Marker",
                tint = secondaryFigmaColor,
                modifier = Modifier.constrainAs(locationIcon) {
                    absoluteRight.linkTo(name.absoluteLeft, 8.dp)
                    top.linkTo(name.top)
                    bottom.linkTo(name.bottom)
                })

            Text(
                text = stringResource(R.string.new_place),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.constrainAs(name) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight)
                }
            )

            OutlinedTextField(
                value = titleValue.value,
                onValueChange = { titleValue.value = it },
                label = { Text(text = stringResource(R.string.title)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(name.bottom, 2.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 2.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 2.dp)
                }/*.navigationBarsWithImePadding(),*/
            )
            OutlinedTextField(
                value = descriptionValue.value,
                onValueChange = {
                    descriptionValue.value = it
                },
                label = { Text(text = stringResource(R.string.description)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(title.bottom, 2.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                }/*.navigationBarsWithImePadding()*/
            )

            OutlinedButton(modifier = Modifier.constrainAs(cancelButton) {
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                top.linkTo(description.bottom, 12.dp)
                bottom.linkTo(parent.bottom, 12.dp)
            },
                shape = RoundedCornerShape(24.dp), onClick = {
                    coroutineScope.launch {
                        dialogState.value = false
                    }
                }) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*Icon(
                        painterResource(id = R.drawable.ic_baseline_shortcut_24),
                        "",
                        modifier = Modifier.size(24.dp)
                    )*/
                    Text(
                        stringResource(id = R.string.cancel),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Button(modifier = Modifier.constrainAs(saveButton) {
                absoluteRight.linkTo(cancelButton.absoluteLeft, 8.dp)
                top.linkTo(cancelButton.top)
                bottom.linkTo(cancelButton.bottom)
            }, shape = RoundedCornerShape(24.dp), onClick = {
                viewModel.addNewMarker(
                    RawMapMarker(
                        titleValue.value,
                        descriptionValue.value,
                        currentPosition.value?.latitude ?: 0.0,
                        currentPosition.value?.longitude ?: 0.0
                    )
                )
            }) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*Icon(
                        painterResource(id = R.drawable.ic_baseline_navigation_24),
                        "",
                        modifier = Modifier.size(24.dp)
                    )*/
                    Text(
                        stringResource(id = R.string.save),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FishLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@ExperimentalMaterialApi
@Composable
fun BottomSheetMarkerDialog(marker: UserMapMarker?, onDescriptionClick: (UserMapMarker) -> Unit) {

    Spacer(modifier = Modifier.size(6.dp))
    Card(
        elevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp).padding(bottom = 8.dp)
            .clip(Shapes.large)
    ) {
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
                    modifier = Modifier.padding(end = 56.dp).constrainAs(title) {
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

@Composable
fun BottomSheetLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(width = 25.dp, height = 3.dp).clip(CircleShape)
                .background(Color.Gray)
        ) {}
    }
}

@ExperimentalPermissionsApi
@Composable
fun GoogleMapLayout(
    modifier: Modifier,
    map: MapView,
    viewModel: MapViewModel,
    permissionsState: MultiplePermissionsState,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    cameraMoveCallback: (state: CameraMoveState) -> Unit,
    lastLocation: LatLng,
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
            googleMap.setOnCameraMoveStartedListener {
                cameraMoveCallback(CameraMoveState.MoveStart)
            }
            googleMap.setOnCameraIdleListener {
                cameraMoveCallback(CameraMoveState.MoveFinish)
            }
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            //googleMap.uiSettings.isCompassEnabled = true

//            if (viewModel.lastLocation.value == null) {
//                //val lastLatLng = lastLocation.value
//                moveCameraToLocation(coroutineScope, map, lastLocation.value)
//                //viewModel.lastLocation.value = lastLocation.value
//            }
        }
    }

    LaunchedEffect(key1 = lastLocation) {
        moveCameraToLocation(
            coroutineScope = coroutineScope,
            map = map,
            location = lastLocation //here we got 0.0
        )
    }

    LaunchedEffect(map, permissionsState) {
        val googleMap = map.awaitMap()
        checkPermission(context)
        if (permissionsState.allPermissionsGranted) {
            googleMap.isMyLocationEnabled = true
//            moveCameraToLocation(
//                coroutineScope = coroutineScope,
//                map = map,
//                location = lastLocation.value //here we got 0.0
//            )
        }
        //checkPermission(context)
        //googleMap.isMyLocationEnabled = permissionsState.allPermissionsGranted
        googleMap.setOnMarkerClickListener { marker ->
            val mapMarker = markers.value.first { it.id == marker.tag }
            onMarkerClick(mapMarker)
            true
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun DialogOnPlaceChoosing(
    context: Context,
    cameraMoveState: CameraMoveState,
    mapView: MapView,
    currentPosition: MutableState<LatLng?>,
    modifier: Modifier,
    pointerState: MutableState<PointerState>
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context)
    var selectedPlace by remember { mutableStateOf<String?>(null) }

    when (cameraMoveState) {
        CameraMoveState.MoveStart -> {
            pointerState.value = PointerState.ShowMarker
            selectedPlace = null
            viewModel.chosenPlace.value = null
            viewModel.showMarker.value = false
        }
        CameraMoveState.MoveFinish -> {
            LaunchedEffect(cameraMoveState) {
                delay(1200)
                mapView.getMapAsync { googleMap ->
                    val target = googleMap.cameraPosition.target

                    currentPosition.value = LatLng(target.latitude, target.longitude)
                    coroutineScope.launch(Dispatchers.Default) {
                        try {
                            val position = geocoder.getFromLocation(
                                currentPosition.value!!.latitude,
                                currentPosition.value!!.longitude,
                                5
                            )
                            position?.first()?.let {
                                viewModel.showMarker.value = true
                                if (!it.subAdminArea.isNullOrBlank()) {
                                    viewModel.chosenPlace.value =
                                        it.subAdminArea
                                } else if (!it.adminArea.isNullOrBlank()) {
                                    viewModel.chosenPlace.value = it.adminArea
                                } else viewModel.chosenPlace.value = "Место без названия"
                            }
                        } catch (e: Throwable) {
                            viewModel.chosenPlace.value = "Не удалось определить место"
                        }
                        pointerState.value = PointerState.HideMarker
                        selectedPlace = viewModel.chosenPlace.value
                    }

                }
            }
        }
    }

    val placeName = viewModel.chosenPlace.value ?: "Searching..."
    val pointerIconColor by animateColorAsState(
        if (selectedPlace != null) secondaryFigmaColor
        else Color.LightGray
    )
    val textColor by animateColorAsState(
        if (selectedPlace != null) Color.Black
        else Color.LightGray
    )
    val shimmerModifier = if (viewModel.chosenPlace.value != null) Modifier else Modifier.shimmer()

    Card(
        shape = RoundedCornerShape(size = 20.dp),
        modifier = modifier
            .heightIn(min = 40.dp, max = 80.dp)
            .widthIn(max = 240.dp).animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                contentDescription = "Marker",
                tint = pointerIconColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                placeName,
                overflow = TextOverflow.Ellipsis,
                color = textColor,
                modifier = shimmerModifier
            )
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
fun PointerIcon(
    cameraMoveState: CameraMoveState,
    pointerState: MutableState<PointerState>,
    modifier: Modifier = Modifier,
) {
    var isFirstTimeCalled = remember { true }
    val coroutineScope = rememberCoroutineScope()

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.another_marker))

    val lottieAnimatable = rememberLottieAnimatable()

    val startMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(0, 50))
    }
    val finishMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    LaunchedEffect(pointerState.value) {
        if (pointerState.value == PointerState.ShowMarker) {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = true,
                clipSpec = startMinMaxFrame,
            )
        } else {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = false,
                clipSpec = finishMinMaxFrame,
            )
        }
    }

    LottieAnimation(
        modifier = modifier.size(128.dp),
        composition = composition,
        progress = lottieAnimatable.progress
    )
}

@ExperimentalMaterialApi
@Composable
fun FabOnMap(state: MapUiState, onClick: () -> Unit) {
    val fabImg = remember { mutableStateOf(R.drawable.ic_baseline_add_location_24) }
    val defaultBottomPadding: Dp = 194.dp
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

@ExperimentalPermissionsApi
@Composable
fun PermissionDialog(
    modifier: Modifier = Modifier,
    permissionsState: MultiplePermissionsState
) {
    val context = LocalContext.current
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            GrantPermissionsDialog(permissionsState)
        },
        permissionsNotAvailableContent = { Text("Your current location is not available") })
    { checkPermission(context) }
}

@Composable
@ExperimentalPermissionsApi
fun GrantPermissionsDialog(permissionsState: MultiplePermissionsState) {
    Dialog(onDismissRequest = {}) {
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
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView: MapView = remember { MapView(context).apply { id = R.id.map } }

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