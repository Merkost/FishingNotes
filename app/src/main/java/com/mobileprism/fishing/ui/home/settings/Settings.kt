package com.mobileprism.fishing.ui.home.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.map.locationPermissionsList
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.custom.DefaultDialog
import com.mobileprism.fishing.ui.home.views.ItemsSelection
import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.ui.utils.ColorPicker
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewmodels.SettingsViewModel
import com.mobileprism.fishing.utils.LocationPermissionDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(backPress: () -> Unit, navController: NavController) {

    val userPreferences: UserPreferences = get()
    val weatherPreferencesImpl: WeatherPreferences = get()

    Scaffold(
        topBar = { SettingsTopAppBar(backPress) },
        modifier = Modifier.fillMaxSize()
    )
    {
        Column(modifier = Modifier.verticalScroll(rememberScrollState(0))) {
            MainAppSettings(userPreferences)
            WeatherSettings(weatherPreferencesImpl)
            AboutSettings(navController)
        }
    }
}

@Composable
fun AboutSettings(navController: NavController) {
    val context = LocalContext.current

    SettingsHeader(text = stringResource(R.string.settings_about))

    SettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = Icons.Default.Info.name
            )
        },
        title = { Text(text = stringResource(R.string.settings_about)) },
        onClick = {
            navController.navigate(MainDestinations.ABOUT_APP)
        }
    )
    /*SettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = Icons.Default.Share.name
            )
        },
        title = { Text(text = "Предложить идею") },
        onClick = {
            //TODO: идея screen
        }
    )*/

}

@Composable
fun WeatherSettings(weatherPreferencesImpl: WeatherPreferences) {
    val coroutineScope = rememberCoroutineScope()

    val isPressureDialogOpen = remember { mutableStateOf(false) }
    val isTemperatureDialogOpen = remember { mutableStateOf(false) }
    val isWindSpeedDialogOpen = remember { mutableStateOf(false) }

    GetPressureUnit(isPressureDialogOpen, weatherPreferencesImpl)
    GetTemperatureUnit(isTemperatureDialogOpen, weatherPreferencesImpl)
    GetWindSpeedUnit(isWindSpeedDialogOpen, weatherPreferencesImpl)

    SettingsHeader(text = stringResource(R.string.settings_weather))

    SettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = Icons.Default.Thermostat.name
            )
        },
        title = { Text(text = stringResource(R.string.temperature_unit)) },
        subtitle = { Text(text = stringResource(R.string.choose_temperature_unit)/*(Current is: ${temperatureUnit.value})*/) },
        onClick = { isTemperatureDialogOpen.value = true }
    )
    SettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Default.Compress,
                contentDescription = Icons.Default.Compress.name
            )
        },
        title = { Text(text = stringResource(R.string.pressure_unit)) },
        subtitle = { Text(text = stringResource(R.string.choose_pressure_unit)/* (Current is: ${pressureUnit.value})*/) },
        onClick = { isPressureDialogOpen.value = true }
    )
    SettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Default.Air,
                contentDescription = Icons.Default.Air.name
            )
        },
        title = { Text(text = stringResource(R.string.wind_speed_unit)) },
        subtitle = { Text(text = stringResource(R.string.choose_wind_speed_unit)/* (Current is: ${pressureUnit.value})*/) },
        onClick = { isWindSpeedDialogOpen.value = true }
    )
}

@ExperimentalPermissionsApi
@Composable
fun MainAppSettings(userPreferences: UserPreferences) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val viewModel: SettingsViewModel = getViewModel()
    val fishUpdateResult = viewModel.fishUpdateResult.collectAsState()

    val appTheme by userPreferences.appTheme.collectAsState(AppThemeValues.Blue)
    val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
    val useFastFabAdd by userPreferences.useFabFastAdd.collectAsState(false)
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)

    var isPermissionDialogOpen by remember { mutableStateOf(false) }
    var isAppThemeDialogOpen by remember { mutableStateOf(false) }
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)


    if (isPermissionDialogOpen)
        LocationPermissionDialog(userPreferences = userPreferences) {
            isPermissionDialogOpen = false
        }

    Column {
        AnimatedVisibility(!permissionsState.allPermissionsGranted) {
            SettingsMenuLink(
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = Icons.Default.LocationOn.name
                    )
                },
                title = { Text(text = stringResource(R.string.location_permission)) },
                subtitle = { Text(text = stringResource(R.string.provide_location_permission)) },
                onClick = { isPermissionDialogOpen = true },
            )
        }

        // TODO: finish fish saving
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = Icons.Default.Sync.name
                )
            },
            title = { Text(text = "Синхронизировать рыбу") },
            subtitle = { Text(text = "Загрузить актуальную рыбу с сервера") },
            onClick = { viewModel.updateFish() },
            action = {
                if (fishUpdateResult.value == UiState.InProgress) {
                    CircularProgressIndicator()
                } else null
            }
        )

        SettingsHeader(text = stringResource(R.string.settings_main))
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = Icons.Default.ColorLens.name
                )
            },
            title = { Text(text = stringResource(R.string.app_theme)) },
            subtitle = { Text(text = stringResource(R.string.choose_app_theme)/* + "Current is: " + "${appTheme.value}"*/) },
            onClick = { isAppThemeDialogOpen = !isAppThemeDialogOpen }
        )
        AnimatedVisibility(isAppThemeDialogOpen) {
            val (selectedColor, onColorSelected) = remember {
                mutableStateOf(appTheme.color)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,

                ) {
                ColorPicker(
                    colors = AppThemeValues.values().map { it.color },
                    selectedColor,
                    (onColorSelected as (Color?) -> Unit).apply {
                        coroutineScope.launch {
                            userPreferences.saveAppTheme(appTheme.getColor(selectedColor))
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                /*DarkModeLottieSwitch(modifier = Modifier
                    .clip(CircleShape)
                    .requiredSize(48.dp))*/
            }
        }
        SettingsCheckbox(
            icon = {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = Icons.Default.AccessTime.name
                )
            },
            title = { Text(text = stringResource(R.string.time_format)) },
            subtitle = { Text(text = stringResource(R.string.use_12h)) },
            onCheckedChange = { use12h ->
                coroutineScope.launch { userPreferences.saveTimeFormatStatus(use12h) }
            },
            state = if (use12hTimeFormat) rememberBooleanSettingState(true) else rememberBooleanSettingState(
                false
            )
        )
        SettingsCheckbox(
            icon = {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = Icons.Default.ZoomIn.name
                )
            },
            title = { Text(text = stringResource(R.string.map_zoom_buttons)) },
            subtitle = { Text(text = stringResource(R.string.map_zoom_buttons_description)) },
            onCheckedChange = { useZoomButtons ->
                coroutineScope.launch { userPreferences.saveMapZoomButtons(useZoomButtons) }
            },
            state = if (useZoomButtons) rememberBooleanSettingState(true) else rememberBooleanSettingState(
                false
            )
        )
        SettingsCheckbox(
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = Icons.Default.LocationCity.name
                )
            },
            title = { Text(text = stringResource(R.string.fab_fast_add)) },
            subtitle = { Text(text = stringResource(R.string.fast_fab_description)) },
            onCheckedChange = { useFastFabAdd ->
                coroutineScope.launch { userPreferences.saveFabFastAdd(useFastFabAdd) }
            },
            state = if (useFastFabAdd) rememberBooleanSettingState(true) else rememberBooleanSettingState(
                false
            )
        )

    }
}

@Composable
fun SettingsHeader(text: String, modifier: Modifier = Modifier) {
    GrayText(text, modifier.padding(14.dp))
}

@Composable
fun GrayText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        //style = MaterialTheme.typography.h4,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Start,
        color = Color.Gray,
        text = text,
        maxLines = 1,
        softWrap = true
    )
}


/*@Composable
fun DarkModeLottieSwitch(modifier: Modifier = Modifier) {
    var animate by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.light_dark_mode_button)
    )
    val lottieAnimatable = rememberLottieAnimatable()

    val minMaxFrame by remember {
        mutableStateOf(
            when (darkTheme) {
                true -> LottieClipSpec.Frame(180)
                false -> LottieClipSpec.Frame(0)
            }
        )
    }

    LaunchedEffect(animate) {
        lottieAnimatable.animate(
            composition,
            iteration = 1,
            continueFromPreviousAnimate = true,
            clipSpec = minMaxFrame,
        )
    }

    Box(modifier = Modifier.padding(4.dp)) {
        LottieAnimation(
            modifier = modifier.fillMaxSize().clickable { animate = !animate },
            composition = composition,
            progress = lottieAnimatable.progress
        )
    }

}*/

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GetTemperatureUnit(
    isTemperatureDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val temperatureUnit =
        weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val radioOptions = TemperatureValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (temperatureValue: TemperatureValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.saveTemperatureUnit(newValue)
            delay(200)
            isTemperatureDialogOpen.value = false
        }
    }

    if (isTemperatureDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(R.string.choose_temperature_unit),
            onDismiss = { isTemperatureDialogOpen.value = false }) {
            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = temperatureUnit
            ) {
                onSelectedValue(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GetPressureUnit(
    pressureDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val pressureUnit = weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
    val radioOptions = PressureValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (pressureUnit: PressureValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.savePressureUnit(newValue)
            delay(200)
            pressureDialogOpen.value = false
        }
    }

    if (pressureDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(R.string.choose_pressure_unit),
            onDismiss = { pressureDialogOpen.value = false }) {

            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = pressureUnit
            ) {
                onSelectedValue(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GetWindSpeedUnit(
    isWindSpeedDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val windSpeedUnit = weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    val radioOptions = WindSpeedValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (windSpeedValues: WindSpeedValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.saveWindSpeedUnit(newValue)
            delay(200)
            isWindSpeedDialogOpen.value = false
        }
    }

    if (isWindSpeedDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(R.string.choose_temperature_unit),
            onDismiss = { isWindSpeedDialogOpen.value = false }) {
            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = windSpeedUnit
            ) {
                onSelectedValue(it)
            }
        }
    }
}


@Composable
fun SettingsTopAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings),
        onNavClick = { backPress() }
    )
}
