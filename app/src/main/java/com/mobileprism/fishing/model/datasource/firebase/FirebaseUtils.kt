package com.mobileprism.fishing.model.datasource.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

const val CATCHES_COLLECTION = "catches"

@ExperimentalCoroutinesApi
fun getCatchesFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
    val registrations = mutableListOf<ListenerRegistration>()
    docs.forEach { doc ->
        val reg = doc.reference.collection(CATCHES_COLLECTION)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.d("Fishing", "getCatchesFromDoc snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    trySend(snapshots.toObjects(UserCatch::class.java))
                } else {
                    trySend(listOf())
                }
            }
        registrations.add(reg)
    }
    awaitClose { registrations.forEach { it.remove() } }
}
