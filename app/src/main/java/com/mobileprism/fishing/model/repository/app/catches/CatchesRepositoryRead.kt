package com.mobileprism.fishing.model.repository.app.catches

import android.net.Uri
import com.mobileprism.fishing.model.entity.common.ContentStateOld
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CatchesRepositoryRead {
    fun getAllUserCatchesList(): Flow<List<UserCatch>>
    fun getAllUserCatchesState(): Flow<ContentStateOld<UserCatch>>
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}
