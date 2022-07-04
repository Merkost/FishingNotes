package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val currentUser: Flow<User?>
    suspend fun registerNewUser(loginPassword: LoginPassword): Result<Unit>
    suspend fun loginUser(loginPassword: LoginPassword): Result<Unit>
    suspend fun skipAuthorization(): Result<Unit>
    suspend fun authWithGoogle(): Result<Unit>
    suspend fun logoutCurrentUser(): Result<Unit>
    suspend fun updateUserProfileData(user: User): Result<Unit>
}