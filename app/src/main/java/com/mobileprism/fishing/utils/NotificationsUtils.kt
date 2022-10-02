package com.mobileprism.fishing.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.views.DefaultDialog

@Composable
fun Context.checkNotificationPolicyAccess(
    notificationManager: NotificationManager,
): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        if (notificationManager.areNotificationsEnabled()) {
            return true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionDialog(this)
            } else OldPermissionDialog(context = this)
        }
    } else if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
        OldPermissionDialog(this)
    }
    return false
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PermissionDialog(context: Context) {
    val openDialog = remember { mutableStateOf(true) }
    val smsPermissions = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(smsPermissions.status){
        if (smsPermissions.status.isGranted) {
            openDialog.value = false
        }
    }

    if (openDialog.value) {
        DefaultDialog(onDismiss = { openDialog.value = false },
            primaryText = stringResource(R.string.notification_permissions_required),
            secondaryText = stringResource(R.string.notification_permissions_required_details),
            onPositiveClick = {
                with(smsPermissions.status) {
                    if (shouldShowRationale) {
                        context.showToast(context.getString(R.string.notification_permission_settings_guide))
                        context.startSmsSettings()
                    } else {
                        smsPermissions.launchPermissionRequest()
                    }
                }
            },
            positiveButtonText = stringResource(id = R.string.allow),
            neutralButtonText = stringResource(id = R.string.cancel),
            onNeutralClick = {
                openDialog.value = false
            }
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OldPermissionDialog(context: Context) {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        DefaultDialog(onDismiss = { openDialog.value = false },
            primaryText = stringResource(R.string.notification_permissions_required),
            secondaryText = stringResource(R.string.notification_permissions_required_details),
            positiveButtonText = stringResource(R.string.allow),
            negativeButtonText = stringResource(R.string.cancel),
            onPositiveClick = { context.startSmsSettings() },
            onNegativeClick = { openDialog.value = false })
    }
}

private fun Context.startSmsSettings() {
    try {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (e: Exception) {
        showToast(getString(R.string.allow_notifications_manually))
        Log.w("TAG", e.message ?: "")
    }
}
