package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.api.AuthApiService
import com.mobileprism.fishing.model.api.GoogleAuthRequest
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AuthRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val authApiService: AuthApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AuthRepository {

    override suspend fun registerNewUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("register_new_user", null)

            authApiService.registerNewUser(
                body = emailPassword
            )

        }

    override suspend fun loginUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("login_user", null)

            authApiService.loginWithEmail(
                body = emailPassword
            )

        }

    override suspend fun loginUser(usernamePassword: UsernamePassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("login_user", null)

            authApiService.loginWithUsername(
                body = usernamePassword
            )

        }

    override suspend fun loginUserWithGoogle(
        email: String,
        userId: String
    ): Result<UserResponse> = safeApiCall(dispatcher) {

        firebaseAnalytics.logEvent("login_with_google", null)

        authApiService.loginUserWithGoogle(
            GoogleAuthRequest(
                email = email,
                googleAuthId = userId
            )
        )

    }
}