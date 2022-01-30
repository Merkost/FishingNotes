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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class FirebaseOfflineRepositoryImpl(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val dbCollections: RepositoryCollections,
) :
    OfflineRepository {

    override fun getAllUserMarkersList() = callbackFlow<List<UserMapMarker>> {
        db.disableNetwork().addOnSuccessListener {
            dbCollections.getUserMapMarkersCollection().get().addOnSuccessListener {
                val markers = it.toObjects(UserMapMarker::class.java)
                trySend(markers)
                db.enableNetwork()
            }
        }
        awaitClose { }
    }

    override fun getAllUserCatchesList() = callbackFlow<List<UserCatch>> {
        db.disableNetwork().addOnSuccessListener {
            dbCollections.getUserMapMarkersCollection().get().addOnSuccessListener {
                val result = mutableListOf<UserCatch>()

                launch {
                    if (it.documents.size > 0) {
                        getCatchesFromDoc(it.documents).take(it.documents.size).onCompletion {
                            trySend(result)
                            db.enableNetwork()
                        }.collect {
                            result.addAll(it)
                        }
                    }
                }

            }
        }
        awaitClose { }
    }

}