package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.entity.common.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser(): Flow<Boolean>
}