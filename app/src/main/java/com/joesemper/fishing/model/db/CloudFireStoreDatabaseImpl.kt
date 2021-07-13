package com.joesemper.fishing.model.db

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.db.storage.Storage
import com.joesemper.fishing.model.entity.common.UserCatch
import com.joesemper.fishing.model.entity.states.AddNewCatchState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class CloudFireStoreDatabaseImpl(private val cloudStorage: Storage) : DatabaseProvider {

    private val db = Firebase.firestore

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    private val allUserMarkers: MutableStateFlow<MutableList<UserCatch?>> =
        MutableStateFlow(mutableListOf(null))

    private val addNewMarkerState = MutableStateFlow<AddNewCatchState>(AddNewCatchState.Loading())

    override suspend fun addNewCatch(userCatch: UserCatch): StateFlow<AddNewCatchState> {
        addNewMarkerState.value = AddNewCatchState.Loading()
        addPhotosToDb(userCatch)
        return addNewMarkerState
    }

    private suspend fun addPhotosToDb(userCatch: UserCatch) {
        val photoUris = userCatch.photoUris
        if (photoUris.isNotEmpty()) {
            try {
                runCatching {
                    val uris = photoUris.map { it.toUri() }
                    cloudStorage.uploadPhotos(uris)
                        .take(photoUris.size)
                        .onCompletion {
                            getUserMarkersCollection().document(userCatch.id).set(userCatch)
                                .addOnCompleteListener {
                                    addNewMarkerState.value = AddNewCatchState.Success
                                }
                        }
                        .collect { downloadLink ->
                            userCatch.downloadPhotoLinks.add(downloadLink)
                            addNewMarkerState.value = AddNewCatchState.Loading()
                        }
                }
            } catch (e: Throwable) {
                addNewMarkerState.value = AddNewCatchState.Error(e)
            }
        } else {
            getUserMarkersCollection().document(userCatch.id).set(userCatch)
        }
    }

    override suspend fun getAllUserMarkers(): StateFlow<List<UserCatch?>> {
        subscribeOnUserMarkersCollection()
        allUserMarkers.value = mutableListOf(null)
        return allUserMarkers
    }

    override suspend fun deleteMarker(userCatch: UserCatch) {
//        cloudStorage.deletePhoto(userCatch.downloadPhotoLink)
//        getUserMarkersCollection().document(userCatch.id).delete()
    }

    private fun subscribeOnUserMarkersCollection() {
        getUserMarkersCollection().addSnapshotListener { documents, error ->
            if (documents != null) {
                runBlocking {
                    allUserMarkers.emit(documents.toObjects<UserCatch>().toMutableList())
                }
            }
        }
    }

    private fun getUserMarkersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION).document(currentUser?.uid!!)
            .collection(MARKERS_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MARKERS_COLLECTION = "markers"
        private const val CATCHES_COLLECTION = "catches"
    }


}