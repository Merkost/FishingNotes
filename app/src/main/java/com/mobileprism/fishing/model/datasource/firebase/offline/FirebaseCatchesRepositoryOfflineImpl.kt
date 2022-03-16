package com.mobileprism.fishing.model.datasource.firebase.offline

import android.net.Uri
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.model.datasource.firebase.getCatchesFromDoc
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.entity.common.ContentStateOld
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FirebaseCatchesRepositoryOfflineImpl(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
) : CatchesRepository {

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

    override fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>> {
        TODO("Not yet implemented")
    }

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        TODO("Not yet implemented")
    }

    override fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserCatch(
        markerId: String,
        catchId: String,
        data: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        TODO("Not yet implemented")
    }

    override fun addNewCatch(markerId: String, newCatch: UserCatch) = callbackFlow {
        dbCollections.getUserCatchesCollection(markerId).document(newCatch.id).set(newCatch)
        firebaseAnalytics.logEvent("new_catch_offline", null)
        trySend(Result.success(null))
        incrementNumOfCatches(markerId)
        awaitClose { }
    }

    private fun incrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(1))
    }

    private fun decrementNumOfCatches(markerId: String) {
        dbCollections.getUserMapMarkersCollection().document(markerId)
            .update("catchesCount", FieldValue.increment(-1))
    }
}