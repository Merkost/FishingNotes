package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.entity.common.ContentState
import com.mobileprism.fishing.model.entity.common.LiteProgress
import com.mobileprism.fishing.model.entity.common.Note
import com.mobileprism.fishing.model.entity.content.MapMarker
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.model.mappers.MapMarkerMapper
import com.mobileprism.fishing.model.mappers.MarkerNoteMapper
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val context: Context
) : MarkersRepository {

    override fun getAllUserMarkers() = channelFlow {
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
        /*listeners.add(
            dbCollections.getMapMarkersCollection()
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersSnapshotListener(this))
        )*/

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    /*override fun getAllUserMarkers() = channelFlow<ContentState<MapMarker>> {
        val listeners = mutableListOf<Task<QuerySnapshot>>()

        //UserMarkers
        listeners.add(
            dbCollections.getUserMapMarkersCollection().get().addOnSuccessListener(
                getUserMarkersStateListener(this)
            )
        )
        //AllPublicMarkers
        *//*listeners.add(
            dbCollections.getMapMarkersCollection()
                .whereNotEqualTo("userId", getCurrentUserId())
                .addSnapshotListener(getMarkersSnapshotListener(this))
        )*//*

        awaitClose {
            listeners.forEach { listeners.remove(it) }
        }
    }

    private fun getUserMarkersStateListener(scope: ProducerScope<ContentState<MapMarker>>)
    : OnSuccessListener<in QuerySnapshot>  =
        OnSuccessListener<QuerySnapshot> { task ->
        scope.launch {
            getMarkersStateFromDoc(task.documents).collect {
                scope.trySend(it)
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun getMarkersStateFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
        docs.forEach { doc ->
            doc.reference.collection(MARKERS_COLLECTION)
                .addSnapshotListener { snapshots, error ->
                    if (snapshots != null) {

                        val result = ContentStateOld<UserCatch>()

                        for (dc in snapshots.documentChanges) {
                            val userCatch = dc.document.toObject<UserCatch>()
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    result.added.add(userCatch)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    result.modified.add(userCatch)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    result.deleted.add(userCatch)
                                }
                            }
                        }

                        trySend(result)
                    }
                }
        }
        awaitClose { }
    }*/

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
    }

    override suspend fun deleteMarkerNote(
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
    }

    override suspend fun changeMarkerVisibility(marker: UserMapMarker, changeTo: Boolean)
            : StateFlow<LiteProgress> {
        val flow = MutableStateFlow<LiteProgress>(LiteProgress.Loading)
        val documentRef = dbCollections.getUserMapMarkersCollection().document(marker.id)
        val task = documentRef.update("visible", changeTo)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseAnalytics.logEvent("marker_visibility_change", null)
                flow.tryEmit(LiteProgress.Complete)
            }
            if (it.isCanceled || it.exception != null) {
                flow.tryEmit(LiteProgress.Error(task.exception?.cause))
            }
        }
        return flow
    }

    override fun getMapMarker(markerId: String) = callbackFlow {
        val listener = dbCollections.getUserMapMarkersCollection().document(markerId)
            .addSnapshotListener { value, _ ->
                trySend(value?.toObject<UserMapMarker>())
            }
        awaitClose { listener.remove() }
    }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<ContentState<MapMarker>>) =
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
                            scope.trySend(ContentState.ADDED<MapMarker>(mapMarker))
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(ContentState.MODIFIED<MapMarker>(mapMarker))
                        }
                        DocumentChange.Type.REMOVED -> {
                            val mapMarker = dc.document.toObject<UserMapMarker>()
                            scope.trySend(ContentState.DELETED<MapMarker>(mapMarker))
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


    override fun addNewMarker(newMarker: RawMapMarker) = flow<Result<Unit>> {
        val mapMarker = MapMarkerMapper().mapRawMapMarker(newMarker)
        try {
            //TODO: saveMarker method improvement
            saveMarker(mapMarker)
        } catch (error: Throwable) {
            emit(Result.failure(error))
        }
        emit(Result.success(Unit))
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