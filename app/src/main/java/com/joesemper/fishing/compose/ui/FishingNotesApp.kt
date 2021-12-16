package com.joesemper.fishing.compose.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.weather.WeatherDaily
import com.joesemper.fishing.compose.ui.login.LoginScreen
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalComposeUiApi
@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
@Composable
fun FishingNotesApp() {

    ProvideWindowInsets {
        val appStateHolder = rememberAppStateHolder()
        var visible by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                if (appStateHolder.shouldShowBottomBar) {
                    FishingNotesBottomBar(
                        tabs = appStateHolder.bottomBarTabs,
                        currentRoute = appStateHolder.currentRoute!!,
                        navigateToRoute = appStateHolder::navigateToBottomBarRoute
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = it,
                    modifier = Modifier.systemBarsPadding(),
                    snackbar = { snackbarData -> AppSnackbar(snackbarData) }
                )
            },
            scaffoldState = appStateHolder.scaffoldState,
            /*modifier = if (appStateHolder.currentRoute == HomeSections.MAP.route)
                Modifier.statusBarsHeight()
            else Modifier*/
        ) { innerPaddingModifier ->
            Column() {

                    //Spacer(modifier = Modifier.statusBarsHeight())
                    NavHost(
                        navController = appStateHolder.navController,
                        startDestination = MainDestinations.HOME_ROUTE,
                        modifier = /*if (appStateHolder.currentRoute != HomeSections.MAP.route)*/
                        Modifier.padding(innerPaddingModifier) /*else Modifier*/
                    ) {
                        NavGraph(
                            navController = appStateHolder.navController,
                            upPress = appStateHolder::upPress,
                        )
                    }

            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalPermissionsApi
@ExperimentalCoilApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalMaterialApi
private fun NavGraphBuilder.NavGraph(
    upPress: () -> Unit,
    navController: NavController,
) {
    navigation(
        route = MainDestinations.HOME_ROUTE,
        startDestination = HomeSections.MAP.route
    ) {
        addHomeGraph(navController)
    }

    composable(MainDestinations.LOGIN_ROUTE) {
        LoginScreen(navController = navController)
    }

    composable(MainDestinations.SETTINGS) {
        SettingsScreen(upPress)
    }

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        NewCatchScreen({
            navController.popBackStack(
                route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true
            )
        }, it.requiredArg(Arguments.PLACE))
    }

    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) { UserPlaceScreen(navController, it.requiredArg(Arguments.PLACE)) }

    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) { UserCatchScreen(navController, it.requiredArg(Arguments.CATCH)) }

    composable(
        route = MainDestinations.DAILY_WEATHER_ROUTE,
    ) {
        WeatherDaily(
            upPress = { navController.popBackStack() },
            data = it.requiredArg(Arguments.WEATHER_DATA)
        )
    }
}
