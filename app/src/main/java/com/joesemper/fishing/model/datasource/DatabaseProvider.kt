package com.joesemper.fishing.model.datasource

import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DatabaseProvider {
    suspend fun addNewUser(user: User): StateFlow<Progress>
    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
    suspend fun deleteMarker(userCatch: UserCatch)
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllMarkers(): Flow<MapMarker>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}