package com.joesemper.fishing.data.datasource

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.common.MapMarker
import com.joesemper.fishing.model.common.UserCatch
import com.joesemper.fishing.model.common.UserMarker
import com.joesemper.fishing.model.states.AddNewCatchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class CloudFireStoreDatabaseImpl(private val cloudStorage: Storage) : DatabaseProvider {

    private val db = Firebase.firestore

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    private val addNewCatchState = MutableStateFlow<AddNewCatchState>(AddNewCatchState.Loading())

    @ExperimentalCoroutinesApi
    override fun getAllUserMarkers() = callbackFlow {
        val catchesCollection = getCatchesCollection()
        val markersCollection = getMarkersCollection()

        val snapshotListenerUserCatches = catchesCollection
            .whereEqualTo("userId", currentUser!!.uid)
            .addSnapshotListener(getCatchSnapshotListener(this))

        val snapshotListenerPublicCatches = catchesCollection
            .whereEqualTo("isPublic", true)
            .whereNotEqualTo("userId", currentUser!!.uid)
            .addSnapshotListener(getCatchSnapshotListener(this))

        val snapshotListenerUserMarkers = markersCollection
            .whereEqualTo("userId", currentUser!!.uid)
            .addSnapshotListener(getMarkersSnapshotListener(this))

        val snapshotListenerPublicMarkers = markersCollection
            .whereEqualTo("isPublic", true)
            .whereNotEqualTo("userId", currentUser!!.uid)
            .addSnapshotListener(getMarkersSnapshotListener(this))


        awaitClose {
            snapshotListenerUserCatches.remove()
            snapshotListenerPublicCatches.remove()
            snapshotListenerUserMarkers.remove()
            snapshotListenerPublicMarkers.remove()
        }
    }

    @ExperimentalCoroutinesApi
    private fun getCatchSnapshotListener(scope: ProducerScope<MapMarker>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Catch snapshot listener", error)
                return@EventListener
            }

            snapshots?.forEach { documentSnapshot ->
                val userCatch = documentSnapshot.toObject<UserCatch>()
                scope.trySend(userCatch)
            }
        }

    @ExperimentalCoroutinesApi
    private fun getMarkersSnapshotListener(scope: ProducerScope<MapMarker>) =
        EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Log.d("Fishing", "Marker snapshot listener", error)
                return@EventListener
            }

            snapshots?.forEach { documentSnapshot ->
                val userMarker = documentSnapshot.toObject<UserMarker>()
                scope.trySend(userMarker)
            }
        }

    override suspend fun addNewCatch(userCatch: UserCatch): StateFlow<AddNewCatchState> {
        addNewCatchState.value = AddNewCatchState.Loading()

        userCatch.userId = currentUser!!.uid
        val photoUris = userCatch.photoUris

        if (photoUris.isNotEmpty()) {
            val photoDownloadLinks = savePhotosToDb(photoUris)
            userCatch.downloadPhotoLinks.addAll(photoDownloadLinks)
        }
        saveMarker(userCatch.marker)
        userCatch.userMarkerId = userCatch.marker?.id
        getCatchesCollection().document(userCatch.id).set(userCatch).addOnCompleteListener {
            addNewCatchState.value = AddNewCatchState.Success
        }

        return addNewCatchState
    }

    private suspend fun savePhotosToDb(photoUris: List<String>): List<String> {
        val downloadLinks = mutableListOf<String>()
        val uris = photoUris.map { it.toUri() }
        try {
            cloudStorage.uploadPhotos(uris)
                .take(photoUris.size)
                .collect { downloadLink ->
                    downloadLinks.add(downloadLink)
                }
        } catch (e: Throwable) {
            addNewCatchState.value = AddNewCatchState.Error(e)
        }
        return downloadLinks
    }

    private fun saveMarker(userMarker: UserMarker?) {
        if (userMarker != null) {
            getMarkersCollection().document(userMarker.id).set(userMarker)
        }

    }

    override suspend fun deleteMarker(userCatch: UserCatch) {
//        cloudStorage.deletePhoto(userCatch.downloadPhotoLink)
//        getUserMarkersCollection().document(userCatch.id).delete()
    }

    private fun getMarkersCollection(): CollectionReference {
        return db.collection(MARKERS_COLLECTION)
    }

    private fun getCatchesCollection(): CollectionReference {
        return db.collection(CATCHES_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }


}