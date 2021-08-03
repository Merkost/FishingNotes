package com.joesemper.fishing.data.repository.map

import com.joesemper.fishing.data.entity.RawMapMarker
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.model.common.content.Content
import com.joesemper.fishing.model.common.content.MapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
//    fun getAllUserContent(): Flow<Content>
    fun getAllUserMarkers(): Flow<MapMarker>
}