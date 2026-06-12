package com.mobileprism.fishing.ui.home.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Savings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.MyClickableCard
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.utils.rememberAppVersion
import com.mobileprism.fishing.ui.utils.rememberBillingLauncher
import com.mobileprism.fishing.ui.utils.rememberOpenAppStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutApp(upPress: () -> Unit) {
    val currentVersion = rememberAppVersion()
    val openAppStore = rememberOpenAppStore()
    val launchBilling = rememberBillingLauncher()

    var isRotating by remember { mutableStateOf(0) }
    val animationModifier = Modifier.graphicsLayer(
        rotationX = animateFloatAsState(
            if (isRotating % 2 == 0) 360f else 0f, tween(800)
        ).value
    )

    Scaffold(
        topBar = { AboutAppAppBar(upPress) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(it)
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
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = stringResource(Res.string.app_icon),
                    modifier = Modifier.size(150.dp)
                )
                PrimaryText(text = stringResource(Res.string.app_name))
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
                        text = stringResource(Res.string.current_app_version) +
                                (currentVersion ?: stringResource(Res.string.unknown_version)),
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

                OutlinedButton(onClick = { openAppStore() }) {
                    Text(text = stringResource(Res.string.leave_review))
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.Default.RateReview, Icons.Default.RateReview.name)
                }

                if (launchBilling != null) {
                    OutlinedButton(onClick = { launchBilling() }) {
                        Text(text = stringResource(Res.string.app_donation))
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.Default.Savings, Icons.Default.Savings.name)
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
                        modifier = Modifier.padding(4.dp).padding(8.dp)
                    ) {
                        Text(stringResource(Res.string.made_in_russia))
                    }
                }
            }
        }
    }
}

@Composable
fun LottieStars(modifier: Modifier = Modifier) {
    AnimatedResource("five_stars", modifier)
}

@Composable
fun AboutAppAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(Res.string.settings_about),
        onNavClick = { backPress() }
    )
}
