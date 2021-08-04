package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.data.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress>
//    fun getAllUserContent(): Flow<Content>
    fun getAllUserMarkers(): Flow<MapMarker>
}