package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.CATCHES_COLLECTION
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.flow

class FirebaseOfflineRepositoryImpl(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val dbCollections: RepositoryCollections,
) : OfflineRepository {

    override fun getAllUserMarkersList() = flow<List<UserMapMarker>> {
        try {
            db.disableNetwork()
            val snapshot = dbCollections.getUserMapMarkersCollection().get()
            val markers = snapshot.documents.mapNotNull { doc ->
                try { doc.data<UserMapMarker>() } catch (_: Exception) { null }
            }
            emit(markers)
        } finally {
            try { db.enableNetwork() } catch (_: Exception) { }
        }
    }

    override fun getAllUserCatchesList() = flow<List<UserCatch>> {
        try {
            db.disableNetwork()
            val markersSnapshot = dbCollections.getUserMapMarkersCollection().get()
            val result = mutableListOf<UserCatch>()

            for (markerDoc in markersSnapshot.documents) {
                val catchesSnapshot = markerDoc.reference.collection(CATCHES_COLLECTION).get()
                val catches = catchesSnapshot.documents.mapNotNull { doc ->
                    try { doc.data<UserCatch>() } catch (_: Exception) { null }
                }
                result.addAll(catches)
            }

            emit(result)
        } finally {
            try { db.enableNetwork() } catch (_: Exception) { }
        }
    }
}
