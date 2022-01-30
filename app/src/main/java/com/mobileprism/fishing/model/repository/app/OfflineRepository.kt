package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface OfflineRepository {
    fun getAllUserMarkersList(): Flow<List<UserMapMarker>>
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
}