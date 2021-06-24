package com.joesemper.fishing.model.db

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.entity.map.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import java.util.*

class CloudFireStoreDatabaseImpl(private val context: Context) : DatabaseProvider {

    private val db = Firebase.firestore

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    private val allUserMarkers: MutableStateFlow<MutableList<Marker?>> =
        MutableStateFlow(mutableListOf(null))

    override suspend fun addMarker(marker: Marker) {
        getUserMarkersCollection().document(marker.id).set(marker)
    }

    override suspend fun getAllUserMarkers(): StateFlow<List<Marker?>> {
        subscribeOnUserMarkersCollection()
        allUserMarkers.value = mutableListOf(null)
        return allUserMarkers
    }

    private fun subscribeOnUserMarkersCollection() {
        getUserMarkersCollection().addSnapshotListener { documents, error ->
            if (documents != null) {
                runBlocking {
                    allUserMarkers.emit(documents.toObjects<Marker>().toMutableList())
                }
            }
        }

    }

    private fun getUserMarkersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION).document(currentUser?.uid!!).collection(MARKERS_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MARKERS_COLLECTION = "markers"
    }


}