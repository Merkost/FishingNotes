package com.joesemper.fishing.compose.ui.home.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
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
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.*
import me.vponomarenko.compose.shimmer.shimmer
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import java.lang.Exception

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    bottomBarVisibilityState: MutableState<Boolean>
) {

    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val mapView = viewModel.mapView.value ?: rememberMapViewWithLifecycle().apply {
        viewModel.mapView.value = this
    }

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val scaffoldState = rememberBottomSheetScaffoldState(
        //bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val lastKnownLocation = remember {
        getCurrentLocation(context = context, permissionsState = permissionsState)
    }

    val currentMarker = remember {
        mutableStateOf<UserMapMarker?>(null)
    }

    val mapLayersSelection = remember { mutableStateOf(false) }
    val mapType = remember { mutableStateOf(MapTypes.roadmap) }

    val currentPosition = remember {
        mutableStateOf<LatLng?>(null)
    }

    var placeSelectMode by remember {
        mutableStateOf(false)
    }

    var mapUiState: MapUiState by remember { mutableStateOf(MapUiState.NormalMode) }

    var cameraMoveState: CameraMoveState by remember {
        mutableStateOf(CameraMoveState.MoveFinish)
    }


    ModalBottomSheetLayout(
        sheetContent = {
            BottomSheetAddMarkerDialog(
                currentPosition,
                modalBottomSheetState,
                viewModel.chosenPlace,
            )
        },
        sheetState = modalBottomSheetState,
    ) {


        BottomSheetScaffold(
            modifier = modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetContent = {
                BottomSheetMarkerDialog(currentMarker.value, navController)
            },
            drawerGesturesEnabled = true,
            sheetPeekHeight = 0.dp,
            floatingActionButton = {
                FabOnMap(
                    state = mapUiState,
                    onClick = {
                        when (mapUiState) {
                            MapUiState.NormalMode -> {
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
                                placeSelectMode = !placeSelectMode
                            }
                            MapUiState.PlaceSelectMode -> {
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Place select mode off",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                mapView.getMapAsync { googleMap ->
                                    val target = googleMap.cameraPosition.target
                                    currentPosition.value =
                                        LatLng(target.latitude, target.longitude)
                                    coroutineScope.launch {
                                        modalBottomSheetState.show()
                                    }
                                }
                                placeSelectMode = !placeSelectMode
                            }
                            MapUiState.BottomSheetInfoMode -> {
                                /*moveCameraToLocation(
                                    coroutineScope = coroutineScope,
                                    map = mapView,
                                    location = lastKnownLocation.value
                                )*/
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Place info mode on",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //scaffoldState.bottomSheetState.collapse()
                                }
                                navController.currentBackStackEntry?.arguments?.putParcelable(
                                    Arguments.PLACE,
                                    currentMarker.value
                                )
                                navController.navigate(MainDestinations.NEW_CATCH_ROUTE)
                                //placeSelectMode = !placeSelectMode

                            }
                            MapUiState.BottomSheetAddMode -> {
                                moveCameraToLocation(
                                    coroutineScope = coroutineScope,
                                    map = mapView,
                                    location = lastKnownLocation.value
                                )
                                coroutineScope.launch {
                                    Toast.makeText(
                                        context,
                                        "Add New Place",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (permissionDialog, mapLayout, myLocationButton, mapLayersButton,
                    mapLayersView, pointer, addMarkerFragment) = createRefs()

                mapUiState = when {

                    modalBottomSheetState.isVisible -> MapUiState.BottomSheetAddMode
                    placeSelectMode -> MapUiState.PlaceSelectMode
                    scaffoldState.bottomSheetState.isExpanded -> MapUiState.BottomSheetInfoMode.apply {
                        bottomBarVisibilityState.value = false
                    }
                    else -> MapUiState.NormalMode.apply { bottomBarVisibilityState.value = true }
                }

                MyLocationButton(coroutineScope, mapView, lastKnownLocation.value,
                    modifier = Modifier
                        .size(40.dp)
                        .constrainAs(myLocationButton) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                        })

                if (mapLayersSelection.value) Surface(modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable { mapLayersSelection.value = false }, color = Color.White
                ) {}
                AnimatedVisibility(mapLayersSelection.value,
                    modifier = Modifier.constrainAs(mapLayersView) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(250.dp)
                            .wrapContentHeight()/*pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { *//* Called when the gesture starts *//* },
                                onDoubleTap = { *//* Called on Double Tap *//* },
                                onLongPress = { *//* Called on Long Press *//* },
                                onTap = { *//* Called on Tap *//* }
                            )
                        }*/
                    ) {
                        Column(
                            modifier = Modifier.padding(2.dp).padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Тип карты")
                                Card(shape = CircleShape, modifier = Modifier.size(20.dp)) {
                                    IconButton(onClick = { mapLayersSelection.value = false }) {
                                        Icon(Icons.Default.Close, "")
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MapLayerItem(
                                    mapType,
                                    layer = MapTypes.roadmap,
                                    painter = painterResource(R.drawable.ic_map_default),
                                    name = "По умолчанию"
                                )
                                MapLayerItem(
                                    mapType,
                                    layer = MapTypes.hybrid,
                                    painter = painterResource(R.drawable.ic_map_satellite),
                                    name = "Спутник"
                                )
                                MapLayerItem(
                                    mapType,
                                    layer = MapTypes.terrain,
                                    painter = painterResource(R.drawable.ic_map_terrain),
                                    name = "Рельеф"
                                )

                                /*LaunchedEffect(mapType) {
                                    val googleMap = mapView.awaitMap()
                                    googleMap.setMapStyle(MapStyleOptions(mapType))
                                }*/
                            }
                        }
                    }
                }

                AnimatedVisibility(!mapLayersSelection.value,
                    modifier = Modifier.constrainAs(mapLayersButton) {
                        top.linkTo(parent.top, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    }) {
                    MapLayersButton(
                        modifier = Modifier.size(40.dp),
                        layersSelectionMode = mapLayersSelection,
                    )
                }


                if (mapUiState == MapUiState.PlaceSelectMode) {
                    DialogOnPlaceChoosing(
                        context, cameraMoveState, mapView, currentPosition,
                        modifier = Modifier
                            .constrainAs(addMarkerFragment) {
                                top.linkTo(parent.top, 16.dp)
                                absoluteLeft.linkTo(parent.absoluteLeft, 72.dp)
                                absoluteRight.linkTo(parent.absoluteRight, 72.dp)
                            }
                            .wrapContentSize()
                    )
                    PointerIcon(modifier = Modifier.constrainAs(pointer) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom, 65.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }, cameraMoveState = cameraMoveState)
                }

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
                            moveCameraToLocation(
                                location = LatLng(marker.latitude, marker.longitude),
                                coroutineScope = coroutineScope,
                                map = mapView
                            )
                            //mapUiState = MapUiState.BottomSheetInfoMode
                            scaffoldState.bottomSheetState.expand()

                        }
                    },
                    cameraMoveCallback = { state ->
                        cameraMoveState = state
                    },
                    mapType = mapType.value,
                    lastLocation = lastKnownLocation
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(mapType: MutableState<Int>, layer: Int, painter: Painter, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        IconToggleButton(
            onCheckedChange = { if (it) mapType.value = layer },
            checked = mapType.value == layer,
            modifier = if (mapType.value == layer) Modifier
                .size(70.dp)
                .border(
                    width = 2.dp,
                    color = Color.Blue,
                    shape = RoundedCornerShape(15.dp)
                ) else Modifier.size(70.dp).padding(2.dp)
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

@Composable
fun MyLocationButton(
    coroutineScope: CoroutineScope,
    mapView: MapView,
    lastKnownLocation: LatLng,
    modifier: Modifier
) {
    Card(shape = CircleShape, modifier = modifier) {
        IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { moveCameraToLocation(coroutineScope, mapView, lastKnownLocation) }) {
            Icon(Icons.Default.MyLocation, stringResource(R.string.my_location))
        }
    }
}

@Composable
fun MapLayersButton(layersSelectionMode: MutableState<Boolean>, modifier: Modifier) {
    Card(shape = CircleShape, modifier = modifier) {
        IconButton(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
            onClick = { layersSelectionMode.value = true }) {
            Icon(Icons.Default.Apps, stringResource(R.string.layers))
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun BottomSheetAddMarkerDialog(
    currentPosition: MutableState<LatLng?>,
    modalBottomSheetState: ModalBottomSheetState,
    chosenPlace: MutableState<String?>,
) {
    val viewModel = get<MapViewModel>()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    Spacer(modifier = Modifier.size(1.dp))

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
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
                        modalBottomSheetState.hide()
                        Toast.makeText(
                            context,
                            "Place added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> {
                }
            }
        }

        val titleValue = remember { mutableStateOf(/*chosenPlace.value ?:*/ "") }
        LaunchedEffect(chosenPlace.value) {
            chosenPlace.value?.let {
                titleValue.value = it
            }
        }
        val descriptionValue = remember { mutableStateOf("") }



        Text(
            text = "Новая точка",
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
            }
        )

        OutlinedTextField(
            value = titleValue.value,
            onValueChange = {
                titleValue.value = it
            },
            label = { Text(text = "Название") },
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
            label = { Text(text = "Описание") },
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

        /*Icon(
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
       )*/



        OutlinedButton(modifier = Modifier.constrainAs(cancelButton) {
            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            top.linkTo(description.bottom, 8.dp)
            bottom.linkTo(parent.bottom, 16.dp)
        },
            shape = RoundedCornerShape(24.dp), onClick = {
                coroutineScope.launch {
                    modalBottomSheetState.hide()
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
fun BottomSheetMarkerDialog(marker: UserMapMarker?, navController: NavController) {
    Spacer(modifier = Modifier.size(1.dp))
    marker?.let {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(2.dp)
        ) {
            val (locationIcon, title, description, navigateButton, detailsButton) = createRefs()
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
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top, 16.dp)
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

            Button(modifier = Modifier.constrainAs(detailsButton) {
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                top.linkTo(description.bottom, 8.dp)
                bottom.linkTo(parent.bottom, 16.dp)
            },
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    navController.currentBackStackEntry?.arguments?.putParcelable(
                        Arguments.PLACE,
                        it
                    )
                    navController.navigate(MainDestinations.PLACE_ROUTE)
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

@ExperimentalPermissionsApi
@Composable
fun GoogleMapLayout(
    modifier: Modifier,
    map: MapView,
    viewModel: MapViewModel,
    permissionsState: MultiplePermissionsState,
    onMarkerClick: (marker: UserMapMarker) -> Unit,
    cameraMoveCallback: (state: CameraMoveState) -> Unit,
    mapType: Int,
    lastLocation: MutableState<LatLng>
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
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            if (viewModel.lastLocation.value == null) {
                //val lastLatLng = lastLocation.value
                moveCameraToLocation(coroutineScope, map, lastLocation.value)
                //viewModel.lastLocation.value = lastLocation.value
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

    LaunchedEffect(mapType) {
        val googleMap = map.awaitMap()
        googleMap.mapType = mapType
    }

    /*moveCameraToLocation(
        coroutineScope = coroutineScope,
        map = map,
        location = lastKnownLocation.value
    )*/
}

@ExperimentalAnimationApi
@Composable
fun DialogOnPlaceChoosing(
    context: Context,
    cameraMoveState: CameraMoveState,
    mapView: MapView,
    currentPosition: MutableState<LatLng?>,
    modifier: Modifier
) {
    val viewModel: MapViewModel = getViewModel()
    val coroutineScope = rememberCoroutineScope()
    val geocoder = Geocoder(context)

    var job: Job? = null


    when (cameraMoveState) {
        CameraMoveState.MoveStart -> {
            //viewModel.getPlaceName
            job?.cancel()
            viewModel.chosenPlace.value = null
        }
        CameraMoveState.MoveFinish -> {
            LaunchedEffect(cameraMoveState) {
                job = coroutineScope.launch {

                    delay(2000)
                    if (isActive) {
                        mapView.getMapAsync { googleMap ->
                            val target = googleMap.cameraPosition.target
                            currentPosition.value =
                                LatLng(target.latitude, target.longitude)
                        }
                        //getPlaceName(coroutineScope, geocoder)
                        try {
                            val position = geocoder.getFromLocation(
                                currentPosition.value!!.latitude,
                                currentPosition.value!!.longitude,
                                1
                            )
                            position?.first()?.let {

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
                    }
                }
            }
        }
    }


    Card(
        shape = RoundedCornerShape(size = 20.dp),
        modifier = modifier.heightIn(min = 40.dp, max = 40.dp).widthIn(max = 240.dp)
    ) {
        AnimatedVisibility(viewModel.chosenPlace.value.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "Marker",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(32.dp)
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    Card(
                        elevation = 0.dp,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .shimmer(),
                        //backgroundColor = Color.LightGray
                    ) {
                        Text("Searching...", color = Color.LightGray)
                    }
                }
            }
        }
        AnimatedVisibility(!viewModel.chosenPlace.value.isNullOrEmpty()) {
            viewModel.chosenPlace.value?.let {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                        contentDescription = "Marker",
                        tint = secondaryFigmaColor,
                        modifier = Modifier
                            .size(32.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(it, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    Spacer(Modifier.size(4.dp))
                }
            }
        }
    }
}

@Composable
fun PointerIcon(cameraMoveState: CameraMoveState, modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.another_marker))
    val lottieAnimatable = rememberLottieAnimatable()
    var minMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    when (cameraMoveState) {
        CameraMoveState.MoveFinish -> {
            minMaxFrame = LottieClipSpec.Frame(50, 82).also { Log.d("MAP", "MoveFinish") }
            LaunchedEffect(Unit) {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    clipSpec = minMaxFrame,
                )
            }
        }
        CameraMoveState.MoveStart -> {
            minMaxFrame = LottieClipSpec.Frame(0, 50).also { Log.d("MAP", "MoveStart") }
            LaunchedEffect(Unit) {
                lottieAnimatable.animate(
                    composition,
                    iteration = 1,
                    clipSpec = minMaxFrame,
                )
            }

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
    val padding = remember { mutableStateOf(128.dp) }

    when (state) {
        MapUiState.NormalMode -> {
            fabImg.value = R.drawable.ic_baseline_add_location_24
            padding.value = 128.dp
        }
        MapUiState.BottomSheetInfoMode -> {
            fabImg.value = R.drawable.ic_add_catch
            padding.value = 15.dp
        }
        MapUiState.PlaceSelectMode -> {
            fabImg.value = R.drawable.ic_baseline_check_24
            padding.value = 128.dp
        }
    }

    FloatingActionButton(
        modifier = Modifier
            .animateContentSize()
            .padding(
                bottom = padding.value
            ),
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