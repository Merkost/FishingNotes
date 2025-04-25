package com.mobileprism.fishing.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.FishingNotesBottomBar
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.SettingsScreen
import com.mobileprism.fishing.ui.home.addHomeGraph
import com.mobileprism.fishing.ui.home.catch.CatchInfoScreen
import com.mobileprism.fishing.ui.home.new_catch.NewCatchMasterScreen
import com.mobileprism.fishing.ui.home.place.UserPlaceScreen
import com.mobileprism.fishing.ui.home.profile.EditProfile
import com.mobileprism.fishing.ui.home.settings.AboutApp
import com.mobileprism.fishing.ui.home.weather.DailyWeatherData
import com.mobileprism.fishing.ui.home.weather.WeatherDailyScreen
import com.mobileprism.fishing.utils.serializableType
import kotlin.reflect.typeOf

@Composable
fun FishingNotesApp() {
    val appStateHolder = rememberAppStateHolder()

//        val statusBarPadding = remember(appStateHolder.currentRoute) {
//            mutableStateOf(
//                if (appStateHolder.currentRoute == HomeSections.MAP.route) Modifier
//                else Modifier.padding()
//            )
//        }

    Scaffold(
        bottomBar = {
            if (appStateHolder.shouldShowBottomBar) {
                FishingNotesBottomBar(
                    modifier = Modifier,
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
    ) { innerPaddingModifier ->
        Column {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colors.primary) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
            }
            NavHost(
                navController = appStateHolder.navController,
                startDestination = MainDestinations.HOME_ROUTE,
                modifier = Modifier.padding(innerPaddingModifier)
            ) {
                NavGraph(
                    navController = appStateHolder.navController,
                    upPress = appStateHolder::upPress,
                )
            }
        }
    }
}

private fun NavGraphBuilder.NavGraph(
    upPress: () -> Unit,
    navController: NavController,
) {
    navigation(
        route = MainDestinations.HOME_ROUTE,
        startDestination = HomeSections.MAP.route,
    ) {
        addHomeGraph(navController, upPress = upPress)
    }

    composable(MainDestinations.LOGIN_ROUTE) {
        LoginScreen(navController = navController)
    }

    composable(MainDestinations.SETTINGS) {
        SettingsScreen(upPress, navController = navController)
    }

    composable(MainDestinations.ABOUT_APP) {
        AboutApp(upPress)
    }

    composable(
        route = MainDestinations.NEW_CATCH_ROUTE,
    ) {
        val place: UserMapMarker? = it.arguments?.getParcelable(Arguments.PLACE)
        it.arguments?.clear()

        NewCatchMasterScreen(place, navController) {
            navController.popBackStack(
                route = MainDestinations.NEW_CATCH_ROUTE,
                inclusive = true
            )
        }
    }

    composable<MainDestinations.Place>(
        typeMap = mapOf(typeOf<UserMapMarker>() to serializableType<UserMapMarker>()),
    ) {
        val place = it.toRoute<MainDestinations.Place>()
        UserPlaceScreen(upPress, navController, place.marker)
    }

    composable<MainDestinations.Catch>(
        typeMap = mapOf(typeOf<UserCatch>() to serializableType<UserCatch>()),
    ) {
        val catch = it.toRoute<MainDestinations.Catch>()
        CatchInfoScreen(navController, catch.catch)
    }

    composable(
        route = MainDestinations.EDIT_PROFILE,
    ) { EditProfile(upPress) }

    composable<MainDestinations.DailyWeather>(
        typeMap = mapOf(typeOf<DailyWeatherData>() to serializableType<DailyWeatherData>()),
    ) {
        val data = it.toRoute<MainDestinations.DailyWeather>()
        WeatherDailyScreen(
            upPress = { navController.popBackStack() },
            data = data.data
        )
    }


}


