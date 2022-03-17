package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.model.entity.common.ContentStateOld
import com.mobileprism.fishing.model.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface CatchesRepository {

    // TODO: Divide on two repos: 1st just reading, 2nd updating/adding

    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
    fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch>

    suspend fun updateUserCatch(markerId: String, catchId: String, data: Map<String, Any>)
    suspend fun deleteCatch(userCatch: UserCatch)
    fun addNewCatch(markerId: String, newCatch: UserCatch): Flow<Result<Nothing?>>
}
