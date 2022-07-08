package com.mobileprism.fishing.model.auth

import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datastore.TokenStore
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserApiResponse
import com.mobileprism.fishing.model.entity.user.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class AuthManagerImpl(
    private val userDatastore: UserDatastore,
    private val userRepository: UserRepository,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val tokenStore: TokenStore
) : AuthManager {

    override val currentUser: Flow<User?>
        get() = userDatastore.getNullableUser

    override val currentFirebaseUser: Flow<User?>
        get() = firebaseUserRepository.currentUser

    override suspend fun registerNewUser(loginPassword: LoginPassword): Result<Unit> {
        userRepository.registerNewUser(loginPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
                return Result.success(Unit)
            },
            onFailure = {
                return Result.failure(it)
            }
        )
    }

    override suspend fun loginUser(loginPassword: LoginPassword): Result<Unit> {
        userRepository.loginUser(loginPassword).fold(
            onSuccess = {
                onLoginSuccess(it)
                return Result.success(Unit)
            },
            onFailure = {
                return Result.failure(it)
            }
        )
    }

    override suspend fun skipAuthorization(): Result<Unit> {
        createOfflineUser()
        return Result.success(Unit)
    }

    override suspend fun authWithGoogle(): Result<Unit> = callbackFlow<Result<Unit>> {
        firebaseUserRepository.currentUser
            .catch { error ->
                trySend(Result.failure(error))
            }
            .collectLatest {
                if (it != null) {
                    userRepository.loginUserWithGoogle(
                        email = it.email,
                        userId = it.uid
                    ).fold(
                        onSuccess = {
                            onLoginSuccess(it)
                            trySend(Result.success(Unit))
                        },
                        onFailure = {
                            trySend(Result.failure(it))
                        }
                    )
                } else {
                    trySend(Result.failure(Throwable()))
                }
            }
        awaitClose() {}
    }.first()

    override suspend fun logoutCurrentUser(): Result<Unit> {
        firebaseUserRepository.logoutCurrentUser().collectLatest { isLoggedOut ->
            if (isLoggedOut) {
                userDatastore.clearUser()
            }
        }
        return Result.success(Unit)
    }

    override suspend fun updateUserProfileData(user: User): Result<Unit> {
        TODO("Not yet implemented")
    }

    private suspend fun onLoginSuccess(data: UserApiResponse) {
        saveUser(data.user)
        saveToken(data.token)
    }

    private suspend fun saveUser(user: UserData) {
        userDatastore.saveUser(
            user = User(
                email = user.email,
                login = user.login
            )
        )
    }

    private suspend fun createOfflineUser() {
        userDatastore.saveUser(
            user = User(
                login = "Anonymous",
                loginType = LoginType.LOCAL
            )
        )
    }

    private suspend fun saveToken(token: String) {
        tokenStore.setToken(newToken = token)
    }

}