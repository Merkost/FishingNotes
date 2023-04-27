package com.mobileprism.fishing.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.FishingNotesBottomBar
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.addHomeGraph
import com.mobileprism.fishing.ui.home.catch.UserCatchScreen
import com.mobileprism.fishing.ui.home.new_catch.NewCatchMasterScreen
import com.mobileprism.fishing.ui.home.place.UserPlaceScreen
import com.mobileprism.fishing.ui.home.settings.AboutApp
import com.mobileprism.fishing.ui.home.settings.SettingsScreen
import com.mobileprism.fishing.ui.home.weather.WeatherDaily
import com.mobileprism.fishing.ui.login.StartNavigation
import kotlinx.coroutines.InternalCoroutinesApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FishingNotesApp(startDestination: String = MainDestinations.HOME_ROUTE) {
    val appStateHolder = rememberAppStateHolder()

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
        modifier = Modifier.fillMaxSize()
    ) { innerPaddingModifier ->
        NavHost(
            navController = appStateHolder.navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPaddingModifier)
                .consumedWindowInsets(innerPaddingModifier)
//                .systemBarsPadding()
        ) {
            NavGraph(
                navController = appStateHolder.navController,
                upPress = appStateHolder::upPress,
            )
        }
    }
}

private fun NavGraphBuilder.NavGraph(
    upPress: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier/*.systemBarsPadding()*/,
) {
    navigation(
        route = MainDestinations.HOME_ROUTE,
        startDestination = HomeSections.MAP.route,
    ) {
        addHomeGraph(navController, modifier = modifier, upPress = upPress)
    }

    composable(
        route = MainDestinations.AUTH_ROUTE,
    ) {
        StartNavigation(toHomeScreen = {
            navController.navigate(MainDestinations.HOME_ROUTE) {
                popUpTo(MainDestinations.HOME_ROUTE) {
                    inclusive = false
                }
            }
        })
    }

    composable(MainDestinations.SETTINGS) {
        SettingsScreen(
            modifier = modifier,
            upPress,
            navController = navController
        )
    }

    composable(MainDestinations.ABOUT_APP) {
        AboutApp(modifier = modifier, upPress)
    }

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        val place: UserMapMarker? = it.arguments?.getParcelable(Arguments.PLACE)
        it.arguments?.clear()

        NewCatchMasterScreen(modifier = modifier, place, navController) {
            navController.popBackStack(
                route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true
            )
        }
    }

    composable(
        route = MainDestinations.PLACE_ROUTE,
    ) {
        UserPlaceScreen(
            modifier = modifier,
            upPress,
            navController,
            it.requiredArg(Arguments.PLACE)
        )
    }

    composable(
        route = MainDestinations.CATCH_ROUTE,
    ) { UserCatchScreen(modifier = modifier, navController, it.requiredArg(Arguments.CATCH)) }

    composable(
        route = MainDestinations.EDIT_PROFILE,
    ) { /*EditProfile(upPress)*/
        // TODO: Fix editProfile screen
    }

    composable(
        route = MainDestinations.DAILY_WEATHER_ROUTE,
    ) {
        WeatherDaily(
            modifier = modifier,
            upPress = { navController.popBackStack() },
            data = it.requiredArg(Arguments.WEATHER_DATA)
        )
    }


}


