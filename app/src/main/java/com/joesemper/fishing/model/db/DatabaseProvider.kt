package com.joesemper.fishing.model.db

import com.joesemper.fishing.model.entity.map.UserMarker
import kotlinx.coroutines.flow.StateFlow

interface DatabaseProvider {
    suspend fun addMarker(userMarker: UserMarker)
    suspend fun deleteMarker(userMarker: UserMarker)
    suspend fun getAllUserMarkers(): StateFlow<List<UserMarker?>>
}