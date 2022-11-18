package com.mobileprism.fishing.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.login.forgot_password.ResetAccountScreen
import com.mobileprism.fishing.ui.login.forgot_password.RestoreDestinations
import com.mobileprism.fishing.ui.login.forgot_password.SearchAccountScreen
import com.mobileprism.fishing.ui.login.forgot_password.addForgotPasswordGraph
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.requiredArg
import com.mobileprism.fishing.ui.resources
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.viewmodels.restore.UserLogin
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun StartNavigation(toHomeScreen: () -> Unit) {
    FishingNotesTheme(isLoginScreen = true) {
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
                        toLoginScreen = { navController.navigate(LoginDestinations.LOGIN) },
                        toRegistration = { navController.navigate(LoginDestinations.REGISTER) },
                        toHomeScreen = toHomeScreen
                    )
                }

                composable(LoginDestinations.LOGIN) {
                    DisposableEffect(this) {
                        systemUiController.setStatusBarColor(primaryColor)
                        onDispose {}
                    }
                    LoginScreen(upPress = upPress, toHomeScreen = toHomeScreen) {
                        navController.navigate(RestoreDestinations.SEARCH_AND_CONFIRM_ACCOUNT)
                    }
                }

                navigation(
                    RestoreDestinations.SEARCH_AND_CONFIRM_ACCOUNT,
                    route = LoginDestinations.FORGOT_PASSWORD
                ) {
                    // TODO: change statusBar color
                    addForgotPasswordGraph(navController, upPress = upPress)
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
                    RegisterScreen(upPress, toHomeScreen = toHomeScreen)
                }

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
