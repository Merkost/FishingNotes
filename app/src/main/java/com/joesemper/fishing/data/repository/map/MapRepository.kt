package com.joesemper.fishing.data.repository.map

import com.joesemper.fishing.model.common.MapMarker
import com.joesemper.fishing.model.common.UserCatch
import com.joesemper.fishing.model.states.AddNewCatchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addNewCatch(userCatch: UserCatch): StateFlow<AddNewCatchState>
    suspend fun deleteMarker(userCatch: UserCatch)
    fun getAllUserMarkers(): Flow<MapMarker>
}