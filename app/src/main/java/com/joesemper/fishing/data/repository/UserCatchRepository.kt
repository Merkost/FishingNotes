package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.content.UserMapMarker
import kotlinx.coroutines.flow.Flow

interface UserCatchRepository {
    fun getMapMarker(markerId: String): Flow<UserMapMarker?>
}