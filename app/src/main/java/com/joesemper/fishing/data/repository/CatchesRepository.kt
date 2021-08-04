package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.content.UserCatch
import kotlinx.coroutines.flow.Flow

interface CatchesRepository {
    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>>
}