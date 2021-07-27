package com.joesemper.fishing.data.repository.map.catches

import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.model.common.Progress
import kotlinx.coroutines.flow.StateFlow

interface CatchesRepository {
    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>

}