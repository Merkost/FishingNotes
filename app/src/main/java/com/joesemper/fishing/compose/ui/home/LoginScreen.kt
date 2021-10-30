package com.joesemper.fishing.compose.ui.home

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.domain.LoginViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.compose.ui.MainActivity
import com.joesemper.fishing.ui.theme.Typography
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import org.koin.androidx.compose.get

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun LoginScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = get()
    val activity = LocalContext.current as MainActivity
    val uiState = loginViewModel.subscribe().collectAsState()

    LaunchedEffect(uiState.value) {
        when (uiState.value) {
            is BaseViewState.Success<*> -> {
                if ((uiState.value as BaseViewState.Success<*>).data as User? != null) {
                    //vb.progressAnimationView.playAnimation()
                    //Timer().schedule(2250) {
                    isLoading = false
                    navController.navigate(MainDestinations.HOME_ROUTE)
                }
            }
            is BaseViewState.Loading -> isLoading = true
            is BaseViewState.Error -> { }  //TODO: logger.log((uiState.value as BaseViewState.Error).error)
        }
    }
    //var isLoading by remember { mutableStateOf(false) }

    ConstraintLayout(modifier = Modifier.fillMaxSize().background(Color.White)) {
        val (background, card, lottieSuccess, cardColumn) = createRefs()

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = {
                    // Slide in from top
                    -it
                },
                animationSpec = tween(
                    durationMillis = MainActivity.splashFadeDurationMillis*4,

                    easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                )
            ),
        ) {
            Surface(modifier = Modifier.fillMaxWidth().height(450.dp).constrainAs(background) {
                top.linkTo(parent.top) }, color = primaryFigmaColor) {}
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = {
                    // Slide in from top
                    2*it
                },
                animationSpec = tween(
                    durationMillis = MainActivity.splashFadeDurationMillis*4,
                    delayMillis = MainActivity.splashFadeDurationMillis/2,
                    //easing = CubicBezierEasing(0f, 0f, 0f, 1f)

                )
            ),
            modifier = Modifier.constrainAs(card) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(30.dp).wrapContentHeight(),
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp)
            ) {

                //LottieSuccess
                AnimatedVisibility(false, modifier = Modifier.constrainAs(lottieSuccess) {
                    top.linkTo(card.top)
                    bottom.linkTo(card.bottom)
                    absoluteLeft.linkTo(card.absoluteLeft)
                    absoluteRight.linkTo(card.absoluteRight)
                }) { LottieSuccess(modifier = Modifier) }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .constrainAs(cardColumn) {
                            top.linkTo(card.top)
                            bottom.linkTo(card.bottom)
                            absoluteLeft.linkTo(card.absoluteLeft)
                            absoluteRight.linkTo(card.absoluteRight)
                        }.fillMaxWidth().animateContentSize()
                ) {

                    //AppIcon
                    Image(
                        painterResource(R.mipmap.ic_launcher), stringResource(R.string.icon),
                        modifier = Modifier.padding(30.dp).size(140.dp)
                    )

                    //Title
                    Text(
                        stringResource(R.string.login_title),
                        style = Typography.h5,
                        color = Color.DarkGray
                    )

                    //LottieLoading
                    AnimatedVisibility(isLoading) { LottieLoading(modifier = Modifier.size(140.dp)) }
                    AnimatedVisibility(!isLoading) {
                        Spacer(
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        )
                    }

                    //Google button
                    Card(
                        shape = RoundedCornerShape(20.dp), elevation = 10.dp,
                        onClickLabel = stringResource(
                            R.string.google_login
                        ),
                        onClick = { isLoading = true; activity.startGoogleLogin() },
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painterResource(R.drawable.googleg_standard_color_18),
                                stringResource(R.string.google_login),
                                modifier = Modifier.size(25.dp)
                            )
                            Text("Sign in with Google", style = Typography.body1)
                        }
                    }

                    //Space
                    Spacer(modifier = Modifier.fillMaxWidth().height(30.dp))
                }


            }
        }
        }
    LaunchedEffect(true) {
        visible = true
    }
    }


@Composable
private fun LottieSuccess(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.another_marker))
    val lottieAnimatable = rememberLottieAnimatable()
    var minMaxFrame by remember {
        mutableStateOf(LottieClipSpec.Frame(0, 30))
    }

    minMaxFrame = LottieClipSpec.Frame(30, 82).also { Log.d("MAP", "MoveFinish") }
    LaunchedEffect(Unit) {
        lottieAnimatable.animate(
            composition,
            iteration = 1,
            continueFromPreviousAnimate = true,
            clipSpec = minMaxFrame,
        )
    }


    LottieAnimation(
        modifier = modifier.size(128.dp),
        composition = composition,
        progress = lottieAnimatable.progress
    )
}

@Composable
private fun LottieLoading(modifier: Modifier = Modifier) {
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