package com.joesemper.fishing.data.repository.map

import android.util.Log
import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.model.common.content.Content
import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

class MapRepositoryImpl(private val provider: DatabaseProvider) : MapRepository {

    private val markers = mutableSetOf<String>()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun getAllUserContent() = provider.getAllUserCatches().flatMapMerge { userCatch ->

        flow {
            emit(userCatch as Content)
            val markerId = userCatch.userMarkerId
            if (!markers.contains(markerId) and markerId.isNotBlank()) {
                try {
                    markers.add(markerId)
                    provider.getMarker(markerId).collect { marker ->
                        emit(marker as Content)
                    }
                } catch (e: Throwable) {
                    Log.d("Fishing", e.message, e)
                }
            }
        }
    }


    override suspend fun addNewCatch(newCatch: RawUserCatch) = provider.addNewCatch(newCatch)
    override suspend fun deleteMarker(userCatch: UserCatch) = provider.deleteMarker(userCatch)
}