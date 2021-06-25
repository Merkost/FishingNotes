package com.joesemper.fishing.model.repository.map

import com.joesemper.fishing.model.db.DatabaseProvider
import com.joesemper.fishing.model.entity.map.UserMarker
import kotlinx.coroutines.flow.StateFlow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {
    override suspend fun addMarker(userMarker: UserMarker) = provider.addMarker(userMarker)
    override suspend fun getAllUserMarkers(): StateFlow<List<UserMarker?>> = provider.getAllUserMarkers()
    override suspend fun deleteMarker(markerId: String) = provider.deleteMarker(markerId)
}