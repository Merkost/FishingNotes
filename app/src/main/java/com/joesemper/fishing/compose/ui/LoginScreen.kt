package com.joesemper.fishing.compose.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.insets.systemBarsPadding
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.AppSnackbar
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.theme.FishingNotesTheme
import com.joesemper.fishing.domain.LoginViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.compose.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun LoginScreen(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var clicked by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = get()
    val activity = LocalContext.current as MainActivity
    val uiState = loginViewModel.subscribe().collectAsState()
    val errorString = stringResource(R.string.signin_error)
    val resources = resources()

    LaunchedEffect(uiState.value) {
        when (uiState.value) {
            is BaseViewState.Success<*> -> {
                delay(300)
                isSuccess = true
                isLoading = false
                delay((MainActivity.splashFadeDurationMillis * 2).toLong())

                if ((uiState.value as BaseViewState.Success<*>).data as User? != null) {
                    visible = false
                    delay((MainActivity.splashFadeDurationMillis * 2).toLong())
                    navController.navigate(MainDestinations.HOME_ROUTE)
                }
            }
            is BaseViewState.Loading -> isLoading = true
            is BaseViewState.Error -> {
                scaffoldState.snackbarHostState.showSnackbar(errorString)
            }  //TODO: logger.log((uiState.value as BaseViewState.Error).error)
        }
    }

    LaunchedEffect(true) {
        coroutineScope.launch {
            SnackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = resources.getText(message.messageId)

                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    scaffoldState.snackbarHostState.showSnackbar(text.toString())
                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    SnackbarManager.setMessageShown(message.id)
                }
            }
        }
    }
    FishingNotesTheme {

        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(
                    hostState = it,
                    modifier = Modifier.systemBarsPadding(),
                    snackbar = { snackbarData -> AppSnackbar(snackbarData) }
                )
            },
        ) {
            ConstraintLayout(modifier = Modifier
                .fillMaxSize()) {
                val (background, card, lottieSuccess, cardColumn) = createRefs()

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = {
                            // Slide in from top
                            -it
                        },
                        animationSpec = tween(
                            durationMillis = MainActivity.splashFadeDurationMillis * 4,

                            easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = {
                            // Slide to top
                            -it
                        },
                        animationSpec = tween(
                            durationMillis = MainActivity.splashFadeDurationMillis * 2,
                            //delayMillis = MainActivity.splashFadeDurationMillis / 2,
                            easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                        )
                    )
                ) {
                    Surface(modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .constrainAs(background) {
                            top.linkTo(parent.top)
                        }, color = MaterialTheme.colors.primary
                    ) {}
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = {
                            // Slide in from top
                            2 * it
                        },
                        animationSpec = tween(
                            durationMillis = MainActivity.splashFadeDurationMillis * 4,
                            //delayMillis = MainActivity.splashFadeDurationMillis / 2,
                            //easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = {
                            // Slide in from top
                            2 * it
                        },
                        animationSpec = tween(
                            durationMillis = MainActivity.splashFadeDurationMillis * 2,
                            //easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                        )
                    ),
                    modifier = Modifier.constrainAs(card) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp)
                            .wrapContentHeight(),
                        elevation = 10.dp,
                        shape = RoundedCornerShape(30.dp)
                    ) {

                        //LottieSuccess
                        /*AnimatedVisibility(visible = isSuccess, modifier = Modifier.constrainAs(lottieSuccess) {
                        top.linkTo(card.top)
                        bottom.linkTo(card.bottom)
                        absoluteLeft.linkTo(card.absoluteLeft)
                        absoluteRight.linkTo(card.absoluteRight)
                    }) { LottieSuccess(modifier = Modifier.fillMaxSize()) {
                        navController.navigate(MainDestinations.HOME_ROUTE)
                    } }*/

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .constrainAs(cardColumn) {
                                    top.linkTo(card.top)
                                    bottom.linkTo(card.bottom)
                                    absoluteLeft.linkTo(card.absoluteLeft)
                                    absoluteRight.linkTo(card.absoluteRight)
                                }
                                .fillMaxWidth()
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                        ) {

                            //AppIcon
                            Image(
                                painterResource(R.mipmap.ic_launcher), stringResource(R.string.icon),
                                modifier = Modifier
                                    .padding(30.dp)
                                    .size(140.dp)
                            )

                            //Title
                            Text(
                                stringResource(R.string.login_title),
                                style = Typography.h5,
                                //color = Color.DarkGray
                            )

                            //LottieLoading
                            //AnimatedVisibility(isLoading,) { LottieLoading(modifier = Modifier.size(140.dp)) }
                            //AnimatedVisibility(!isLoading) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                            )
                            //}

                            //Google button
                            Card(
                                shape = RoundedCornerShape(20.dp), elevation = 10.dp,
                                onClickLabel = stringResource(
                                    R.string.google_login
                                ),
                                onClick = { clicked = true; activity.startGoogleLogin() },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .animateContentSize(
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = LinearOutSlowInEasing
                                            )
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        painterResource(R.drawable.googleg_standard_color_18),
                                        stringResource(R.string.google_login),
                                        modifier = Modifier.size(25.dp)
                                    )
                                    Text(text = if (clicked) stringResource(R.string.signing_in)
                                    else stringResource(R.string.sign_with_google))
                                    if (clicked) {
                                        //Spacer(modifier = Modifier.width(16.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .height(16.dp)
                                                .width(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colors.primary
                                        )
                                    }
                                    //Text("Sign in with Google", style = Typography.body1)
                                }
                            }

                            /*GoogleButton(
                                text = "Sign In with Google",
                                loadingText = "Signing In...",
                                onClicked = {activity.startGoogleLogin()}
                            )*/

                            //Space
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp))
                        }


                    }
                }
                LaunchedEffect(this) {
                    visible = true
                }
            }
        }
    }
}


@Composable
fun LottieSuccess(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    val spec = LottieCompositionSpec.RawRes(R.raw.confetti)
    val composition by rememberLottieComposition(spec)
    val compositionResult: LottieCompositionResult = rememberLottieComposition(spec)
    val progress by animateLottieCompositionAsState(
        composition
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
    LaunchedEffect(compositionResult.value) {
        compositionResult.await()
        if (compositionResult.isSuccess) {
            onFinished()
        }
    }
}

@Composable
fun LottieLoading(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walking_fish))
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