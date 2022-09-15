package com.mobileprism.fishing.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.resources
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun StartNavigation() {
    val navController = rememberAnimatedNavController()
    val upPress: () -> Unit = { navController.navigateUp() }

    val scaffoldState = rememberScaffoldState()

    val systemUiController = rememberSystemUiController()

    val surfaceColor = MaterialTheme.colors.surface
    val primaryColor = MaterialTheme.colors.primary

    setSnackbarsListener(scaffoldState)

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier.systemBarsPadding(),
                snackbar = { snackbarData -> AppSnackbar(snackbarData) }
            )
        }
    ) {

        AnimatedNavHost(
            navController = navController,
            startDestination = LoginDestinations.START,
            enterTransition = { fadeIn(animationSpec = tween(600)) },
            exitTransition = { fadeOut(animationSpec = tween(600)) },
            popEnterTransition = { fadeIn(animationSpec = tween(600)) },
            popExitTransition = { fadeOut(animationSpec = tween(600)) }
        ) {

            composable(LoginDestinations.START) {
                DisposableEffect(this) {
                    systemUiController.setStatusBarColor(surfaceColor)
                    onDispose {}
                }
                StartScreen(
                    toLoginScreen = { navController.navigate(LoginDestinations.LOGIN) }
                ) { navController.navigate(LoginDestinations.REGISTER) }
            }

            composable(LoginDestinations.LOGIN) {
                DisposableEffect(this) {
                    systemUiController.setStatusBarColor(primaryColor)
                    onDispose {}
                }
                LoginScreen(upPress = upPress)
            }

            composable(LoginDestinations.REGISTER,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { 1500 }, animationSpec = tween(600))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -1500 }, animationSpec = tween(600))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -1500 }, animationSpec = tween(600))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { 1500 }, animationSpec = tween(600))
                }
            ) {
                RegisterScreen(upPress)
            }

        }
    }

}

@Composable
fun setSnackbarsListener(scaffoldState: ScaffoldState) {
    val coroutineScope = rememberCoroutineScope()
    val resources = resources()

    LaunchedEffect(true) {
        coroutineScope.launch {
            SnackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages.first()
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

}
