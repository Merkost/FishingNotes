package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.common.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.TokenStore
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import kotlinx.coroutines.flow.*

class AuthManagerImpl(
    private val userDatastore: UserDatastore,
    private val authRepository: AuthRepository,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val tokenStore: TokenStore,
) : AuthManager {

    override val loginState: MutableStateFlow<LoginState> =
        MutableStateFlow<LoginState>(LoginState.NotLoggedIn)

    override val currentUser: Flow<User?>
        get() = userDatastore.getNullableUser

    override val currentFirebaseUser: Flow<User?>
        get() = firebaseUserRepository.currentUser

    override suspend fun subscribeOnLoginState(): Flow<LoginState> = loginState.asStateFlow()

    override suspend fun registerNewUserWithEmail(emailPassword: EmailPassword) {
        authRepository.registerNewUser(emailPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
            },
            onFailure = {
                onLoginFailure(it)
            }
        )
    }

    override suspend fun registerNewUserWithUserName(userNamePassword: UsernamePassword) {
        // TODO: Implement login with username
        onLoginFailure(throwable = Throwable("Not implemented"))
    }

    override suspend fun loginUser(emailPassword: EmailPassword) {
        authRepository.loginUser(emailPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
            },
            onFailure = {
                onLoginFailure(it)
            }
        )
    }

    override suspend fun loginUser(usernamePassword: UsernamePassword) {
        authRepository.loginUser(usernamePassword).fold(
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
        loginState.update { LoginState.LoggedIn }
    }

    override suspend fun googleLogin() {

        firebaseUserRepository.currentUser
            .catch { error -> onLoginFailure(error) }
            .collectLatest { user ->
                if (user != null) {
                    authRepository.loginUserWithGoogle(
                        email = user.email,
                        userId = user.uid
                    ).fold(
                        onSuccess = {
                            onLoginSuccess(it)
                        },
                        onFailure = {
                            onLoginFailure(it)
                        }
                    )
                } else {
                    loginState.update { LoginState.NotLoggedIn }
                }
            }
    }

    override suspend fun logoutCurrentUser() {
        firebaseUserRepository.logoutCurrentUser().collectLatest { isLoggedOut ->
            if (isLoggedOut) {
                userDatastore.clearUser()
                loginState.update { LoginState.NotLoggedIn }
            }
        }
    }

    override suspend fun updateUserProfileData(user: User) {
        TODO("Not yet implemented")
    }

    private suspend fun onLoginSuccess(data: UserResponse) {
        saveUser(data.user)
        saveToken(data.token)
        loginState.update { LoginState.LoggedIn }
    }

    private suspend fun onLoginFailure(throwable: Throwable) {
        loginState.update { LoginState.LoginFailure(throwable) }
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
                login = user.login,
                loginType = LoginType.SERVER,
            )
        )
    }

    private suspend fun saveToken(token: String) {
        tokenStore.setToken(newToken = token)
    }

}