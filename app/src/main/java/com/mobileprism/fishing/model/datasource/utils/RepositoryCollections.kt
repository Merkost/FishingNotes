package com.mobileprism.fishing.model.datasource.utils

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.CATCHES_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.MARKERS_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.USERS_COLLECTION
import com.mobileprism.fishing.utils.getCurrentUserId

class RepositoryCollections(val db: FirebaseFirestore = Firebase.firestore) {

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    fun getUsersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION)
    }

    fun getUserMapMarkersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION).document(getCurrentUserId())
            .collection(MARKERS_COLLECTION)
    }

    fun getUserCatchesCollection(usermarkerId: String): CollectionReference {
        return db.collection(USERS_COLLECTION).document(getCurrentUserId())
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

