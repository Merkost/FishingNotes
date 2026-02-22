package com.mobileprism.fishing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.mobileprism.fishing.ui.home.settings.SettingsScreen
import com.mobileprism.fishing.ui.home.addHomeGraph
import com.mobileprism.fishing.ui.home.catch.CatchInfoScreen
import com.mobileprism.fishing.ui.home.new_catch.NewCatchMasterScreen
import com.mobileprism.fishing.ui.home.place.UserPlaceScreen
import com.mobileprism.fishing.ui.home.profile.EditProfile
import com.mobileprism.fishing.ui.home.settings.AboutApp
import com.mobileprism.fishing.ui.home.views.SyncStatusIndicator
import com.mobileprism.fishing.ui.home.weather.DailyWeatherData
import com.mobileprism.fishing.ui.home.weather.WeatherDailyScreen
import com.mobileprism.fishing.utils.serializableType
import com.mobileprism.fishing.viewmodels.MainViewModel
import kotlin.reflect.typeOf
import androidx.compose.foundation.layout.WindowInsets
import org.koin.androidx.compose.koinViewModel

@Composable
fun FishingNotesApp() {
    val appStateHolder = rememberAppStateHolder()
    val mainViewModel: MainViewModel = koinViewModel()
    val syncState by mainViewModel.syncState.collectAsState()

    Scaffold(
        bottomBar = {
            if (appStateHolder.shouldShowBottomBar) {
                val currentSection = appStateHolder.currentSection() ?: HomeSections.MAP
                FishingNotesBottomBar(
                    modifier = Modifier,
                    tabs = appStateHolder.bottomBarTabs,
                    currentSection = currentSection,
                    navigateToRoute = appStateHolder::navigateToBottomBarRoute
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = appStateHolder.snackbarHostState,
                modifier = Modifier.systemBarsPadding(),
                snackbar = { snackbarData -> AppSnackbar(snackbarData) }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars)
    ) { innerPaddingModifier ->
        Column(modifier = Modifier.padding(innerPaddingModifier)) {
            SyncStatusIndicator(syncState = syncState)
            NavHost(
                navController = appStateHolder.navController,
                startDestination = HomeGraph,
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
    navigation<HomeGraph>(
        startDestination = MainDestinations.Map::class,
    ) {
        addHomeGraph(navController, upPress = upPress)
    }

    composable<MainDestinations.Login> {
        LoginScreen(navController = navController)
    }

    composable<MainDestinations.Settings> {
        SettingsScreen(upPress, navController = navController)
    }

    composable<MainDestinations.AboutApp> {
        AboutApp(upPress)
    }

    composable<MainDestinations.NewCatch>(
        typeMap = mapOf(typeOf<UserMapMarker?>() to serializableType<UserMapMarker>(isNullableAllowed = true)),
    ) {
        val route = it.toRoute<MainDestinations.NewCatch>()

        NewCatchMasterScreen(route.place, navController) {
            navController.popBackStack<MainDestinations.NewCatch>(inclusive = true)
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

    composable<MainDestinations.EditProfile> {
        EditProfile(upPress)
    }

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
