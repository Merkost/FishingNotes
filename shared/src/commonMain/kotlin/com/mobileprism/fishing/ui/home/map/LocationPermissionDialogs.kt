package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.utils.AnimatedResource
import com.mobileprism.fishing.ui.utils.rememberAppSettingsOpener
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.location.LOCATION
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocationPermissionDialog(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onPermissionGranted: () -> Unit = { },
    onCloseCallback: () -> Unit = { },
) {
    var isDialogOpen by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val permissionsController = rememberPermissionsController()
    val openAppSettings = rememberAppSettingsOpener()

    if (isDialogOpen) {
        GrantLocationPermissionsDialog(
            modifier = modifier,
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
                coroutineScope.launch {
                    var permissionGranted = false
                    try {
                        permissionsController.providePermission(Permission.LOCATION)
                        permissionGranted = true
                    } catch (e: DeniedAlwaysException) {
                        SnackbarManager.showMessage(
                            messageTextId = Res.string.location_permission_denied,
                            snackbarAction = SnackbarAction(
                                textId = Res.string.goto_app_settings,
                                action = openAppSettings,
                            ),
                        )
                    } catch (e: DeniedException) {
                        SnackbarManager.showMessage(Res.string.location_permission_denied)
                    }
                    if (permissionGranted) {
                        onPermissionGranted()
                    }
                    onCloseCallback()
                }
            },
            onDontAskClick = {
                isDialogOpen = false
                SnackbarManager.showMessage(
                    messageTextId = Res.string.location_dont_ask,
                    snackbarAction = SnackbarAction(
                        textId = Res.string.goto_app_settings,
                        action = openAppSettings,
                    ),
                )
                coroutineScope.launch {
                    userPreferences.saveLocationPermissionStatus(false)
                }
                onCloseCallback()
            }
        )
    }
}

@ExperimentalComposeUiApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GrantLocationPermissionsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDontAskClick: () -> Unit
) {

    DefaultDialog(
        primaryText = stringResource(Res.string.location_permission_dialog),
        secondaryText = stringResource(Res.string.location_permission_dialog_body),
        neutralButtonText = stringResource(Res.string.dont_ask_again),
        onNeutralClick = onDontAskClick,
        negativeButtonText = stringResource(Res.string.cancel),
        onNegativeClick = onNegativeClick,
        positiveButtonText = stringResource(Res.string.enable_location),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieMyLocation(
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    )
}

@Composable
fun LottieMyLocation(modifier: Modifier) {
    AnimatedResource("my_location", modifier)
}
