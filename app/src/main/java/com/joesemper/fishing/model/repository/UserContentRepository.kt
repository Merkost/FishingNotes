package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.Content
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserContentRepository {
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllUserMarkers(): Flow<MapMarker>
    fun getAllUserMarkersList(): Flow<List<UserMapMarker>>
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserContentList(): Flow<List<Content>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>


    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>
    suspend fun deleteMarker(userCatch: UserCatch)
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
}