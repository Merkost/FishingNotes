package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.views.DefaultAppBar
import com.joesemper.fishing.compose.ui.home.views.MyClickableCard
import com.joesemper.fishing.compose.ui.home.views.PrimaryText


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutApp(upPress: () -> Unit) {

    val context = LocalContext.current
    val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    var isRotating by remember { mutableStateOf(0) }
    val animationModifier = Modifier.graphicsLayer (
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
                    painterResource(R.mipmap.ic_launcher), "appIcon",
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


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

@Composable
fun AboutAppAppBar(backPress: () -> Unit) {
    DefaultAppBar(
        title = stringResource(id = R.string.settings_about),
        onNavClick = { backPress() }
    )
}