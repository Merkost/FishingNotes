package com.joesemper.fishing.compose.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.map.GrantLocationPermissionsDialog
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.home.weather.PressureValues
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun SettingsScreen(backPress: () -> Unit) {

    val userPreferences: UserPreferences = get()
    val weatherPreferences: WeatherPreferences = get()
    val isPermissionDialogOpen = remember {
        mutableStateOf(false)
    }
    val isPressureDialogOpen = remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
    val pressureUnit = weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg.name)

    GetLocationPermission(isPermissionDialogOpen)
    GetPressureUnit(isPressureDialogOpen, pressureUnit) { newValue ->
        coroutineScope.launch {
            weatherPreferences.savePressureUnit(newValue)
            delay(200)
            isPressureDialogOpen.value = false
        }
    }
    //GetTemperatureUnit()

    Scaffold(
        topBar = { SettingsTopAppBar(backPress) },
        modifier = Modifier.fillMaxSize()
    )
    {
        Column {
            if (checkPermission(context))
                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "LocationOn"
                        )
                    },
                    title = { Text(text = stringResource(R.string.location_permission)) },
                    subtitle = { Text(text = stringResource(R.string.provide_location_permission)) },
                    onClick = { isPermissionDialogOpen.value = true },
                )
            SettingsCheckbox(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "AccessTime"
                    )
                },
                title = { Text(text = stringResource(R.string.time_format)) },
                subtitle = { Text(text = stringResource(R.string.use_12h)) },
                onCheckedChange = { use12h ->
                    coroutineScope.launch {
                        userPreferences.saveTimeFormatStatus(use12h)
                    }
                },
                state = if (use12hTimeFormat) rememberBooleanSettingState(true) else rememberBooleanSettingState(
                    false
                )
            )
            SettingsMenuLink(
                icon = { Icon(imageVector = Icons.Default.Compress, contentDescription = Icons.Default.Compress.name) },
                title = { Text(text = stringResource(R.string.pressure_unit)) },
                subtitle = { Text(text = "Choose another pressure unit (Current is: ${pressureUnit.value})") },
                onClick = { isPressureDialogOpen.value = true }
            )
            /*SettingsMenuLink(
                icon = { Icon(imageVector = Icons.Default.Thermostat, contentDescription = Icons.Default.Thermostat.name) },
                title = { Text(text = stringResource(R.string.temperature_unit)) },
                subtitle = { Text(text = "Choose another temperature unit (Current is: ${temperatureUnit.value})") },
                onClick = { isTemperatureDialogOpen.value = true }
            )*/
        }


    }
}

fun GetTemperatureUnit() {
    TODO("Not yet implemented")
}

@Composable
fun GetPressureUnit(
    pressureDialogOpen: MutableState<Boolean>,
    currentPressureUnit: State<String>,
    onSelectedValue: (pressureUnit: PressureValues) -> Unit
) {
    val radioOptions = PressureValues.values().asList()
    val context = LocalContext.current

    if (pressureDialogOpen.value) {
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                PressureValues.valueOf(
                    currentPressureUnit.value
                )
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
                                .fillMaxWidth()
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
                                    Toast.makeText(context, pressureValue.name, Toast.LENGTH_LONG)
                                        .show()
                                }
                            )
                            Text(
                                text = pressureValue.name,
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
@OptIn(ExperimentalPermissionsApi::class)
fun GetLocationPermission(isPermissionDialogOpen: MutableState<Boolean>) {
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isPermissionDialogOpen.value) {
                GrantLocationPermissionsDialog(
                    onDismiss = {
                        isPermissionDialogOpen.value = false
                    },
                    onNegativeClick = {
                        isPermissionDialogOpen.value = false
                    },
                    onPositiveClick = {
                        isPermissionDialogOpen.value = false
                        permissionsState.launchMultiplePermissionRequest()
                    },
                    onDontAskClick = {
                        isPermissionDialogOpen.value
                    }
                )
            }
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
