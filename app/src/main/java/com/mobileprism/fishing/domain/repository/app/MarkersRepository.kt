package com.mobileprism.fishing.domain.repository.app

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface MarkersRepository {
    suspend fun getMapMarker(markerId: String): Result<UserMapMarker>
    fun getAllUserMarkersList(): Flow<List<UserMapMarker>>

    suspend fun saveNewNote(markerId: String, newNote: Note): Flow<Result<Unit>>
    suspend fun updateNotes(markerId: String, notes: List<Note>): Flow<Result<Unit>>

    suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean): Flow<Result<Unit>>

    suspend fun deleteMarker(userMapMarker: UserMapMarker)
    suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit>

}
