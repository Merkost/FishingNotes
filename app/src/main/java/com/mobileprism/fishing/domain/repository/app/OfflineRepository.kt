package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface OfflineRepository {
    fun getAllUserMarkersList(): Flow<List<UserMapMarker>>
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
}