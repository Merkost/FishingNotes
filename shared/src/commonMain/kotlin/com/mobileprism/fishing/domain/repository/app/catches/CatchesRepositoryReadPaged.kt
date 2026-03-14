package com.mobileprism.fishing.domain.repository.app.catches

import androidx.paging.PagingData
import com.mobileprism.fishing.domain.entity.common.SortDirection
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface CatchesRepositoryReadPaged : CatchesRepositoryRead {
    fun getAllUserCatchesPaged(
        sortField: String = "date",
        sortDirection: SortDirection = SortDirection.DESCENDING
    ): Flow<PagingData<UserCatch>>
}
