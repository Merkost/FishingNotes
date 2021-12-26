package com.joesemper.fishing.compose.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.map.GrantLocationPermissionsDialog
import com.joesemper.fishing.compose.ui.home.map.LocationPermissionDialog
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import com.joesemper.fishing.compose.ui.home.weather.TemperatureValues
import com.joesemper.fishing.compose.ui.theme.AppThemeValues
import com.joesemper.fishing.compose.ui.theme.getAppThemeValueFromColor
import com.joesemper.fishing.compose.ui.utils.ColorPicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalComposeUiApi
@Composable
fun SettingsScreen(backPress: () -> Unit, navController: NavController) {

    val userPreferences: UserPreferences = get()
    val weatherPreferences: WeatherPreferences = get()

    Scaffold(
        topBar = { SettingsTopAppBar(backPress) },
        modifier = Modifier.fillMaxSize())
    {
        Column(modifier = Modifier.verticalScroll(rememberScrollState(0))) {
            MainAppSettings(userPreferences)
            WeatherSettings(weatherPreferences)
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
fun WeatherSettings(weatherPreferences: WeatherPreferences) {
    val coroutineScope = rememberCoroutineScope()

    val isPressureDialogOpen = remember { mutableStateOf(false) }
    val isTemperatureDialogOpen = remember { mutableStateOf(false) }

    val pressureUnit = weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit = weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C.name)

    GetPressureUnit(isPressureDialogOpen, pressureUnit) { newValue ->
        coroutineScope.launch {
            weatherPreferences.savePressureUnit(newValue)
            delay(200)
            isPressureDialogOpen.value = false
        }
    }
    GetTemperatureUnit(isTemperatureDialogOpen, temperatureUnit) { newValue ->
        coroutineScope.launch {
            weatherPreferences.saveTemperatureUnit(newValue)
            delay(200)
            isTemperatureDialogOpen.value = false
        }
    }

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
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun MainAppSettings(userPreferences: UserPreferences) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val appTheme = userPreferences.appTheme.collectAsState(AppThemeValues.Blue.name)
    val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
    val useFastFabAdd by userPreferences.useFabFastAdd.collectAsState(false)

    var isPermissionDialogOpen by remember { mutableStateOf(false) }
    var isAppThemeDialogOpen by remember { mutableStateOf(false) }
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)


    if (isPermissionDialogOpen)
        LocationPermissionDialog(userPreferences = userPreferences) {
            isPermissionDialogOpen = false
        }

    Column {
        AnimatedVisibility (!permissionsState.allPermissionsGranted) {
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
                mutableStateOf(AppThemeValues.valueOf(appTheme.value).color)
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
                            userPreferences.saveAppTheme(getAppThemeValueFromColor(selectedColor))
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
            state = if (use12hTimeFormat) rememberBooleanSettingState(true) else rememberBooleanSettingState(false)
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
            state = if (useFastFabAdd) rememberBooleanSettingState(true) else rememberBooleanSettingState(false)
        )
    }
}

@Composable
fun SettingsHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(14.dp),
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

@Composable
fun GetTemperatureUnit(
    isTemperatureDialogOpen: MutableState<Boolean>,
    currentTemperatureUnit: State<String>,
    onSelectedValue: (temperatureValues: TemperatureValues) -> Unit
) {
    val radioOptions = TemperatureValues.values().asList()
    val context = LocalContext.current

    if (isTemperatureDialogOpen.value) {
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                TemperatureValues.valueOf(
                    currentTemperatureUnit.value
                )
            )
        }
        Dialog(onDismissRequest = { isTemperatureDialogOpen.value = false }) {
            DefaultCard {
                Column(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrimaryText(text = stringResource(R.string.choose_temperature_unit))
                    }

                    radioOptions.forEach { temperatureValue ->
                        Row(
                            Modifier
                                .fillMaxWidth().height(60.dp)
                                .selectable(
                                    selected = (temperatureValue == selectedOption),
                                    onClick = {
                                        onOptionSelected(temperatureValue)
                                        onSelectedValue(temperatureValue)
                                    }
                                )
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (temperatureValue == selectedOption),
                                modifier = Modifier.padding(all = Dp(value = 8F)),
                                onClick = {
                                    onOptionSelected(temperatureValue)
                                    onSelectedValue(temperatureValue)
                                    Toast.makeText(
                                        context,
                                        temperatureValue.name,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            )
                            Text(
                                text = stringResource(temperatureValue.stringRes),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun GetPressureUnit(
    pressureDialogOpen: MutableState<Boolean>,
    currentPressureUnit: State<PressureValues>,
    onSelectedValue: (pressureUnit: PressureValues) -> Unit
) {
    val radioOptions = PressureValues.values().asList()
    val context = LocalContext.current

    if (pressureDialogOpen.value) {
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                currentPressureUnit.value
            )
        }
        Dialog(onDismissRequest = { pressureDialogOpen.value = false }) {
            DefaultCard {
                Column(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrimaryText(text = stringResource(R.string.choose_pressure_unit))
                    }

                    radioOptions.forEach { pressureValue ->
                        Row(
                            Modifier
                                .fillMaxWidth().height(60.dp)
                                .selectable(
                                    selected = (pressureValue == selectedOption),
                                    onClick = {
                                        onOptionSelected(pressureValue)
                                        onSelectedValue(pressureValue)
                                    }
                                )
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (pressureValue == selectedOption),
                                modifier = Modifier.padding(all = Dp(value = 8F)),
                                onClick = {
                                    onOptionSelected(pressureValue)
                                    onSelectedValue(pressureValue)
                                }
                            )
                            Text(
                                text = stringResource(pressureValue.stringRes),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}

@ExperimentalComposeUiApi
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun GetLocationPermission(closeDialog: () -> Unit) {
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            GrantLocationPermissionsDialog(
                onDismiss = closeDialog,
                onNegativeClick = closeDialog,
                onPositiveClick = closeDialog,
                onDontAskClick = closeDialog
            )
        },
        permissionsNotAvailableContent = { SnackbarManager.showMessage(R.string.location_permission_denied) })
    { checkPermission(context) }
}

@Composable
fun SettingsTopAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings),
        onNavClick = { backPress() }
    ) {}
}
