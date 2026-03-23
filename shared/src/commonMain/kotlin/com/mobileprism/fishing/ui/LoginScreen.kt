package com.mobileprism.fishing.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val snackbarHostState = remember { SnackbarHostState() }
    var visible by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    var showLottie by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = koinInject()
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BaseViewState.Success<*> -> {
                state.data?.let {
                    googleLoading = false
                    showLottie = true
                    delay(2500)
                    visible = false
                    delay((UiConstants.SPLASH_FADE_DURATION_MILLIS * 2).toLong())

                    navController.navigate(HomeGraph) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            }

            is BaseViewState.Loading -> {}
            is BaseViewState.Error -> {
                googleLoading = false
                SnackbarManager.showMessage(Res.string.signin_error)
            }
        }
    }

    LaunchedEffect(Unit) {
        SnackbarManager.messages.collect { currentMessages ->
            if (currentMessages.isNotEmpty()) {
                val message = currentMessages[0]
                val text = org.jetbrains.compose.resources.getString(message.messageId)
                googleLoading = false
                snackbarHostState.showSnackbar(text)
                SnackbarManager.setMessageShown(message.id)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.systemBarsPadding(),
                snackbar = { snackbarData -> AppSnackbar(snackbarData) }
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Background: colored top portion + transparent bottom
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = UiConstants.SPLASH_FADE_DURATION_MILLIS * 4,
                        easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = UiConstants.SPLASH_FADE_DURATION_MILLIS * 2,
                        easing = CubicBezierEasing(0f, 0f, 0f, 1f)
                    )
                )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(6.5f), color = MaterialTheme.colorScheme.primary
                    ) {}
                    Box(modifier = Modifier.weight(4f))
                }
            }

            // Card: slides in from bottom, centered vertically
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { 2 * it },
                    animationSpec = tween(
                        durationMillis = UiConstants.SPLASH_FADE_DURATION_MILLIS * 4,
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { 2 * it },
                    animationSpec = tween(
                        durationMillis = UiConstants.SPLASH_FADE_DURATION_MILLIS * 2,
                    )
                ),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .padding(30.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Lottie success overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showLottie,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LottieSuccess() {}
                        }

                        // Login content
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                        ) {
                            Image(
                                painterResource(Res.drawable.ic_launcher), stringResource(Res.string.icon),
                                modifier = Modifier
                                    .padding(30.dp)
                                    .size(140.dp)
                            )

                            Text(
                                stringResource(Res.string.login_title),
                                style = MaterialTheme.typography.headlineSmall,
                            )

                            Spacer(modifier = Modifier.height(30.dp))

                            GoogleButtonUiContainer(
                                onGoogleSignInResult = { googleUser ->
                                    val idToken = googleUser?.idToken
                                    if (idToken != null) {
                                        loginViewModel.firebaseSignInWithGoogle(idToken)
                                    } else {
                                        googleLoading = false
                                        loginViewModel.onGoogleSignInFailed()
                                    }
                                }
                            ) {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                                    onClick = {
                                        googleLoading = true
                                        this@GoogleButtonUiContainer.onClick()
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
                                            painterResource(Res.drawable.ic_google_logo),
                                            stringResource(Res.string.google_login),
                                            modifier = Modifier.size(25.dp)
                                        )
                                        Text(
                                            text = if (googleLoading) stringResource(Res.string.signing_in)
                                            else stringResource(Res.string.sign_with_google)
                                        )
                                        if (googleLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .height(16.dp)
                                                    .width(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
            LaunchedEffect(Unit) { visible = true }
        }
    }
}


@Composable
fun LottieSuccess(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    AnimatedResource("confetti", modifier, iterations = 1, contentScale = ContentScale.Crop)
}
