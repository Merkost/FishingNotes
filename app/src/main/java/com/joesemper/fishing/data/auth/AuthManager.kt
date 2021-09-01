package com.joesemper.fishing.data.auth

import com.joesemper.fishing.data.entity.common.User
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser(): Flow<Boolean>
}