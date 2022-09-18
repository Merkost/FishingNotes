package com.mobileprism.fishing.model.datasource.fake

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import com.mobileprism.fishing.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class FakeAuthRepositoryRetrofitImpl(private val dispatcher: CoroutineDispatcher = Dispatchers.IO):
    AuthRepository {

    override suspend fun registerNewUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            UserResponse(
                token = "123",
                UserData(email = emailPassword.email, login = "Anonymous")
            )
        }

    override suspend fun loginUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            UserResponse(
                token = "123",
                UserData(email = emailPassword.email, login = "Anonymous")
            )
        }

    override suspend fun loginUser(usernamePassword: UsernamePassword): Result<UserResponse> =
        safeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)

            UserResponse(
                token = "123",
                UserData(login = usernamePassword.username, email = "fromServer@email.ru")
            )
        }

    override suspend fun loginUserWithGoogle(
        email: String,
        userId: String
    ): Result<UserResponse> = safeApiCall(dispatcher) {
        delay(Constants.DEFAULT_DELAY)
        UserResponse(
            token = "123",
            UserData(email = email, login = "Anonymous")
        )
    }
}