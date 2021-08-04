package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.raw.RawUserCatch
import com.joesemper.fishing.data.entity.common.Progress
import kotlinx.coroutines.flow.StateFlow

interface NewCatchRepository {
    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>

}