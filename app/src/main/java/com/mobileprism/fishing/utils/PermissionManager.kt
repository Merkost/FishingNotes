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
import com.mobileprism.fishing.model.utils.UserHandler.coroutineScope
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
    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)
    val context = LocalContext.current
    if (permissionsState.allPermissionsGranted.not()) {
        GrantLocationPermissionsDialog(
            onDismiss = onCloseCallback,
            onNegativeClick = onCloseCallback,
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