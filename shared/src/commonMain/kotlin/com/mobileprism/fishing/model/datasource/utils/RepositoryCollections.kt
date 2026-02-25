package com.mobileprism.fishing.model.datasource.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.CATCHES_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.MARKERS_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.USERS_COLLECTION

class RepositoryCollections(
    val db: FirebaseFirestore = Firebase.firestore,
    private val authRepository: AuthRepository
) {

    init {
        db.settings = dev.gitlive.firebase.firestore.firestoreSettings {
            isPersistenceEnabled = true
        }
    }

    fun getUsersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION)
    }

    fun getUserMapMarkersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION).document(authRepository.getCurrentUserId())
            .collection(MARKERS_COLLECTION)
    }

    fun getUserCatchesCollection(usermarkerId: String): CollectionReference {
        return db.collection(USERS_COLLECTION).document(authRepository.getCurrentUserId())
            .collection(MARKERS_COLLECTION).document(usermarkerId)
            .collection(CATCHES_COLLECTION)
    }

    fun getMapMarkersCollection(): CollectionReference {
        return db.collection(MARKERS_COLLECTION)
    }

    fun getCatchesCollection(usermarkerId: String): CollectionReference {
        return db.collection(MARKERS_COLLECTION).document(usermarkerId)
            .collection(CATCHES_COLLECTION)
    }
}
