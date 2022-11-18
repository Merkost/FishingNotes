package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.GoogleAuthRequest
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.auth.AuthState
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val loginState: Flow<LoginState>
    val authState: Flow<AuthState>
    val currentUser: Flow<UserData>
    //val currentFirebaseUser: Flow<User?>

    suspend fun registerNewUser(emailPassword: EmailPassword): Flow<ResultWrapper<UserResponse>>
    suspend fun loginUser(emailPassword: EmailPassword): Flow<ResultWrapper<UserResponse>>
    suspend fun loginUser(usernamePassword: UsernamePassword): Flow<ResultWrapper<UserResponse>>

    suspend fun googleLogin(
        email: String,
        googleAuthId: String,
        firebaseAuthId: String?
    ): Flow<ResultWrapper<UserResponse>>

    suspend fun skipAuthorization()
    suspend fun logoutCurrentUser()
    suspend fun updateUserProfileData(user: User)
}