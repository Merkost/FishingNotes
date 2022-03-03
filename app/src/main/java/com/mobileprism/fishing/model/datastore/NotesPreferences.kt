package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.ui.utils.enums.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import kotlinx.coroutines.flow.Flow

interface NotesPreferences {
    val getPlacesSortValue: Flow<PlacesSortValues>
    val getCatchesSortValue: Flow<CatchesSortValues>
    suspend fun savePlacesSortValue(placesSortValue: PlacesSortValues)
    suspend fun saveCatchesSortValue(catchesSortValue: CatchesSortValues)
}