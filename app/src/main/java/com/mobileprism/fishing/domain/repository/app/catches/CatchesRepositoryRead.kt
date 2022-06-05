package com.mobileprism.fishing.domain.repository.app.catches


import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow


interface CatchesRepositoryRead {
    fun getCatchById(markerId: String, catchId: String): Flow<UserCatch?>

    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}
