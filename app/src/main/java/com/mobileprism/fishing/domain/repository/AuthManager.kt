package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.auth.LoginState
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val currentUser: Flow<User?>
    val currentFirebaseUser: Flow<User?>
    suspend fun subscribeOnLoginState(): Flow<LoginState>
    suspend fun registerNewUser(emailPassword: EmailPassword)
    suspend fun loginUser(emailPassword: EmailPassword)
    suspend fun skipAuthorization()
    suspend fun authWithGoogle()
    suspend fun logoutCurrentUser()
    suspend fun updateUserProfileData(user: User)
}