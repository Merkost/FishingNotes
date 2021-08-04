package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {

    private val markers = mutableSetOf<String>()

//    @FlowPreview
//    @ExperimentalCoroutinesApi
//    override fun getAllUserContent() = provider.getAllUserCatches()
////        .flatMapMerge { userCatch ->
////
////        flow {
////            emit(userCatch as Content)
////            val markerId = userCatch.userMarkerId
////            if (!markers.contains(markerId) and markerId.isNotBlank()) {
////                try {
////                    markers.add(markerId)
////                    provider.getMarker(markerId).collect { marker ->
////                        emit(marker as Content)
////                    }
////                } catch (e: Throwable) {
////                    Log.d("Fishing", e.message, e)
////                }
////            }
////        }
////    }

    override fun getAllUserMarkers(): Flow<MapMarker> = provider.getAllMarkers()
    override suspend fun addNewMarker(newMarker: RawMapMarker) = provider.addNewMarker(newMarker)
}