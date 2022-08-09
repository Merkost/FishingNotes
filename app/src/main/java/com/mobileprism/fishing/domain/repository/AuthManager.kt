package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val currentUser: Flow<User?>
    val currentFirebaseUser: Flow<User?>
    suspend fun registerNewUser(emailPassword: EmailPassword): Result<Unit>
    suspend fun loginUser(emailPassword: EmailPassword): Result<Unit>
    suspend fun skipAuthorization(): Result<Unit>
    suspend fun authWithGoogle(): Result<Unit>
    suspend fun logoutCurrentUser(): Result<Unit>
    suspend fun updateUserProfileData(user: User): Result<Unit>
}