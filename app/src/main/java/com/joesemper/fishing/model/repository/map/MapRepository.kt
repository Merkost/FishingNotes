package com.joesemper.fishing.model.repository.map

import com.joesemper.fishing.model.entity.map.Marker
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addMarker(marker: Marker)
    suspend fun getAllUserMarkers(): StateFlow<List<Marker?>>
}