package com.mobileprism.fishing.domain.repository.app.catches

import android.net.Uri
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CatchesRepository{

    fun getCatchById(markerId: String, catchId: String): Flow<UserCatch?>

    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>


    fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch>
    suspend fun updateUserCatch(userCatch: UserCatch)
    suspend fun updateUserCatchPhotos(markerId: String, catchId: String, newPhotos: List<Uri>): StateFlow<Progress>

    suspend fun deleteCatch(userCatch: UserCatch)
    fun addNewCatch(markerId: String, newCatch: UserCatch): Flow<Result<Unit>>
}
