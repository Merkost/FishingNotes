package com.mobileprism.fishing.domain.repository.app.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface CatchesRepositoryUpdate {

    fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch>
    suspend fun updateUserCatch(markerId: String, catchId: String, data: Map<String, Any>): Result<Unit>
    suspend fun updateUserCatchPhotos(markerId: String, catchId: String, newPhotos: List<String>): Result<Unit>

    suspend fun deleteCatch(userCatch: UserCatch): Result<Unit>
    suspend fun addNewCatch(markerId: String, newCatch: UserCatch): Result<Unit>
}
