package com.mobileprism.fishing.model.datasource.room

import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.datasource.room.dao.MapMarkersDao
import kotlinx.coroutines.flow.*
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

class LocalMarkersRepositoryImpl(
    private val markersDao: MapMarkersDao,
    private val firebaseAnalytics: FirebaseAnalytics,
) : MarkersRepository {

    override fun getAllUserMarkersList() = channelFlow {
        markersDao.getMapMarkers().collect {
            send(it)
        }
    }

    override suspend fun saveNewNote(
        markerId: String,
        newNote: Note
    ): Flow<Result<Unit>> = flow {
        val marker = markersDao.getMarkerById(markerId).first()
        val newMarker = marker.copy(
            notes = marker.notes.toMutableList().apply { add(newNote) }
        )
        markersDao.updateMapMarker(newMarker)
        emit(Result.success(Unit))
    }

    override suspend fun updateNotes(
        markerId: String, notes: List<Note>
    ): Flow<Result<Unit>> = flow {
        val marker = markersDao.getMarkerById(markerId).first()
        val newMarker = marker.copy(notes = notes)
        markersDao.updateMapMarker(newMarker)
        emit(Result.success(Unit))
    }

    override suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean) = flow {
        val marker = markersDao.getMarkerById(marker.id).first()
        val newMarker = marker.copy(visible = changeTo)
        markersDao.updateMapMarker(newMarker)
        emit(Result.success(Unit))
    }

    override suspend fun getMapMarker(markerId: String) =
        Result.success(markersDao.getMarkerById(markerId).first())


    override suspend fun addNewMarker(newMarker: UserMapMarker) = suspendCoroutine<Result<Unit>> { continuation ->
       val result = suspend { Result.success(markersDao.addMapMarker(newMarker)) }
        result.startCoroutine(continuation)
    }
    //emit(Result.success(markersDao.addMapMarker(newMarker)))


    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        markersDao.deleteMapMarker(userMapMarker)
    }

}
