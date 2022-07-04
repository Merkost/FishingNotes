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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthManagerImpl(
    private val userDatastore: UserDatastore,
    private val userRepository: UserRepository,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val tokenStore: TokenStore
) : AuthManager {

    override val currentUser: Flow<User?>
        get() = userDatastore.getNullableUser

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

    override suspend fun authWithGoogle(): Result<Unit> {

        val currentGoogleUser = firebaseUserRepository.currentUser.first()

        if (currentGoogleUser != null) {
            userRepository.loginUserWithGoogle(
                email = currentGoogleUser.email,
                userId = currentGoogleUser.uid
            ).fold(
                onSuccess = {
                    onLoginSuccess(it)
                    return Result.success(Unit)
                },
                onFailure = {
                    return Result.failure(it)
                }
            )
        } else {
            return Result.failure(Throwable())
        }
    }

    override suspend fun logoutCurrentUser(): Result<Unit> {
        userDatastore.clearUser()
        firebaseUserRepository.logoutCurrentUser()
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