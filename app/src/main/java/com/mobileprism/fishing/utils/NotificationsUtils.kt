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
import com.google.accompanist.permissions.rememberPermissionState
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

    LaunchedEffect(smsPermissions.hasPermission){
        if (smsPermissions.hasPermission) {
            openDialog.value = false
        }
    }

    if (openDialog.value) {
        DefaultDialog(onDismiss = { openDialog.value = false },
            primaryText = stringResource(R.string.notification_permissions_required),
            secondaryText = stringResource(R.string.notification_permissions_required_details),
            onPositiveClick = {
                if (!smsPermissions.shouldShowRationale && smsPermissions.hasPermission.not() && smsPermissions.permissionRequested) {
                    context.showToast(context.getString(R.string.notification_permission_settings_guide))
                    context.startSmsSettings()
                } else {
                    smsPermissions.launchPermissionRequest()
                }
            },
            positiveButtonText = stringResource(id = R.string.good),
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

    DefaultDialog(onDismiss = { /*TODO*/ },
        primaryText = "Необходимо разрешение на отправку уведомлений",
        secondaryText = "Без разрешения приложение не сможет отпаравлять уведомления и будет работать неправильно",
        onPositiveClick = {
            context.startSmsSettings()
        })

}

private fun Context.startSmsSettings() {
    try {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (e: Exception) {
        // TODO:
        Log.w("TAG", e.message ?: "")
    }
}
