package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datastore.TokenStore
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserApiResponse
import com.mobileprism.fishing.model.entity.user.UserData
import kotlinx.coroutines.flow.*

class AuthManagerImpl(
    private val userDatastore: UserDatastore,
    private val userRepository: UserRepository,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val tokenStore: TokenStore,
) : AuthManager {

    private val loginEvent = MutableStateFlow<LoginState>(LoginState.NotLoggedIn)

    override val currentUser: Flow<User?>
        get() = userDatastore.getNullableUser

    override val currentFirebaseUser: Flow<User?>
        get() = firebaseUserRepository.currentUser

    override suspend fun subscribeOnLoginState(): Flow<LoginState> {
        checkCurrentUser()
        return loginEvent
    }

    override suspend fun registerNewUser(emailPassword: EmailPassword) {
        userRepository.registerNewUser(emailPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
            },
            onFailure = {
                onLoginFailure(it)
            }
        )
    }

    override suspend fun loginUser(emailPassword: EmailPassword) {
        userRepository.loginUser(emailPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
            },
            onFailure = {
                onLoginFailure(it)
            }
        )
    }

    override suspend fun skipAuthorization() {
        createOfflineUser()
        loginEvent.emit(LoginState.LoggedIn)
    }

    override suspend fun authWithGoogle() {
        loginEvent.emit(LoginState.GoogleAuthRequest)

        firebaseUserRepository.currentUser
            .catch { error ->
                onLoginFailure(error)
            }
            .collectLatest { it ->
                if (it != null) {
                    userRepository.loginUserWithGoogle(
                        email = it.email,
                        userId = it.uid
                    ).fold(
                        onSuccess = {
                            onLoginSuccess(it)
                        },
                        onFailure = {
                            onLoginFailure(it)
                        }
                    )
                } else {
                    onLoginFailure(Throwable())
                }
            }
    }

    override suspend fun logoutCurrentUser() {
        firebaseUserRepository.logoutCurrentUser().collectLatest { isLoggedOut ->
            if (isLoggedOut) {
                userDatastore.clearUser()
                loginEvent.emit(LoginState.NotLoggedIn)
            }
        }
    }

    override suspend fun updateUserProfileData(user: User) {
        TODO("Not yet implemented")
    }

    private suspend fun checkCurrentUser() {
        if (currentUser.first() != null) {
            loginEvent.emit(LoginState.LoggedIn)
        }
    }

    private suspend fun onLoginSuccess(data: UserApiResponse) {
        saveUser(data.user)
        saveToken(data.token)
        loginEvent.emit(LoginState.LoggedIn)
    }

    private suspend fun onLoginFailure(throwable: Throwable) {
        loginEvent.emit(LoginState.LoginFailure(throwable))
    }

    private suspend fun createOfflineUser() {
        userDatastore.clearUser()
        userDatastore.saveUser(
            user = User(
                login = "Anonymous",
                loginType = LoginType.LOCAL
            )
        )
    }

    private suspend fun saveUser(user: UserData) {
        userDatastore.clearUser()
        userDatastore.saveUser(
            user = User(
                email = user.email,
                login = user.login
            )
        )
    }

    private suspend fun saveToken(token: String) {
        tokenStore.setToken(newToken = token)
    }

}