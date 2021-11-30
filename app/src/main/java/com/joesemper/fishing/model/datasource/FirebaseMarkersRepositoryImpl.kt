package com.joesemper.fishing.model.datasource

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.entity.common.CatchesContentState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.mappers.MapMarkerMapper
import com.joesemper.fishing.model.mappers.UserCatchMapper
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.utils.getCurrentUserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FirebaseMarkersRepositoryImpl(
    private val dbCollections: RepositoryCollections,
    private val cloudPhotoStorage: PhotoStorage
) : MarkersRepository {

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
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


    @ExperimentalCoroutinesApi
    override fun getMapMarker(markerId: String) = channelFlow {
        val listener = dbCollections.getUserMapMarkersCollection().document(markerId)
            .addSnapshotListener { value, error ->
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
                        DocumentChange.Type.MODIFIED -> {
                        }
                        DocumentChange.Type.REMOVED -> {
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


    @ExperimentalCoroutinesApi
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

    private suspend fun savePhotos(
        photos: List<Uri>,
        progressFlow: MutableStateFlow<Progress>
    ) =
        cloudPhotoStorage.uploadPhotos(photos, progressFlow)

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) {
        dbCollections.getUserMapMarkersCollection().document(userMapMarker.id).delete()
    }

}