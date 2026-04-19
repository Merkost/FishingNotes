package com.mobileprism.fishing.ui

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.weather.DailyWeatherData
import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph

/**
 * Destinations used in the FishingNotesApp.
 */
object MainDestinations {

    @Serializable
    data object Settings

    @Serializable
    data object AboutApp

    @Serializable
    data object EditProfile

    @Serializable
    data class NewCatch(val place: UserMapMarker? = null)

    @Serializable
    data class Place(val marker: UserMapMarker)

    @Serializable
    data class Catch(val catch: UserCatch)

    @Serializable
    data class DailyWeather(val data: DailyWeatherData)

    @Serializable
    data class Map(
        val isAddingNewPlace: Boolean = false,
        val place: UserMapMarker? = null
    )
}

object HomeTabs {
    @Serializable
    data object NotesTab

    @Serializable
    data class WeatherTab(val place: UserMapMarker? = null)

    @Serializable
    data object ProfileTab
}
