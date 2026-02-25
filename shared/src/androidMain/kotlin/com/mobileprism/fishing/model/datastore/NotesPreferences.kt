package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.ui.utils.enums.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import kotlinx.coroutines.flow.Flow

interface NotesPreferences {
    val getPlacesSortValue: Flow<PlacesSortValues>
    val getCatchesSortValue: Flow<CatchesSortValues>
    suspend fun savePlacesSortValue(placesSortValue: PlacesSortValues)
    suspend fun saveCatchesSortValue(catchesSortValue: CatchesSortValues)
}