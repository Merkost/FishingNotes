package com.joesemper.fishing.model.repository.app

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

    suspend fun deleteCatch(userCatch: UserCatch)
    suspend fun addNewCatch(markerId: String, newCatch: RawUserCatch): StateFlow<Progress>
}
