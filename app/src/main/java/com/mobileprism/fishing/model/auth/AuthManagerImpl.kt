package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.api.fishing.GoogleAuthRequest
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
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

    override suspend fun googleLogin(
        email: String,
        googleAuthId: String,
        googleAuthIdToken: String,
        firebaseUser: FishingFirebaseUser?
    ) = flow {
        firebaseUser?.let { userDatastore.saveFirebaseUser(firebaseUser) }

        val googleAuthRequest = GoogleAuthRequest(
            email = email,
            googleAuthId = googleAuthId,
            googleAuthIdToken = googleAuthIdToken,
            firebaseAuthId = firebaseUser?.uid,
            googlePhotoUrl = firebaseUser?.photoUrl
        )

        val result = authRepository.loginUserWithGoogle(googleAuthRequest).single()
        if (result is ResultWrapper.Success) {
            onLoginSuccess(result.data)
        }
        emit(result)

    }

    /*override suspend fun logoutCurrentUser() {
        firebaseUserRepository.logoutCurrentUser().collectLatest { isLoggedOut ->
            userDatastore.logout()
        }
    }*/

    override suspend fun updateUserProfileData(user: FishingFirebaseUser) {
        TODO("Not yet implemented")
    }

    private suspend fun onLoginSuccess(data: UserResponse) {
        userDatastore.saveUser(data.user)
        userDatastore.setToken(data.token)
    }

    private suspend fun createOfflineUser() {
        /*userDatastore.saveUser(
            user = UserData(
                login = "Anonymous",
            )
        )*/
    }

}