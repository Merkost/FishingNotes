package com.joesemper.fishing.model.auth

import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthManager {
    val currentUser: Flow<User?>
    suspend fun logoutCurrentUser()
}