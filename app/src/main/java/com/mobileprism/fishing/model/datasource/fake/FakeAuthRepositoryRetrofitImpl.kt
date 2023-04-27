package com.mobileprism.fishing.model.datasource.fake

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.api.GoogleAuthRequest
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
import com.mobileprism.fishing.model.utils.fishingSafeApiCall
import com.mobileprism.fishing.model.utils.safeApiCall
import com.mobileprism.fishing.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class FakeAuthRepositoryRetrofitImpl(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    AuthRepository {

    override suspend fun registerNewUser(emailPassword: EmailPassword) = flow {
        emit(fishingSafeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            Response.success(
                UserResponse(
                    token = "123",
                    UserData(email = emailPassword.email, login = "Anonymous")
                )
            )
        })
    }

    override suspend fun loginUser(emailPassword: EmailPassword) = flow {
        emit(fishingSafeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            Response.success(
                UserResponse(
                    token = "123",
                    UserData(email = emailPassword.email, login = "Anonymous")
                )
            )
        })
    }

    override suspend fun loginUser(usernamePassword: UsernamePassword) = flow {
        emit(fishingSafeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)

            Response.success(
                UserResponse(
                    token = "123",
                    UserData(login = usernamePassword.username, email = "fromServer@email.ru")
                )
            )
        })
    }

    override suspend fun loginUserWithGoogle(
        loginAuthRequest: GoogleAuthRequest
    ) = flow {
        emit(fishingSafeApiCall(dispatcher) {
            delay(Constants.DEFAULT_DELAY)
            Response.success(
                UserResponse(
                    token = "123",
                    UserData(email = loginAuthRequest.email, login = "Anonymous")
                )
            )
        })
    }
}