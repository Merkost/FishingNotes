package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.auth.LoginState
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val loginState: Flow<LoginState>
    val currentUser: Flow<User?>
    val currentFirebaseUser: Flow<User?>

    suspend fun registerNewUser(emailPassword: EmailPassword)
    suspend fun loginUser(emailPassword: EmailPassword)
    suspend fun loginUser(usernamePassword: UsernamePassword)
    suspend fun googleLogin()
    suspend fun skipAuthorization()
    suspend fun logoutCurrentUser()
    suspend fun updateUserProfileData(user: User)
}