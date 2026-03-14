package com.mobileprism.fishing.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.addHomeGraph
import com.mobileprism.fishing.ui.home.catch.CatchInfoScreen
import com.mobileprism.fishing.ui.home.new_catch.NewCatchMasterScreen
import com.mobileprism.fishing.ui.home.place.UserPlaceScreen
import com.mobileprism.fishing.ui.home.profile.EditProfile
import com.mobileprism.fishing.ui.home.settings.AboutApp
import com.mobileprism.fishing.ui.home.settings.SettingsScreen
import com.mobileprism.fishing.ui.home.weather.DailyWeatherData
import com.mobileprism.fishing.ui.home.weather.WeatherDailyScreen
import com.mobileprism.fishing.utils.serializableType
import kotlin.reflect.typeOf

fun NavGraphBuilder.AppNavGraph(
    navController: NavController,
    upPress: () -> Unit,
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
