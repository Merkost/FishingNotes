package com.joesemper.fishing.model.datasource.firebase

import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.datasource.utils.RepositoryCollections
import com.joesemper.fishing.model.entity.common.LiteProgress
import com.joesemper.fishing.model.entity.common.Note
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.mappers.MapMarkerMapper
import com.joesemper.fishing.model.mappers.MarkerNoteMapper
import com.joesemper.fishing.model.repository.PhotoStorage
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.utils.getCurrentUserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val cloudPhotoStorage: PhotoStorage
) : MarkersRepository {

    override fun getAllUserMarkers() = channelFlow<MapMarker> {
        val listeners = mutableListOf<ListenerRegistration>()

        //UserMarkers
        listeners.add(
            dbCollections.getUserMapMarkersCollection().addSnapshotListener(
                getMarkersSnapshotListener(
                    this
                )
            )
        )
        //AllPublicMarkers
        listeners.add(
            dbCollections.getMapMarkersCollection()
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersSnapshotListener(this))
        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    override fun getAllUserMarkersList() = channelFlow {
        val listeners = mutableListOf<ListenerRegistration>()
        listeners.add(
            dbCollections.getUserMapMarkersCollection()
                .addSnapshotListener(getMarkersListSnapshotListener(this))
        )

//        listeners.add(
//            getMapMarkersCollection()
//                .whereEqualTo("isPublic", true)
//                .whereNotEqualTo("userId", getCurrentUserId())
//                .addSnapshotListener(getMarkersListSnapshotListener(this))
//        )

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    override suspend fun updateUserMarkerNote(
        markerId: String,
        currentNotes: List<Note>,
        note: Note
    ): StateFlow<BaseViewState> {
        val flow = MutableStateFlow<BaseViewState>(BaseViewState.Loading())

        if (note.id.isEmpty()) {
            val newNote = MarkerNoteMapper().mapRawMarkerNote(note)
            dbCollections.getUserMapMarkersCollection().document(markerId)
                .update("notes", FieldValue.arrayUnion(newNote)).addOnCompleteListener {
                    it.exception?.let { exp ->
                        flow.tryEmit(BaseViewState.Error(exp))
                    }

                    if (it.isSuccessful) {
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
                        flow.tryEmit(BaseViewState.Success(newNotes))
                    }
                }
        }
        return flow
    }

    override suspend fun deleteMarkerNote(
        markerId: String,
        currentNotes: List<Note>,
        noteToDelete: Note
    ): StateFlow<BaseViewState> {
        val flow = MutableStateFlow<BaseViewState>(BaseViewState.Loading())

        val newNotes = currentNotes.toMutableList().apply {
            remove(noteToDelete)
        }

        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("notes", newNotes).addOnCompleteListener {
                it.exception?.let { exp ->
                    flow.tryEmit(BaseViewState.Error(exp))
                }
                if (it.isSuccessful) {
                    flow.tryEmit(BaseViewState.Success(newNotes))
                }
            }
        return flow
    }

    override suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean)
            : StateFlow<LiteProgress> {
        val flow = MutableStateFlow<LiteProgress>(LiteProgress.Loading)
        val documentRef = dbCollections.getUserMapMarkersCollection().document(marker.id)
        val task = documentRef.update("visible", changeTo)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                flow.tryEmit(LiteProgress.Complete)
            }
            if (it.isCanceled || it.exception != null) {
                flow.tryEmit(LiteProgress.Error(task.exception?.cause))
            }
        }
        return flow
    }


    override fun getMapMarker(markerId: String) = channelFlow {
        val listener = dbCollections.getUserMapMarkersCollection().document(markerId)
            .addSnapshotListener { value, _ ->
                trySend(value?.toObject<UserMapMarker>())
            }
        awaitClose { listener.remove() }
    }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<UserMapMarker>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }
            snapshots?.let { snapshot ->
                for (dc in snapshot.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(mapMarker)
                        }
                        DocumentChange.Type.MODIFIED -> {}
                        DocumentChange.Type.REMOVED -> {}
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


    override suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())
        val mapMarker = MapMarkerMapper().mapRawMapMarker(newMarker)
        try {
            saveMarker(mapMarker)
        } catch (error: Throwable) {
            flow.emit(Progress.Error(error))
        }
        flow.emit(Progress.Complete)
        return flow
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
            trySend(userMapMarker.id)
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
    }

}