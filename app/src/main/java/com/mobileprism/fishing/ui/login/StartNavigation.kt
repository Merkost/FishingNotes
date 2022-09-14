package com.mobileprism.fishing.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun StartNavigation() {
    val navController = rememberAnimatedNavController()
    val upPress: () -> Unit = { navController.navigateUp() }

    val systemUiController = rememberSystemUiController()

    val surfaceColor = MaterialTheme.colors.surface
    val primaryColor = MaterialTheme.colors.primary

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
                onDispose{}
            }
            StartScreen(
                toLoginScreen = { navController.navigate(LoginDestinations.LOGIN) }
            ) { navController.navigate(LoginDestinations.REGISTER) }
        }

        composable(LoginDestinations.LOGIN) {
            DisposableEffect(this) {
                systemUiController.setStatusBarColor(primaryColor)
                onDispose{}
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
            DisposableEffect(this) {
                systemUiController.setStatusBarColor(surfaceColor)
                onDispose{}
            }
            RegisterScreen(upPress)
        }

    }

}