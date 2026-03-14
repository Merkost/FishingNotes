package com.mobileprism.fishing.ui.home

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.HomeTabs
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.map.MapScreen
import com.mobileprism.fishing.ui.home.notes.Notes
import com.mobileprism.fishing.ui.home.profile.Profile
import com.mobileprism.fishing.ui.home.weather.WeatherScreen
import com.mobileprism.fishing.utils.serializableType
import kotlin.reflect.typeOf

fun NavGraphBuilder.addHomeGraph(
    navController: NavController,
    modifier: Modifier = Modifier,
    upPress: () -> Unit,
) {
    composable<MainDestinations.Map>(
        typeMap = mapOf(typeOf<UserMapMarker?>() to serializableType<UserMapMarker>(isNullableAllowed = true)),
    ) { from ->
        val route = from.toRoute<MainDestinations.Map>()
        MapScreen(modifier, navController, route.isAddingNewPlace, route.place, upPress)
    }
    composable<HomeTabs.NotesTab> {
        Notes(modifier, navController, upPress)
    }
    composable<HomeTabs.WeatherTab>(
        typeMap = mapOf(typeOf<UserMapMarker?>() to serializableType<UserMapMarker>(isNullableAllowed = true)),
    ) { from ->
        val route = from.toRoute<HomeTabs.WeatherTab>()
        WeatherScreen(modifier, navController, route.place)
        { navController.popBackStack() }
    }
    composable<HomeTabs.ProfileTab> {
        Profile(navController, modifier)
    }
}
