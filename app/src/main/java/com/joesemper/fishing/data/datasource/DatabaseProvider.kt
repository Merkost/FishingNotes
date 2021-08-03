package com.joesemper.fishing.data.datasource

import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.data.entity.RawMapMarker
import com.joesemper.fishing.model.common.content.UserMapMarker
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.model.common.content.MapMarker
import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DatabaseProvider {
    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
    suspend fun deleteMarker(userCatch: UserCatch)
    fun getMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllMarkers(): Flow<MapMarker>
    fun getAllUserCatches(): Flow<List<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}