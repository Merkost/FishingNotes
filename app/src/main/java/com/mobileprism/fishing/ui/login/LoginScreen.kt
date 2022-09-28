package com.mobileprism.fishing.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.MainActivity
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.custom.FishingPasswordTextField
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.viewmodels.login.LoginViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun LoginScreen(upPress: () -> Unit) {
    val viewModel: LoginViewModel = get()
    var visible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current


    LaunchedEffect(uiState) {
        when (val state = uiState) {
            UiState.Error -> {
                // TODO: create auth state
                //state.errorText?.let { showToast(context, state.errorText) }
            }
            UiState.InProgress -> {
                focusManager.clearFocus()
                localSoftwareKeyboardController?.hide()
            }
            else -> {}
        }
        /*if (uiState.isLoggedIn) {
            visible = false
            delay((MainActivity.splashFadeDurationMillis * 2).toLong())
        }*/
    }


    Scaffold(modifier = Modifier) {
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxSize(),
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = {
                    // Slide in from top
                    -it
                },
                animationSpec = tween(
                    durationMillis = MainActivity.splashFadeDurationMillis * 4,
                    //delayMillis = MainActivity.splashFadeDurationMillis,
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
                    //delayMillis = MainActivity.splashFadeDurationMillis,
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
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 30.dp, vertical = 20.dp),
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp)
            ) {

                /*//LottieSuccess
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
                }*/

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())

                        .padding(24.dp)
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        )
                ) {


                    val loginInfo by viewModel.loginInfo.collectAsState()
                    val showPassword = rememberSaveable() { mutableStateOf(false) }

                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderText(
                                text = stringResource(R.string.sign_in),
                            )

                            IconButton(onClick = upPress) {
                                Icon(Icons.Default.Close, Icons.Default.Close.name)
                            }
                        }

                        Column(
                            modifier = Modifier.navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            FishingOutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusEvent {
                                        if (it.isFocused.not())
                                            viewModel.validateLogin(skipEmpty = true)
                                    },
                                enabled = uiState !is UiState.InProgress,
                                isError = loginInfo.loginError.successful.not(),
                                errorString = loginInfo.loginError.errorMessage,
                                value = loginInfo.login,
                                onValueChange = viewModel::setLogin,
                                placeholder = stringResource(R.string.email_or_username),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, Icons.Default.Person.name)
                                }
                            )

                            FishingPasswordTextField(
                                modifier = Modifier.fillMaxWidth(),
                                password = loginInfo.password,
                                onValueChange = viewModel::setPassword,
                                enabled = uiState !is UiState.InProgress,
                                isError = loginInfo.passwordError.successful.not(),
                                errorString = loginInfo.passwordError.errorMessage,
                                showPassword = showPassword.value,
                                onShowPasswordChanged = { showPassword.value = !showPassword.value }
                            )
                        }

                        AnimatedVisibility(visible = uiState is UiState.InProgress) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Crossfade(uiState is UiState.InProgress) {
                                when (it) {
                                    true -> {
                                        DefaultButtonOutlined(
                                            text = stringResource(R.string.cancel),
                                            onClick = viewModel::cancelLogin
                                        )
                                    }
                                    else -> {
                                        Spacer(modifier = Modifier.size(4.dp))
                                    }
                                }
                            }
                            DefaultButtonFilled(
                                text = stringResource(id = R.string.login),
                                enabled = uiState !is UiState.InProgress,
                                onClick = viewModel::signInUser
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) { visible = true }
    }
}