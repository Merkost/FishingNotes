package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

class UserCatchRepositoryImpl(private val provider: DatabaseProvider): UserCatchRepository {

    override fun getMapMarker(markerId: String): Flow<UserMapMarker?> = provider.getMapMarker(markerId)
}