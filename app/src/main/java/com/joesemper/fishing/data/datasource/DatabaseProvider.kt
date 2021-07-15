package com.joesemper.fishing.data.datasource

import com.joesemper.fishing.model.common.MapMarker
import com.joesemper.fishing.model.common.UserCatch
import com.joesemper.fishing.model.states.AddNewCatchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DatabaseProvider {
    suspend fun addNewCatch(userCatch: UserCatch): StateFlow<AddNewCatchState>
    suspend fun deleteMarker(userCatch: UserCatch)
    fun getAllUserMarkers(): Flow<MapMarker>
}