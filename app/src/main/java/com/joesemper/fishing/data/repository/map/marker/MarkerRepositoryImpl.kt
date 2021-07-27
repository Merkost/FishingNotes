package com.joesemper.fishing.data.repository.map.marker

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.model.common.content.UserCatch

class MarkerRepositoryImpl(private val provider: DatabaseProvider): MarkerRepository {

    override suspend fun deleteMarker(userCatch: UserCatch) {}
    override fun getCatchesByMarkerId(markerId: String) = provider.getCatchesByMarkerId(markerId)
}