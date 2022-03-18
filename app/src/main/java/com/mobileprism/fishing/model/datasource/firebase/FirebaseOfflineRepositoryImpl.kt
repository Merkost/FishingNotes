package com.mobileprism.fishing.model.datasource.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.OfflineRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseOfflineRepositoryImpl(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val dbCollections: RepositoryCollections,
) : OfflineRepository {

    override fun getAllUserMarkersList() = flow<List<UserMapMarker>> {
        db.disableNetwork().await()
        val collection = dbCollections.getUserMapMarkersCollection().get().await()
        val markers = collection.toObjects(UserMapMarker::class.java)
        emit(markers)
    }.onCompletion { db.enableNetwork() }

    override fun getAllUserCatchesList() = flow<List<UserCatch>> {
        db.disableNetwork().await()
        val collection = dbCollections.getUserMapMarkersCollection().get().await()
        val collectionSize = collection.documents.size
        val result = mutableListOf<UserCatch>()

        if (collectionSize > 0) {
            getCatchesFromDoc(collection.documents)
                .take(collectionSize)
                .onCompletion {
                    emit(result)
                    db.enableNetwork()
                }.collect {
                    result.addAll(it)
                }
        }
    }

}