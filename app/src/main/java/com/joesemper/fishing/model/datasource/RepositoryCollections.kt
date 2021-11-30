package com.joesemper.fishing.model.datasource

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.datasource.RepositoryConstants.CATCHES_COLLECTION
import com.joesemper.fishing.model.datasource.RepositoryConstants.MARKERS_COLLECTION
import com.joesemper.fishing.model.datasource.RepositoryConstants.USERS_COLLECTION
import com.joesemper.fishing.utils.getCurrentUserId

class RepositoryCollections(val db: FirebaseFirestore = Firebase.firestore) {

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

