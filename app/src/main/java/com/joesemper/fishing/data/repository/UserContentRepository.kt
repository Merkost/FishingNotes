package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.data.entity.content.MapMarker
import com.joesemper.fishing.data.entity.content.UserCatch
import com.joesemper.fishing.data.entity.content.UserMapMarker
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.raw.RawUserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserContentRepository {
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllUserMarkers(): Flow<MapMarker>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>

    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>
    suspend fun deleteMarker(userCatch: UserCatch)
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
}