package com.mobileprism.fishing.domain.repository.app.catches


import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow


interface CatchesRepositoryRead {
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
    fun getAllUserCatchesPaged(
        sortField: String = "date",
        sortDirection: Query.Direction = Query.Direction.DESCENDING
    ): Flow<PagingData<UserCatch>>
}
