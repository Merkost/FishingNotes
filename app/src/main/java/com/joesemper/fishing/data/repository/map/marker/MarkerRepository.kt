package com.joesemper.fishing.data.repository.map.marker

import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    suspend fun deleteMarker(userCatch: UserCatch)
}