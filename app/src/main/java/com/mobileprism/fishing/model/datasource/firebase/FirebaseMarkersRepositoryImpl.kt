package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.mobileprism.fishing.domain.entity.common.ContentState
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val context: Context
) : MarkersRepository {

    override fun getAllUserMarkersList() = channelFlow {
        val listeners = mutableListOf<ListenerRegistration>()
        listeners.add(
            dbCollections.getUserMapMarkersCollection()
                .addSnapshotListener(getMarkersListSnapshotListener(this))
        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    override suspend fun saveNewNote(
        markerId: String,
        newNote: Note
    ): Flow<Result<Unit>> = callbackFlow {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("notes", FieldValue.arrayUnion(newNote)).addOnCompleteListener {
                it.exception?.let { exp ->
                    trySend(Result.failure(exp))
                }

                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("add_marker_note", null)
                    trySend(Result.success(Unit))
                }
            }
        awaitClose()
    }

    override suspend fun updateNotes(
        markerId: String, notes: List<Note>
    ): Flow<Result<Unit>> = callbackFlow {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("notes", notes).addOnCompleteListener {
                it.exception?.let { exp ->
                    trySend(Result.failure(exp))
                }
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("edit_marker_note", null)
                    trySend(Result.success(Unit))
                }
            }
        awaitClose()
    }

    /*override suspend fun updateUserMarkerNote(
        markerId: String,
        currentNotes: List<Note>,
        note: Note
    ): StateFlow<BaseViewState<List<Note>>> {
        val flow = MutableStateFlow<BaseViewState<List<Note>>>(BaseViewState.Loading())

        if (note.id.isEmpty()) {
            val newNote = MarkerNoteMapper().mapRawMarkerNote(note)
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("notes", FieldValue.arrayUnion(newNote)).addOnCompleteListener {
                    it.exception?.let { exp ->
                        flow.tryEmit(BaseViewState.Error(exp))
                    }

                    if (it.isSuccessful) {
                        firebaseAnalytics.logEvent("add_marker_note", null)
                        flow.tryEmit(BaseViewState.Success(currentNotes + newNote))
                    }
                }
        } else {
            val newNotes = currentNotes.toMutableList().apply {
                set(indexOf(find { it.id == note.id }), note)
            }

            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("notes", newNotes).addOnCompleteListener {
                    it.exception?.let { exp ->
                        flow.tryEmit(BaseViewState.Error(exp))
                    }
                    if (it.isSuccessful) {
                        firebaseAnalytics.logEvent("edit_marker_note", null)
                        flow.tryEmit(BaseViewState.Success(newNotes))
                    }
                }
        }
        return flow
    }*/

    /*override suspend fun deleteMarkerNote(
        markerId: String,
        currentNotes: List<Note>,
        noteToDelete: Note
    ): StateFlow<BaseViewState<List<Note>>> {
        val flow = MutableStateFlow<BaseViewState<List<Note>>>(BaseViewState.Loading())

        val newNotes = currentNotes.toMutableList().apply {
            remove(noteToDelete)
        }

        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("notes", newNotes).addOnCompleteListener {
                it.exception?.let { exp ->
                    flow.tryEmit(BaseViewState.Error(exp))
                }
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("delete_marker_note", null)
                    flow.tryEmit(BaseViewState.Success(newNotes))
                }
            }
        return flow
    }*/

    override suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean) = callbackFlow {
        val documentRef = dbCollections.getUserMapMarkersCollection().document(marker.id)
        val task = documentRef.update("visible", changeTo)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseAnalytics.logEvent("marker_visibility_change", null)
                trySend(Result.success(Unit))
            }
            if (it.isCanceled || it.exception != null) {
                trySend(Result.failure(task.exception?.cause ?: Throwable()))
            }
        }
        awaitClose()
    }

    override suspend fun getMapMarker(markerId: String) =
        suspendCoroutine<Result<UserMapMarker>> { continuation ->

            dbCollections.getUserMapMarkersCollection().document(markerId).get()
                .addOnSuccessListener {
                    val result = it.toObject<UserMapMarker>()

                    result?.let { continuation.resume(Result.success(result)) }
                        ?: continuation.resume(Result.failure(Throwable()))

                }
//                .addSnapshotListener { value, _ ->
//                    trySend(value?.toObject<UserMapMarker>())
//                }
        }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<ContentState<UserMapMarker>>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }
            snapshots?.let { snapshot ->
                snapshot.documentChanges
                for (dc in snapshot.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(ContentState.ADDED<UserMapMarker>(mapMarker))
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(ContentState.MODIFIED<UserMapMarker>(mapMarker))
                        }
                        DocumentChange.Type.REMOVED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(ContentState.DELETED<UserMapMarker>(mapMarker))
                        }
                    }
                }
            }

        }

    @ExperimentalCoroutinesApi
    private fun getMarkersListSnapshotListener(scope: ProducerScope<List<UserMapMarker>>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }

            if (snapshots != null) {
                val markers = snapshots.toObjects(UserMapMarker::class.java)
                scope.trySend(markers)
            }
        }


    override suspend fun addNewMarker(newMarker: UserMapMarker) =
        suspendCoroutine<Result<Unit>> { continuation ->
            val task = dbCollections.getUserMapMarkersCollection().document(newMarker.id).set(newMarker)
            task.addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("new_marker", null)
                    continuation.resume(Result.success(Unit))
                }
                if (it.isCanceled || it.exception != null) {
                    continuation.resume(Result.failure(it.exception ?: Throwable()))
                }
            }
        }


    @ExperimentalCoroutinesApi
    private suspend fun saveMarker(userMapMarker: UserMapMarker?): String {
        var markerId = ""
        if (userMapMarker != null) {
            saveMarkerToDb(userMapMarker)
                .take(1)
                .collect {
                    markerId = it
                }
        }
        return markerId
    }

    @ExperimentalCoroutinesApi
    private fun saveMarkerToDb(userMapMarker: UserMapMarker) = callbackFlow {
        val documentRef = dbCollections.getUserMapMarkersCollection().document(userMapMarker.id)
        val task = documentRef.set(userMapMarker)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseAnalytics.logEvent("new_marker", null)
                trySend(userMapMarker.id)
            }
            //todo: Если ошибка?
            if (it.isCanceled || it.exception != null) {
                trySend("")
            }
        }
        awaitClose {}
    }

//    private suspend fun savePhotos(
//        photos: List<Uri>,
//        progressFlow: MutableStateFlow<Progress>
//    ) =
//        cloudPhotoStorage.uploadPhotos(photos, progressFlow)

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        dbCollections.getUserMapMarkersCollection().document(userMapMarker.id).delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseAnalytics.logEvent("delete_marker", null)
                }
            }
    }

}