package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import kotlinx.coroutines.flow.Flow

interface NotesPreferences {
    val getPlacesSortValue: Flow<PlacesSortValues>
    val getCatchesSortValue: Flow<CatchesSortValues>
    suspend fun savePlacesSortValue(placesSortValue: PlacesSortValues)
    suspend fun saveCatchesSortValue(catchesSortValue: CatchesSortValues)
}
