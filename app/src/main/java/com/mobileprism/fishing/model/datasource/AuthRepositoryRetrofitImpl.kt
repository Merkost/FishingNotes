package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.api.AuthApiService
import com.mobileprism.fishing.model.api.GoogleAuthRequest
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.fishingSafeApiCall
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class AuthRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val authApiService: AuthApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AuthRepository {

    override suspend fun registerNewUser(emailPassword: EmailPassword) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("register_new_user", null)
            authApiService.registerNewUser(body = emailPassword)
        }
        emit(result)
    }

    override suspend fun loginUser(emailPassword: EmailPassword) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("login_user", null)
            authApiService.loginWithEmail(body = emailPassword)
        }
        emit(result)
    }

    override suspend fun loginUser(usernamePassword: UsernamePassword) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("login_user", null)
            authApiService.loginWithUsername(
                body = usernamePassword
            )
        }
        emit(result)
    }


    override suspend fun loginUserWithGoogle(
        loginAuthRequest: GoogleAuthRequest
    ) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("login_with_google", null)
            authApiService.loginUserWithGoogle(loginAuthRequest)
        }
        emit(result)
    }
}