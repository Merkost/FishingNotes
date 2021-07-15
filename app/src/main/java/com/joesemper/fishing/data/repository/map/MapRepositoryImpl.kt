package com.joesemper.fishing.data.repository.map

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.model.common.MapMarker
import com.joesemper.fishing.model.common.UserCatch
import kotlinx.coroutines.flow.Flow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {
    override suspend fun addNewCatch(userCatch: UserCatch) = provider.addNewCatch(userCatch)
    override fun getAllUserMarkers(): Flow<MapMarker> = provider.getAllUserMarkers()
    override suspend fun deleteMarker(userCatch: UserCatch) = provider.deleteMarker(userCatch)
}