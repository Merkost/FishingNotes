package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.LiteProgress
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker
) : MarkersRepository {

    override fun getAllUserMarkers(): Flow<ContentState<MapMarker>> = channelFlow {
        dbCollections.getUserMapMarkersCollection()
            .snapshots
            .collect { snapshot ->
                for (dc in snapshot.documentChanges) {
                    try {
                        val mapMarker = dc.document.data<UserMapMarker>()
                        val state = when (dc.type) {
                            ChangeType.ADDED -> ContentState.ADDED<MapMarker>(mapMarker)
                            ChangeType.MODIFIED -> ContentState.MODIFIED<MapMarker>(mapMarker)
                            ChangeType.REMOVED -> ContentState.DELETED<MapMarker>(mapMarker)
                        }
                        send(state)
                    } catch (_: Exception) {
                        // Skip corrupt document
                    }
                }
            }
    }

    override fun getAllUserMarkersList(): Flow<List<UserMapMarker>> {
        return dbCollections.getUserMapMarkersCollection()
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<UserMapMarker>() }
            }
    }

    override suspend fun saveNewNote(
        markerId: String,
        newNote: Note
    ): Flow<Result<Unit>> = flow {
        try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("notes" to FieldValue.arrayUnion(newNote))
            analyticsTracker.logEvent(AnalyticsEvent.AddMarkerNote)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateNotes(
        markerId: String, notes: List<Note>
    ): Flow<Result<Unit>> = flow {
        try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("notes" to notes)
            analyticsTracker.logEvent(AnalyticsEvent.EditMarkerNote)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun changeMarkerVisibility(
        marker: UserMapMarker,
        changeTo: Boolean
    ): StateFlow<LiteProgress> {
        val flow = MutableStateFlow<LiteProgress>(LiteProgress.Loading)
        try {
            dbCollections.getUserMapMarkersCollection().document(marker.id)
                .update("visible" to changeTo)
            analyticsTracker.logEvent(AnalyticsEvent.MarkerVisibilityChange)
            flow.tryEmit(LiteProgress.Complete)
        } catch (e: Exception) {
            flow.tryEmit(LiteProgress.Error(e.cause))
        }
        return flow
    }

    override suspend fun getMapMarker(markerId: String): Result<UserMapMarker> {
        return try {
            val doc = dbCollections.getUserMapMarkersCollection().document(markerId).get()
            if (doc.exists) {
                Result.success(doc.data<UserMapMarker>())
            } else {
                Result.failure(Throwable("Marker not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addNewMarker(newMarker: UserMapMarker): Result<Unit> {
        return try {
            dbCollections.getUserMapMarkersCollection().document(newMarker.id).set(newMarker)
            analyticsTracker.logEvent(AnalyticsEvent.NewMarker)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        dbCollections.getUserMapMarkersCollection().document(userMapMarker.id).delete()
        analyticsTracker.logEvent(AnalyticsEvent.DeleteMarker)
    }
}
