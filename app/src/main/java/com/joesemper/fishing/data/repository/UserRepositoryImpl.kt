package com.joesemper.fishing.data.repository

import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.entity.common.User
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(private val authManager: AuthManager): UserRepository {

    override val currentUser: Flow<User?>
        get() = authManager.currentUser

    override suspend fun logoutCurrentUser() {
        authManager.logoutCurrentUser()
    }
}