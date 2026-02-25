package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.MARKERS_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.USERS_COLLECTION
import kotlinx.coroutines.flow.Flow

class FirebaseMarkersPagedRepository(
    private val coreRepo: FirebaseMarkersRepositoryImpl,
    private val authRepository: AuthRepository
) : MarkersRepositoryPaged, MarkersRepository by coreRepo {

    private val googleDb = com.google.firebase.Firebase.firestore
    private val googleMarkersCollection
        get() = googleDb.collection(USERS_COLLECTION)
            .document(authRepository.getCurrentUserId())
            .collection(MARKERS_COLLECTION)

    override fun getAllUserMarkersListPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserMapMarker>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            MarkersPagingSource(googleMarkersCollection, sortField, sortDirection)
        }
    ).flow
}
