package com.joesemper.fishing.model.repository

import com.joesemper.fishing.domain.viewstates.ContentState
import com.joesemper.fishing.model.datasource.DatabaseProvider
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.MapMarker
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class UserContentRepositoryImpl(private val dataProvider: DatabaseProvider) :
    UserContentRepository {

    override fun getMapMarker(markerId: String): Flow<UserMapMarker?> =
        dataProvider.getMapMarker(markerId)

    override fun getAllUserMarkers(): Flow<MapMarker> = dataProvider.getAllMarkers()

    override fun getAllUserMarkersList(): Flow<List<MapMarker>> =
        dataProvider.getAllUserMarkersList()

    override fun getAllUserCatchesList(): Flow<List<UserCatch>> =
        dataProvider.getAllUserCatchesList()

    override fun getAllUserCatchesState(): Flow<ContentState> =
        dataProvider.getAllUserCatchesState()

    override fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> =
        dataProvider.getCatchesByMarkerId(markerId)

    override suspend fun addNewCatch(
        markerId: String,
        newCatch: RawUserCatch
    ): StateFlow<Progress> =
        dataProvider.addNewCatch(markerId, newCatch)

    override suspend fun deleteCatch(userCatch: UserCatch) = dataProvider.deleteCatch(userCatch)

    override suspend fun deleteMarker(userMapMarker: UserMapMarker) =
        dataProvider.deleteMarker(userMapMarker)

    override suspend fun addNewMarker(newMarker: RawMapMarker): StateFlow<Progress> =
        dataProvider.addNewMarker(newMarker)

}