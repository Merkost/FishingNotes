package com.mobileprism.fishing.domain.repository.app.catches


import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow


interface CatchesRepositoryRead {
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    suspend fun getCatchesByMarkerId(markerId: String): Result<List<UserCatch>>
}
