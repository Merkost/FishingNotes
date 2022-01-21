package com.joesemper.fishing.model.repository.app

import android.net.Uri
import com.joesemper.fishing.model.entity.common.CatchesContentState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.raw.RawUserCatch
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
    suspend fun addNewCatch(markerId: String, newCatch: RawUserCatch): Flow<Progress>
}
