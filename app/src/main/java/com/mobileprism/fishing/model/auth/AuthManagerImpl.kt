package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.GoogleAuthRequest
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.FishingCodes
import com.mobileprism.fishing.model.entity.FishingResponse
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
import com.mobileprism.fishing.model.utils.fishingSafeApiCall
import com.mobileprism.fishing.model.utils.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class AuthManagerImpl(
    private val userDatastore: UserDatastore,
    private val authRepository: AuthRepository,
//    private val firebaseUserRepository: FirebaseUserRepository,
) : AuthManager {

    override val loginState: MutableStateFlow<LoginState> =
        MutableStateFlow<LoginState>(LoginState.NotLoggedIn)

    override val authState: Flow<AuthState> = userDatastore.getAuthState
    override val currentUser: Flow<UserData> = userDatastore.getUser

/*    override val currentFirebaseUser: Flow<User?>
        get() = firebaseUserRepository.currentUser*/


    override suspend fun registerNewUser(emailPassword: EmailPassword) = flow {
        val result = authRepository.registerNewUser(emailPassword).single()
        if (result is ResultWrapper.Success) {
            onLoginSuccess(result.data)
        }
        emit(result)
    }

    override suspend fun loginUser(emailPassword: EmailPassword) = flow {
        val result = authRepository.loginUser(emailPassword).single()
        if (result is ResultWrapper.Success) {
            onLoginSuccess(result.data)
        }
        emit(result)
    }

    override suspend fun loginUser(usernamePassword: UsernamePassword) = flow {
        val result = authRepository.loginUser(usernamePassword).single()
        if (result is ResultWrapper.Success) {
            onLoginSuccess(result.data)
        }
        emit(result)
    }

    override suspend fun skipAuthorization() {
        createOfflineUser()
        loginState.update { LoginState.LoggedIn }
    }

    override suspend fun logoutCurrentUser() {
        userDatastore.logout()
    }

    override suspend fun googleLogin(email: String, googleAuthId: String, firebaseAuthId: String?) = flow {
        val result = authRepository.loginUserWithGoogle(email, googleAuthId, firebaseAuthId).single()
        if (result is ResultWrapper.Success) {
            onLoginSuccess(result.data)
        }
        emit(result)

        /*firebaseUserRepository.currentUser
            .catch { error ->
                onLoginFailure(
                    FishingResponse(
                        fishingCode = FishingCodes.UNKNOWN_ERROR,
                        description = error.message ?: ""
                    )
                )
            }
            .collectLatest { user ->
                if (user != null) {
                    authRepository.loginUserWithGoogle(
                        email = user.email,
                        userId = user.uid
                    ).single().fold(
                        onSuccess = {
                            onLoginSuccess(it)
                        },
                        onError = {
                            onLoginFailure(it)
                        }
                    )
                } else {
                    loginState.update { LoginState.NotLoggedIn }
                }
            }*/
    }

    /*override suspend fun logoutCurrentUser() {
        firebaseUserRepository.logoutCurrentUser().collectLatest { isLoggedOut ->
            userDatastore.logout()
        }
    }*/

    override suspend fun updateUserProfileData(user: User) {
        TODO("Not yet implemented")
    }

    private suspend fun onLoginSuccess(data: UserResponse) {
        userDatastore.saveUser(data.user)
        userDatastore.setToken(data.token)
    }

    private suspend fun createOfflineUser() {
        userDatastore.saveUser(
            user = UserData(
                login = "Anonymous",
            )
        )
    }

}