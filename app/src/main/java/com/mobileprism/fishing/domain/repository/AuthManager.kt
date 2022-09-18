package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.common.UsernamePassword
import com.mobileprism.fishing.model.auth.LoginState
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val loginState: Flow<LoginState>
    val currentUser: Flow<User?>
    val currentFirebaseUser: Flow<User?>

    @Deprecated("Replaced", replaceWith = ReplaceWith("loginState"))
    suspend fun subscribeOnLoginState(): Flow<LoginState>
    suspend fun registerNewUserWithEmail(emailPassword: EmailPassword)
    suspend fun registerNewUserWithUserName(userNamePassword: UsernamePassword)
    suspend fun loginUser(emailPassword: EmailPassword)
    suspend fun loginUser(usernamePassword: UsernamePassword)
    suspend fun googleLogin()
    suspend fun skipAuthorization()
    suspend fun logoutCurrentUser()
    suspend fun updateUserProfileData(user: User)
}