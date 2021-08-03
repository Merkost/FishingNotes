package com.joesemper.fishing.data.repository.map.catches

import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface CatchesRepository {
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}