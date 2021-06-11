package com.joesemper.fishing.model.auth

import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val user: Flow<User?>
    suspend fun getCurrentUser(): Flow<User?>
    suspend fun logoutCurrentUser()
}