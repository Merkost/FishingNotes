package com.mobileprism.fishing.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.insets.systemBarsPadding
import com.mobileprism.fishing.R
import com.mobileprism.fishing.di.repositoryModuleLocal
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.showErrorToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.core.context.loadKoinModules

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun LoginScreen() {

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var visible by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    var showLottie by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = get()
    val context = LocalContext.current
    val uiState by loginViewModel.uiState.collectAsState()
    val errorString = stringResource(R.string.signin_error)
    val resources = resources()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BaseViewState.Success<*> -> {
                state.data?.let {
                    googleLoading = false
                    showLottie = true
                    delay(2500)
                    visible = false
                    delay((MainActivity.splashFadeDurationMillis * 2).toLong())
                }
            }
            is BaseViewState.Loading -> {}
            is BaseViewState.Error -> {
                showErrorToast(context, state.error?.message)
                googleLoading = false
                scaffoldState.snackbarHostState.showSnackbar(errorString)
            }
        }
    }

    LaunchedEffect(true) {
        coroutineScope.launch {
            SnackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = resources.getText(message.messageId)
                    googleLoading = false
                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    scaffoldState.snackbarHostState.showSnackbar(text.toString())
                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    SnackbarManager.setMessageShown(message.id)
                }
            }
        }
    }

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
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (card, lottieSuccess, cardColumn) = createRefs()

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
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(6.5f), color = MaterialTheme.colors.primary
                    ) {}
                    Box(modifier = Modifier.weight(4f))
                }
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
                        .height(450.dp)
                        .padding(30.dp),
                    elevation = 10.dp,
                    shape = RoundedCornerShape(30.dp)
                ) {

                    //LottieSuccess
                    AnimatedVisibility(
                        visible = showLottie,
                        modifier = Modifier.constrainAs(lottieSuccess) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                            width = Dimension.fillToConstraints
                            height = Dimension.wrapContent
                        }) {
                        LottieSuccess() {
                            //navController.navigate(MainDestinations.HOME_ROUTE)
                        }
                    }

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
                            painterResource(R.drawable.ic_launcher), stringResource(R.string.icon),
                            modifier = Modifier
                                .padding(30.dp)
                                .size(140.dp)
                        )

                        //Title
                        Text(
                            stringResource(R.string.login_title),
                            style = MaterialTheme.typography.h5,
                            //color = Color.DarkGray
                        )

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )

                        //Google button
                        Card(
                            shape = RoundedCornerShape(20.dp), elevation = 10.dp,
                            onClickLabel = stringResource(R.string.google_login),
                            enabled = !googleLoading,
                            onClick = {
                                loginViewModel.continueWithGoogle()
                                googleLoading = true;
                                (context as MainActivity).startGoogleLogin()
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .padding(end = 2.dp)
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
                                Text(
                                    text = if (googleLoading) stringResource(R.string.signing_in)
                                    else stringResource(R.string.sign_with_google)
                                )
                                if (googleLoading) {
                                    //Spacer(modifier = Modifier.width(16.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )
                        OutlinedButton(
                            onClick = {
                                loginViewModel.continueWithoutLogin()
                            },
                            shape = CircleShape
                        ) {
                            Text(text = "Продолжить без входа")
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )

                    }
                }
            }
            LaunchedEffect(this) { visible = true }
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