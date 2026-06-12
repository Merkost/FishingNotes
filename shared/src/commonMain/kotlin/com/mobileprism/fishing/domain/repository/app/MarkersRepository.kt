package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface MarkersRepository {
    suspend fun getMapMarker(markerId: String): Result<UserMapMarker>
    fun getAllUserMarkers(): Flow<ContentState<MapMarker>>
    fun getAllUserMarkersList(): Flow<List<UserMapMarker>>

    suspend fun saveNewNote(markerId: String, newNote: Note): Result<Unit>
    suspend fun updateNotes(markerId: String, notes: List<Note>): Result<Unit>

    suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean): Result<Unit>

    suspend fun deleteMarker(userMapMarker: UserMapMarker): Result<Unit>
    suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit>
}
