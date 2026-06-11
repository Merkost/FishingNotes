package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.MapMarker
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker
) : MarkersRepository, AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob())

    private val markersSnapshotFlow: Flow<QuerySnapshot> by lazy {
        dbCollections.getUserMapMarkersCollection()
            .snapshots
            .shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)
    }

    override fun getAllUserMarkers(): Flow<ContentState<MapMarker>> = channelFlow {
        markersSnapshotFlow
            .collect { snapshot ->
                for (dc in snapshot.documentChanges) {
                    try {
                        val mapMarker = dc.document.data<UserMapMarker>()
                        val state = when (dc.type) {
                            ChangeType.ADDED -> ContentState.ADDED<MapMarker>(mapMarker)
                            ChangeType.MODIFIED -> ContentState.MODIFIED<MapMarker>(mapMarker)
                            ChangeType.REMOVED -> ContentState.DELETED<MapMarker>(mapMarker)
                            else -> null
                        }
                        if (state != null) send(state)
                    } catch (_: Exception) { }
                }
            }
    }

    override fun getAllUserMarkersList(): Flow<List<UserMapMarker>> {
        return markersSnapshotFlow
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<UserMapMarker>() } catch (_: Exception) { null }
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun saveNewNote(
        markerId: String,
        newNote: Note
    ): Result<Unit> {
        return try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .updateFields { "notes" to FieldValue.arrayUnion(newNote) }
            analyticsTracker.logEvent(AnalyticsEvent.AddMarkerNote)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotes(
        markerId: String, notes: List<Note>
    ): Result<Unit> {
        return try {
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .updateFields { "notes" to notes }
            analyticsTracker.logEvent(AnalyticsEvent.EditMarkerNote)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeMarkerVisibility(
        marker: UserMapMarker,
        changeTo: Boolean
    ): Result<Unit> {
        return try {
            dbCollections.getUserMapMarkersCollection().document(marker.id)
                .updateFields { "visible" to changeTo }
            analyticsTracker.logEvent(AnalyticsEvent.MarkerVisibilityChange)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    override suspend fun deleteMarker(userMapMarker: UserMapMarker): Result<Unit> {
        return try {
            dbCollections.getUserMapMarkersCollection().document(userMapMarker.id).delete()
            analyticsTracker.logEvent(AnalyticsEvent.DeleteMarker)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun close() {
        scope.cancel()
    }
}
