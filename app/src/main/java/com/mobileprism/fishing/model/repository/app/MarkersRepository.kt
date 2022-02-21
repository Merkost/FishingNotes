package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.LiteProgress
import com.mobileprism.fishing.model.entity.common.Note
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.MapMarker
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarkersRepository {
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
    fun getAllUserMarkers(): Flow<MapMarker>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>

    suspend fun updateUserMarkerNote(markerId: String, currentNotes: List<Note>, note: Note)
            : StateFlow<BaseViewState<List<Note>>>
    suspend fun deleteMarkerNote(markerId: String, currentNotes: List<Note>, noteToDelete: Note)
            : StateFlow<BaseViewState<List<Note>>>

    suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean): StateFlow<LiteProgress>

    suspend fun deleteMarker(userMapMarker: UserMapMarker)
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>


}
