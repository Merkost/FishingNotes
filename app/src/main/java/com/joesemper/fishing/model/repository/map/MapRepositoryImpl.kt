package com.joesemper.fishing.model.repository.map

import com.joesemper.fishing.model.db.DatabaseProvider
import com.joesemper.fishing.model.entity.map.Marker
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {
    override suspend fun addMarker(marker: Marker) = provider.addMarker(marker)
    override suspend fun getAllUserMarkers(): StateFlow<List<Marker?>> = provider.getAllUserMarkers()
}