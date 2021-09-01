package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.entity.common.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser(): Flow<Boolean>
}