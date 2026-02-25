package com.mobileprism.fishing.domain.repository.app

import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface MarkersRepositoryPaged : MarkersRepository {
    fun getAllUserMarkersListPaged(
        sortField: String = "dateOfCreation",
        sortDirection: Query.Direction = Query.Direction.DESCENDING
    ): Flow<PagingData<UserMapMarker>>
}
