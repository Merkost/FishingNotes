package com.mobileprism.fishing.ui.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.*
import com.mobileprism.fishing.ui.home.SnackbarManager
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*

actual fun isDynamicColorSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
actual fun rememberAppVersion(): String? {
    val context = LocalContext.current
    return remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun rememberOpenAppStore(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val packageName = context.packageName
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}

private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
        for (purchase in purchases) {
            // handlePurchase(purchase)
        }
    }
}

@Composable
actual fun rememberBillingLauncher(): (() -> Unit)? {
    val context = LocalContext.current
    val billingClient = remember {
        BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().build())
            .build()
    }

    DisposableEffect(billingClient) {
        onDispose { billingClient.endConnection() }
    }

    return remember(billingClient) {
        {
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        // Billing flow commented out in original code
                    }

                    override fun onBillingServiceDisconnected() {
                        SnackbarManager.showMessage(Res.string.billing_unavaliable)
                    }
                }
            )
        }
    }
}
