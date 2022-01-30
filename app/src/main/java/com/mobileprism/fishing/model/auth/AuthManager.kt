package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.model.entity.common.User
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser(): Flow<Boolean>
}