package com.joesemper.fishing.model.db

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.db.storage.Storage
import com.joesemper.fishing.model.entity.map.UserMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

class CloudFireStoreDatabaseImpl(private val cloudStorage: Storage) : DatabaseProvider {

    private val db = Firebase.firestore

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    private val allUserMarkers: MutableStateFlow<MutableList<UserMarker?>> =
        MutableStateFlow(mutableListOf(null))

    override suspend fun addMarker(userMarker: UserMarker) {
        if (userMarker.photoUri.isNotBlank()) {
            runCatching {
                cloudStorage.uploadPhoto(userMarker.photoUri.toUri()).collect{
                    userMarker.downloadPhotoLink = it
                    getUserMarkersCollection().document(userMarker.id).set(userMarker)
                }
            }
        } else {
            getUserMarkersCollection().document(userMarker.id).set(userMarker)
        }
    }

    override suspend fun getAllUserMarkers(): StateFlow<List<UserMarker?>> {
        subscribeOnUserMarkersCollection()
        allUserMarkers.value = mutableListOf(null)
        return allUserMarkers
    }

    override suspend fun deleteMarker(markerId: String) {
        getUserMarkersCollection().document(markerId).delete()
    }

    private fun subscribeOnUserMarkersCollection() {
        getUserMarkersCollection().addSnapshotListener { documents, error ->
            if (documents != null) {
                runBlocking {
                    allUserMarkers.emit(documents.toObjects<UserMarker>().toMutableList())
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
    }


}