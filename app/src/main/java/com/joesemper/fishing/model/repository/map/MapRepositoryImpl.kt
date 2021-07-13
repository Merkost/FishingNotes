package com.joesemper.fishing.model.repository.map

import com.joesemper.fishing.model.db.DatabaseProvider
import com.joesemper.fishing.model.entity.common.UserCatch
import kotlinx.coroutines.flow.StateFlow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {
    override suspend fun addNewCatch(userCatch: UserCatch) = provider.addNewCatch(userCatch)
    override suspend fun getAllUserMarkers(): StateFlow<List<UserCatch?>> = provider.getAllUserMarkers()
    override suspend fun deleteMarker(userCatch: UserCatch) = provider.deleteMarker(userCatch)
}