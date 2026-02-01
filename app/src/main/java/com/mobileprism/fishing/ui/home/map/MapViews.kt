package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.home.SettingsHeader
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.theme.RedGoogleChrome
import com.mobileprism.fishing.ui.theme.secondaryFigmaColor
import com.mobileprism.fishing.ui.theme.supportTextColor
import com.mobileprism.fishing.utils.location.LocationManager
import com.mobileprism.fishing.viewmodels.MapViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@ExperimentalMaterialApi
@Composable
fun MapScaffold(
    mapUiState: MapUiState,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState,
    fab: @Composable() (() -> Unit)?,
    bottomSheet: @Composable() (ColumnScope.() -> Unit),
    content: @Composable (PaddingValues) -> Unit,
) {

    val dp = animateDpAsState(
        when (mapUiState) {
            is MapUiState.BottomSheetInfoMode -> 168.dp
            else -> 0.dp
        }
    )

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetBackgroundColor = MaterialTheme.colors.surface.copy(0f),
        sheetElevation = 0.dp,
        sheetShape = RectangleShape,
        sheetPeekHeight = dp.value,
        floatingActionButton = fab,
        sheetContent = bottomSheet,
        sheetGesturesEnabled = true,
        content = content
    )
}

@Composable
fun MapModalBottomSheet(
    mapPreferences: UserPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    val showHiddenPlaces by mapPreferences.shouldShowHiddenPlacesOnMap.collectAsState(false)

    val color = animateColorAsState(
        targetValue = if (showHiddenPlaces) {
            MaterialTheme.colors.onSurface
        } else {
            supportTextColor
        },
        animationSpec = tween(800)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsHeader(stringResource(R.string.settings))
        SettingsCheckbox(
            icon = {
                Icon(
                    Icons.Default.Visibility, Icons.Default.Visibility.name,
                    tint = color.value
                )
            },
            title = { Text(text = stringResource(R.string.hidden_places)) },
            subtitle = { Text(text = stringResource(R.string.show_hidden_places)) },
            onCheckedChange = { newValue ->
                coroutineScope.launch { mapPreferences.saveMapHiddenPlaces(newValue) }
            },
            state = if (showHiddenPlaces) rememberBooleanSettingState(true) else rememberBooleanSettingState(
                false
            )
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyLocationButton(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val locationManager: LocationManager = koinInject()
    var locationDialogIsShowing by remember { mutableStateOf(false) }
    val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(false)
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    if (locationDialogIsShowing) {
        if (shouldShowPermissions) {
            LocationPermissionDialog(userPreferences = userPreferences) {
                checkLocationPermissions(context)
                locationDialogIsShowing = false
            }
        } else SnackbarManager.showMessage(R.string.location_permission_denied)
    }

    val color = animateColorAsState(
        when {
            !shouldShowPermissions || !permissionsState.allPermissionsGranted -> {
                RedGoogleChrome
            }

            else -> {
                LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            }
        }
    )

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = {
                when (permissionsState.allPermissionsGranted) {
                    true -> {
                        locationManager.checkGPSEnabled(context as MainActivity)
                        { onClick() }
                    }

                    false -> {
                        locationDialogIsShowing = true
                    }
                }
            }
        ) {
            Icon(
                if (!shouldShowPermissions) Icons.Default.GpsOff
                else Icons.Default.MyLocation,
                stringResource(R.string.my_location),
                tint = color.value
            )
        }
    }
}

@Composable
fun CompassButton(
    modifier: Modifier = Modifier,
    mapBearing: State<Float>,
    onClick: () -> Unit
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = mapBearing.value < 356f && mapBearing.value > 4f,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(delayMillis = 3000, durationMillis = 1000))
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                onClick = { onClick() }) {
                Icon(
                    painterResource(
                        if (mapBearing.value > 356f ||
                            mapBearing.value < 4f
                        ) R.drawable.north
                        else R.drawable.gps
                    ),
                    stringResource(R.string.compass),
                    modifier = Modifier
                        .rotate(1f - mapBearing.value)
                        .fillMaxSize()
                )
            }
        }
    }

}

@Composable
fun MapZoomInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = { onClick() }) {
            Icon(
                Icons.Default.Add,
                Icons.Default.Add.name,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MapZoomOutButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = { onClick() }) {
            Icon(
                Icons.Default.Remove,
                Icons.Default.Remove.name,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MapLayersButton(modifier: Modifier, onLayersSelectionOpen: () -> Unit) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onLayersSelectionOpen
        ) {
            Icon(painterResource(R.drawable.ic_baseline_layers_24), stringResource(R.string.layers))
        }
    }
}

@Composable
fun LayersView(
    mapType: State<Int>,
    onLayerSelected: (Int) -> Unit,
    onCloseMapSelection: () -> Unit
) {
    val context = LocalContext.current
    val analyticsTracker = LocalAnalytics.current

    DisposableEffect(context) {
        analyticsTracker.logEvent(AnalyticsEvent.MapLayers)
        onDispose {}
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(250.dp)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.map_type))
                Card(shape = CircleShape, modifier = Modifier.size(20.dp)) {
                    IconButton(onClick = onCloseMapSelection) {
                        Icon(Icons.Default.Close, stringResource(R.string.close))
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
                    name = stringResource(R.string.roadmap),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.hybrid,
                    painter = painterResource(R.drawable.ic_map_satellite),
                    name = stringResource(R.string.satellite),
                    onLayerSelected = onLayerSelected
                )
                MapLayerItem(
                    mapType,
                    layer = MapTypes.terrain,
                    painter = painterResource(R.drawable.ic_map_terrain),
                    name = stringResource(R.string.terrain),
                    onLayerSelected = onLayerSelected
                )
            }
        }
    }
}

@Composable
fun MapLayerItem(
    mapType: State<Int>,
    layer: Int,
    painter: Painter,
    name: String,
    onLayerSelected: (Int) -> Unit
) {
    val animatedColor by animateColorAsState(
        if (mapType.value == layer) MaterialTheme.colors.primary else Color.White,
        animationSpec = tween(300)
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        IconToggleButton(
            onCheckedChange = { if (it) onLayerSelected(layer) },
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

@Composable
fun MapSettingsButton(
    modifier: Modifier,
    onCLick: () -> Unit,
) {

    Card(
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            onClick = onCLick
        ) {
            Icon(
                Icons.Default.Settings, Icons.Default.Settings.name,
            )
        }
    }
}

@Composable
fun PointerIcon(
    pointerState: PointerState,
    modifier: Modifier = Modifier,
) {
    var isFirstTimeCalled by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(if (darkTheme) R.raw.marker_night else R.raw.marker)
    )
    val lottieAnimatable = rememberLottieAnimatable()

    val startMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(0, 50))
    }
    val finishMinMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(50, 82))
    }

    LaunchedEffect(isFirstTimeCalled) {
        lottieAnimatable.animate(
            composition,
            iteration = 1,
            continueFromPreviousAnimate = true,
            clipSpec = startMinMaxFrame,
        )
    }

    LaunchedEffect(pointerState) {
        if (pointerState == PointerState.ShowMarker) {
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

    isFirstTimeCalled = true

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

@Composable
fun PlaceTileView(
    modifier: Modifier,
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = koinViewModel()
    val placeTileViewNameState by viewModel.placeTileViewNameState.collectAsState()

    val selectedPlace = remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        viewModel.getPlaceTileViewName()
        onDispose { viewModel.cancelPlaceTileNameJob() }
    }

    SetPlaceNameResultListener(placeTileViewNameState.geocoderResult) { newPlaceName ->
        selectedPlace.value = newPlaceName
    }

    val shimmerModifier = if (selectedPlace.value.isNotBlank()) Modifier else
        Modifier.placeholder(
            visible = true,
            color = Color.LightGray,
            shape = CircleShape,
            highlight = PlaceholderHighlight.shimmer(
                highlightColor = Color.White,
            ),
        )
    val pointerIconColor by animateColorAsState(
        if (selectedPlace.value.isNotBlank()) secondaryFigmaColor
        else Color.LightGray
    )
    val textColor by animateColorAsState(
        if (selectedPlace.value.isNotBlank()) MaterialTheme.colors.onSurface
        else Color.LightGray
    )


    Card(
        shape = RoundedCornerShape(size = 20.dp),
        modifier = modifier
            .heightIn(min = 40.dp, max = 80.dp)
            .widthIn(max = 240.dp)
            .animateContentSize(
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
                contentDescription = stringResource(R.string.marker_icon),
                tint = pointerIconColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                selectedPlace.value.ifEmpty { stringResource(R.string.searching) },
                overflow = TextOverflow.Ellipsis,
                color = textColor,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .then(shimmerModifier)
            )
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
fun SetPlaceNameResultListener(geocoderResult: GeocoderResult, setPlaceName: (String) -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(geocoderResult) {
        geocoderResult.let {
            when (it) {
                is GeocoderResult.Success -> {
                    setPlaceName(it.placeName)
                }

                GeocoderResult.NoNamePlace -> {
                    setPlaceName(context.getString(R.string.unnamed_place))
                }

                GeocoderResult.Failed -> {
                    setPlaceName(context.getString(R.string.cant_recognize_place))
                }

                GeocoderResult.InProgress -> {
                    setPlaceName("")
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
@ExperimentalPermissionsApi
fun GrantLocationPermissionsDialog(
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDontAskClick: () -> Unit
) {

    DefaultDialog(
        primaryText = stringResource(R.string.location_permission_dialog),
        neutralButtonText = stringResource(id = R.string.dont_ask_again),
        onNeutralClick = onDontAskClick,
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = onNegativeClick,
        positiveButtonText = stringResource(id = R.string.ok_button),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieMyLocation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieMyLocation(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.my_location))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun BottomSheetLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 25.dp, height = 3.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
    }
}