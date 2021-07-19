package com.joesemper.fishing.data.repository.map

import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.model.common.content.MapMarker
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.model.common.content.Content
import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addNewCatch(newCatch: RawUserCatch): StateFlow<Progress>
    suspend fun deleteMarker(userCatch: UserCatch)
    fun getAllUserContent(): Flow<Content>
}