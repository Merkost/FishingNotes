package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUser: Flow<User?>
    val datastoreUser: Flow<User>
    val isLoggedIn: Boolean
    val cachedUser: User?

    suspend fun logoutCurrentUser()
    suspend fun deleteAccount(): Result<Unit>
    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit>
    suspend fun addNewUser(user: User): Result<Unit>
    suspend fun setUserListener(user: User)
    suspend fun setNewProfileData(user: User): Result<Unit>

}

class ReauthRequiredException : Exception("Recent login required")

class NoConnectionException : Exception("No network connection")
