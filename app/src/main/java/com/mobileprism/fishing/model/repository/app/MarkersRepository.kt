package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.model.entity.common.ContentState
import com.mobileprism.fishing.model.entity.common.LiteProgress
import com.mobileprism.fishing.model.entity.common.Note
import com.mobileprism.fishing.model.entity.content.MapMarker
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarkersRepository {
    suspend fun getMapMarker(markerId: String): Result<UserMapMarker>
    fun getAllUserMarkers(): Flow<ContentState<MapMarker>>
    fun getAllUserMarkersList(): Flow<List<MapMarker>>

    suspend fun saveNewNote(markerId: String, newNote: Note): Flow<Result<Unit>>
    suspend fun updateNotes(markerId: String, notes: List<Note>): Flow<Result<Unit>>

    suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean): StateFlow<LiteProgress>

    suspend fun deleteMarker(userMapMarker: UserMapMarker)
    fun addNewMarker(newMarker: RawMapMarker): Flow<Result<Unit>>


}
