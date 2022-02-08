package com.mobileprism.fishing.compose.ui.home.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.override
import com.android.billingclient.api.*
import com.google.firebase.installations.remote.InstallationResponse
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.MainActivity
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.compose.ui.home.views.MyClickableCard
import com.mobileprism.fishing.compose.ui.home.views.PrimaryText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.get
import org.koin.core.parameter.parametersOf

private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
        for (purchase in purchases) {
            //handlePurchase(purchase)
        }
    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
        // Handle an error caused by a user cancelling the purchase flow.
    } else {
        // Handle any other error codes.
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutApp(upPress: () -> Unit) {
    val billingClient: BillingClient = get(parameters = { parametersOf(purchaseUpdateListener) })

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    var isRotating by remember { mutableStateOf(0) }
    val animationModifier = Modifier.graphicsLayer(
        /*rotationY = animateFloatAsState(
            if (isRotating % 3 == 0) 360f else 0f, tween(800)
        ).value,*/
        rotationX = animateFloatAsState(
            if (isRotating % 2 == 0) 360f else 0f, tween(800)
        ).value
    )

    Scaffold(
        topBar = { AboutAppAppBar(upPress) },
        modifier = Modifier.fillMaxSize()
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState(0)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = "appIcon",
                    modifier = Modifier.size(150.dp)
                )
                PrimaryText(text = "Fishing Notes")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        //style = MaterialTheme.typography.h4,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        text = stringResource(R.string.current_app_version) + currentVersion,
                        softWrap = true
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Button(onClick = {
                    billingClient.startConnection(
                        onBillingStart(
                            coroutineScope,
                            billingClient,
                            context
                        )
                    )
                }) {
                    Text(text = stringResource(id = R.string.thanks_developers))
                }

                //test crash
                if (BuildConfig.DEBUG) {
                    Button(onClick = {
                        throw RuntimeException("Test Crash") // Force a crash
                    }) {
                        Text(text = "Test Crash")
                    }
                }

                MyClickableCard(
                    shape = RoundedCornerShape(12.dp),
                    onClick = { isRotating++ },
                    modifier = animationModifier.wrapContentSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painterResource(R.drawable.russia), "",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(6.dp)
                        )
                        Text(" Made in Russia with love  ❤️ ")
                    }
                }
            }
        }
    }
}

fun onBillingStart(
    coroutineScope: CoroutineScope,
    billingClient: BillingClient,
    context: Context
) =
    object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
                coroutineScope.launch {
                    querySkuDetails(billingClient) {
                        val products = it.skuDetailsList
                        products?.let {
                            if (products.isEmpty()) {
                                Toast.makeText(context, "Products list is empty!", Toast.LENGTH_LONG).show()
                            } else {
                                val flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(products.first())
                                    .build()
                                val responseCode = billingClient
                                    .launchBillingFlow(context as MainActivity, flowParams)
                                    .responseCode
                                checkResponseCode(responseCode, context)
                            }

                        }
                    }

                }
            }
        }

        override fun onBillingServiceDisconnected() {
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
            SnackbarManager.showMessage(R.string.billing_unavaliable)
        }
    }


suspend fun querySkuDetails(billingClient: BillingClient, onReady: (SkuDetailsResult) -> Unit) {
    val skuList = ArrayList<String>()
    skuList.add("coffee")
    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

    // leverage querySkuDetails Kotlin extension function
    val skuDetailsResult = withContext(Dispatchers.IO) {
        billingClient.querySkuDetails(params.build())
    }
    onReady(skuDetailsResult)

    // Process the result.
}

private fun checkResponseCode(responseCode: Int, context: Context) {
    when(responseCode) {
        BillingClient.BillingResponseCode.OK -> {}
        else -> {
            Toast.makeText(context, responseCode, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun AboutAppAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings_about),
        onNavClick = { backPress() }
    )
}