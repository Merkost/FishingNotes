package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {

    override fun getAllUserMarkers(): Flow<MapMarker> = provider.getAllMarkers()
    override suspend fun addNewMarker(newMarker: RawMapMarker) = provider.addNewMarker(newMarker)
}