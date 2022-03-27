package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.LiteProgress
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
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
    suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit>


}
