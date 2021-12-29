package com.joesemper.fishing.compose.ui.home

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.bar_chart.BarChart
import com.joesemper.fishing.compose.bar_chart.BarChartDataModel
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.views.DefaultAppBar
import com.joesemper.fishing.compose.ui.home.views.DefaultCardClickable
import com.joesemper.fishing.compose.ui.home.views.DefaultDialog
import com.joesemper.fishing.compose.ui.home.views.SecondaryText
import com.joesemper.fishing.domain.UserViewModel
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.vponomarenko.compose.shimmer.shimmer
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun Profile(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel = getViewModel<UserViewModel>()
    var visible by remember { mutableStateOf(false) }
    val user by viewModel.currentUser.collectAsState()
    val userPlacesNum by viewModel.currentPlaces.collectAsState()
    val userCatchesNum by viewModel.currentCatches.collectAsState()


    //val firebaseUser = Firebase.auth.currentUser

    val imgSize: Dp = 120.dp
    val bgHeight: Dp = 180.dp

    val uiState = viewModel.uiState
    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = { ProfileAppBar(navController, viewModel) }) {
        ConstraintLayout(modifier = Modifier
            .fillMaxSize()
            .scrollable(rememberScrollState(0), Orientation.Vertical, true,)) {

            val (background, card, image, name, places, catches, content, stats, box, logout, settings) = createRefs()
            val bgGl = createGuidelineFromTop(120.dp)
            val verticalCenterGl = createGuidelineFromAbsoluteLeft(0.5f)
            Surface(//shape = RoundedCornerShape(0.dp,0.dp,15.dp,15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bgHeight)
                    .constrainAs(background) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }, color = MaterialTheme.colors.primary
            ) {}


            UserImage(user, imgSize, modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    bottom.linkTo(parent.bottom)
                    centerAround(bgGl)
                }
                .zIndex(2f))
            UserText(user, modifier = Modifier
                .constrainAs(name) {
                    top.linkTo(image.bottom)
                    absoluteLeft.linkTo(card.absoluteLeft)
                    absoluteRight.linkTo(card.absoluteRight)
                }
                .zIndex(2f))

            Card(
                modifier = Modifier
                    .constrainAs(card) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .zIndex(1f),
                shape = AbsoluteRoundedCornerShape(25.dp, 25.dp),
                elevation = 10.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Divider()
                Column(
                    modifier = Modifier
                        .padding(top = 120.dp, bottom = 120.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {

                    OutlinedTextField(value = /*user?.login ?: */"@fisherman",
                        label = { Text(text = "Login") },
                        readOnly = true,
                        onValueChange = {})
                    OutlinedTextField(value = user?.email ?: "",
                        label = { Text(text = "Email") },
                        readOnly = true,
                        onValueChange = {})
                    OutlinedTextField(value = user?.registerDate?.toDateTextMonth() ?: "",
                        label = { Text(text = "Register date") },
                        readOnly = true,
                        onValueChange = {})

                    /*userPlacesNum?.let {
                        if (it.isEmpty()) {
                            NoPlacesStats()
                        } else {
                            CatchesChart()
                        }
                    }*/
                }
            }

            PlacesNumber(userPlacesNum,
                Modifier
                    .constrainAs(places) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(verticalCenterGl, imgSize / 2)
                        absoluteRight.linkTo(parent.absoluteRight)
                        bottom.linkTo(image.bottom)
                    }
                    .zIndex(3f))
            CatchesNumber(userCatchesNum,
                Modifier
                    .constrainAs(catches) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(verticalCenterGl, imgSize / 2)
                        bottom.linkTo(image.bottom)
                    }
                    .zIndex(3f))
            /*SettingsIcon(Modifier.constrainAs(settings) {
                top.linkTo(parent.top, 60.dp)
                absoluteLeft.linkTo(verticalCenterGl, imgSize / 2)
                absoluteRight.linkTo(parent.absoluteRight)
                bottom.linkTo(card.top)
            }) { navController.popBackStack() }*/
        }

    }
}

@Composable
fun CatchesChart() {
    val barChartDataModel = BarChartDataModel()
    BarChart(
        barChartData = barChartDataModel.barChartData,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
fun NoPlacesStats() {
    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround) {
        SecondaryText(text = "Добавьте места и уловы чтобы увидеть статистику!")
        LottieMyStats(modifier = Modifier.fillMaxWidth())
    }
    
}

@Composable
fun LottieMyStats(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.stats))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun SettingsIcon(modifier: Modifier, settingsClicked: () -> Unit) {
    IconButton(onClick = settingsClicked, modifier = modifier) {
        Icon(Icons.Default.Settings, stringResource(R.string.settings))
    }
}

@Composable
fun UserText(user: User?, modifier: Modifier) {
    user?.let {
        Text(
            modifier = modifier,
            text = when (user.isAnonymous) {
                true -> stringResource(R.string.anonymous)
                false -> user.displayName
            }, style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlacesNumber(userPlacesNum: List<MapMarker>?, modifier: Modifier = Modifier) {
    //val userPlacesNum by viewModel.getUserPlaces().collectAsState(null)
    userPlacesNum?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            Icon(
                Icons.Default.Place, stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            Text(it.size.toString())
        }
    } ?: Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Icon(
            Icons.Default.Place, stringResource(R.string.place),
            modifier = Modifier
                .size(25.dp)
                .shimmer(),
            tint = Color.LightGray
        )
        Text(
            "0",
            color = Color.LightGray,
            modifier = Modifier
                .background(Color.LightGray)
                .shimmer()
        )
    }

}

@Composable
fun CatchesNumber(userCatchesNum: List<UserCatch>?, modifier: Modifier = Modifier) {
//    val userCatchesNum by viewModel.getUserCatches().collectAsState(null)
    userCatchesNum?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            Icon(
                painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            Text(it.size.toString())
        }
    } ?: Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Icon(
            painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
            modifier = Modifier
                .size(25.dp)
                .shimmer(),
            tint = Color.LightGray
        )
        Text(
            "0",
            color = Color.LightGray,
            modifier = Modifier
                .background(Color.LightGray)
                .shimmer()
        )
    }
}

@InternalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun UserButtons(navController: NavController) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 80.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Bottom)
    ) {
//        ColumnButton(painterResource(R.drawable.ic_friends), stringResource(R.string.friends)) {
//            //notReadyYetToast()
//        }
//
//        ColumnButton(
//            painterResource(R.drawable.ic_edit),
//            stringResource(R.string.edit_profile)
//        ) {
//            //notReadyYetToast()
//        }
//
//        ColumnButton(
//            painterResource(R.drawable.ic_settings),
//            stringResource(R.string.settings)
//        ) {
//            val action =
//                UserFragmentDirections.actionUserFragmentToSettingsFragment()
//            findNavController().navigate(action)
//        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@InternalCoroutinesApi
@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>, navController: NavController) {
    val scope = rememberCoroutineScope()

    val viewModel = getViewModel<UserViewModel>()

    DefaultDialog(
        primaryText = stringResource(R.string.logout_dialog_title),
        secondaryText = stringResource(R.string.logout_dialog_message),
        negativeButtonText = stringResource(id = R.string.No),
        onNegativeClick = { dialogOnLogout.value = false },
        positiveButtonText = stringResource(id = R.string.Yes),
        onPositiveClick = {
            scope.launch {
                viewModel.logoutCurrentUser().collect { isLogout ->
                    if (isLogout) {
                        dialogOnLogout.value = false
                        navController.navigate(MainDestinations.LOGIN_ROUTE) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        },
        onDismiss = { dialogOnLogout.value = false },
        content = {
            LottieLogout(modifier = Modifier
                .fillMaxWidth()
                .height(180.dp))
        }
    )
/*
    AlertDialog(
        title = { Text(stringResource(R.string.logout_dialog_title)) },
        text = { Text(stringResource(R.string.logout_dialog_message)) },
        onDismissRequest = { dialogOnLogout.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        viewModel.logoutCurrentUser().collect { isLogout ->
                            if (isLogout) {
                                dialogOnLogout.value = false
                                navController.navigate(MainDestinations.LOGIN_ROUTE) {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                },
                content = { Text(stringResource(R.string.Yes)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { dialogOnLogout.value = false },
                content = { Text(stringResource(R.string.No)) })
        }
    )*/
}

@Composable
fun LottieLogout(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bye_bye))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun ColumnButton(image: ImageVector, name: String, click: () -> Unit) {
    DefaultCardClickable(
        onClick = click,
        modifier = Modifier.fillMaxWidth(),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(image, name)
                Text(name, modifier = Modifier.padding(start = 10.dp))
            }
        })
}


@ExperimentalCoilApi
@Composable
fun UserImage(user: User?, imgSize: Dp, modifier: Modifier = Modifier) {
    val linearGradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFED2939), Color(0xFFFFFF66))
    )
    user?.let {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            CoilImage(
                imageModel = if (user.photoUrl.isNullOrEmpty() or user.isAnonymous)
                    painterResource(R.drawable.ic_fisher) else user.photoUrl,
                contentScale = ContentScale.Crop,
                shimmerParams = ShimmerParams(
                    baseColor = Color.LightGray,
                    highlightColor = Color.White,
                    durationMillis = 650,
                    dropOff = 0.65f,
                    tilt = 20f,
                ),
                failure = {
                    Text("Image request failed")
                },
                modifier = Modifier
                    .size(imgSize)
                    .clip(CircleShape)
                    .border(2.dp, linearGradientBrush, CircleShape)

            )

        }
    }

}

@OptIn(InternalCoroutinesApi::class)
@Composable
fun ProfileAppBar(navController: NavController, viewModel: UserViewModel) {
    val dialogOnLogout = rememberSaveable { mutableStateOf(false) }
    DefaultAppBar(
        title = stringResource(R.string.profile),
        actions = {
            IconButton(onClick = { dialogOnLogout.value = true }) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
            IconButton(onClick = { navController.navigate(MainDestinations.SETTINGS) }) {
                Icon(Icons.Default.Settings, stringResource(R.string.settings))
            }
        },
        elevation = 0.dp
    )
    if (dialogOnLogout.value) LogoutDialog(dialogOnLogout, navController)
}

@ExperimentalAnimationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun ProfilePreview() {
    //FigmaTheme {
    Profile(rememberNavController())
    //}
}

