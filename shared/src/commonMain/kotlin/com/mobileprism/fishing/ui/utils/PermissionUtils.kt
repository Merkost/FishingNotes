package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION

@Composable
fun rememberPermissionsController(): PermissionsController {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)
    return controller
}

@Composable
fun rememberLocationPermissionGranted(
    controller: PermissionsController
): MutableState<Boolean> {
    val granted = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        granted.value = controller.getPermissionState(Permission.LOCATION) == PermissionState.Granted
    }
    return granted
}
