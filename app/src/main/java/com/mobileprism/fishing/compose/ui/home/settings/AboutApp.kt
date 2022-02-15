package com.mobileprism.fishing.compose.ui.home.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Savings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.android.billingclient.api.*
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.MainActivity
import com.mobileprism.fishing.compose.ui.home.SnackbarManager
import com.mobileprism.fishing.compose.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.compose.ui.home.views.MyClickableCard
import com.mobileprism.fishing.compose.ui.home.views.PrimaryText
import com.mobileprism.fishing.compose.ui.theme.customColors
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
                    .weight(6f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = "appIcon",
                    modifier = Modifier.size(150.dp)
                )
                PrimaryText(text = stringResource(id = R.string.app_name))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.customColors.secondaryTextColor,
                        text = stringResource(R.string.current_app_version) +
                                (currentVersion ?: stringResource(id = R.string.unknown_version)),
                        softWrap = true
                    )
                }

            }
            Column(
                modifier = Modifier
                    .weight(4f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                OutlinedButton(onClick = { goToPlayStore(context) }) {
                    Text(text = stringResource(id = R.string.leave_review))
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.Default.RateReview, Icons.Default.RateReview.name)
                }

                OutlinedButton(onClick = {
                    billingClient.startConnection(
                        onBillingStart(
                            coroutineScope,
                            billingClient,
                            context
                        )
                    )
                }) {
                    Text(text = stringResource(id = R.string.app_donation))
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.Default.Savings, Icons.Default.Savings.name)
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
                                .padding(8.dp)
                        )
                        Text(stringResource(id = R.string.made_in_russia))
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
                                SnackbarManager.showMessage(R.string.payment_no_content)
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
    skuList.add("donation")
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
    when (responseCode) {
        BillingClient.BillingResponseCode.OK -> {}
        else -> {
            /*Toast.makeText(context, responseCode, Toast.LENGTH_LONG).show()*/
            Log.d("BILLING", responseCode.toString())
        }
    }
}

@Composable
fun LottieStars(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.five_stars))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun AboutAppAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings_about),
        onNavClick = { backPress() }
    )
}

fun goToPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}