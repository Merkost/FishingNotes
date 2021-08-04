package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.content.UserCatch

class MarkerRepositoryImpl(private val provider: DatabaseProvider): MarkerRepository {

    override suspend fun deleteMarker(userCatch: UserCatch) {}
}