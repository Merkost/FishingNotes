package com.mobileprism.fishing.model.repository.app

import android.net.Uri
import com.mobileprism.fishing.model.entity.common.CatchesContentState
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CatchesRepository {
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<CatchesContentState>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>

    fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch>
    suspend fun updateUserCatch(markerId: String, catchId: String, data: Map<String, Any>)
    suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress>

    suspend fun deleteCatch(userCatch: UserCatch)
    fun addNewCatch(markerId: String, newCatch: UserCatch): Flow<Result<Nothing?>>
}
