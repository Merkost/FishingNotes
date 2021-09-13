package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser(): Flow<Boolean>
    suspend fun addNewUser(user: User): StateFlow<Progress>

}