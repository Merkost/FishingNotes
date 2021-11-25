package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.home.map.GrantLocationPermissionsDialog
import com.joesemper.fishing.compose.ui.home.map.checkPermission
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun SettingsScreen(backPress: () -> Unit) {

    val userPreferences: UserPreferences = get()
    val isPermissionDialogOpen = remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)

    GetLocationPermission(isPermissionDialogOpen)

    Scaffold (
        topBar = { SettingsTopAppBar(backPress) },
        modifier = Modifier.fillMaxSize())
    {
        if (checkPermission(context)) SettingsMenuLink(
            icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "LocationOn") },
            title = { Text(text = stringResource(R.string.location_permission)) },
            subtitle = { Text(text = stringResource(R.string.provide_location_permission)) },
            onClick = { isPermissionDialogOpen.value = true },
        )
        SettingsCheckbox(
            icon = { Icon(imageVector = Icons.Default.AccessTime, contentDescription = "AccessTime") },
            title = { Text(text = stringResource(R.string.time_format))},
            subtitle = { Text(text = stringResource(R.string.use_12h)) },
            onCheckedChange = { use12h ->
                coroutineScope.launch {
                    userPreferences.saveTimeFormatStatus(use12h)
                }
            },
            state = if (use12hTimeFormat) rememberBooleanSettingState(true) else rememberBooleanSettingState(false)
        )

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
