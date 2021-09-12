package com.joesemper.fishing.model.repository

import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(private val authManager: AuthManager): UserRepository {

    override val currentUser: Flow<User?>
        get() = authManager.currentUser

    override suspend fun logoutCurrentUser() = authManager.logoutCurrentUser()
}