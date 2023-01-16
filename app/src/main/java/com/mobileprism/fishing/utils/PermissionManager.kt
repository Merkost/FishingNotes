package com.mobileprism.fishing.utils

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.MutableState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState


@OptIn(ExperimentalPermissionsApi::class)
fun addPhoto(
    permissionState: PermissionState,
    addPhotoState: MutableState<Boolean>,
    choosePhotoLauncher: ManagedActivityResultLauncher<Array<String>, List<Uri>>
) {
    // TODO: Manage permission checking
    //when {
    //    permissionState.hasPermission -> {
            choosePhotoLauncher.launch(arrayOf("image/*"))
            addPhotoState.value = false
    //    }
    //}
}