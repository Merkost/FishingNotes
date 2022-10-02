package com.mobileprism.fishing.utils

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.map.GrantLocationPermissionsDialog
import com.mobileprism.fishing.ui.home.map.locationPermissionsList
import kotlinx.coroutines.launch

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
    if (permissionsState.shouldShowRationale) {
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

    /*PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            if (isDialogOpen) {

            }
        },
        permissionsNotAvailableContent = { onCloseCallback(); SnackbarManager.showMessage(R.string.location_permission_denied) })
    { checkLocationPermissions(context); }*/
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetLocationPermission(closeDialog: () -> Unit) {
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current

    /*PermissionsRequired(
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
    { checkLocationPermissions(context) }*/
}


@ExperimentalPermissionsApi
fun addPhoto(
    permissionState: PermissionState,
    addPhotoState: MutableState<Boolean>,
    choosePhotoLauncher: ManagedActivityResultLauncher<Array<String>, List<Uri>>
) {
    when {
        permissionState.status.isGranted -> {
            choosePhotoLauncher.launch(arrayOf("image/*"))
            addPhotoState.value = false
        }
    }
}