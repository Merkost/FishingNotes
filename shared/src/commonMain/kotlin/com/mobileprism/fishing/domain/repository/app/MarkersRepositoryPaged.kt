package com.mobileprism.fishing.domain.repository.app

import androidx.paging.PagingData
import com.mobileprism.fishing.domain.entity.common.SortDirection
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface MarkersRepositoryPaged : MarkersRepository {
    fun getAllUserMarkersListPaged(
        sortField: String = "dateOfCreation",
        sortDirection: SortDirection = SortDirection.DESCENDING
    ): Flow<PagingData<UserMapMarker>>
}
