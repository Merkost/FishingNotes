package com.joesemper.fishing.model.repository.map

import com.joesemper.fishing.model.entity.common.UserCatch
import com.joesemper.fishing.model.entity.states.AddNewCatchState
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addNewCatch(userCatch: UserCatch): StateFlow<AddNewCatchState>
    suspend fun deleteMarker(userCatch: UserCatch)
    suspend fun getAllUserMarkers(): StateFlow<List<UserCatch?>>
}