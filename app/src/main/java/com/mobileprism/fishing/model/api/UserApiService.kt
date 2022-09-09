package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.model.entity.user.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {
    @POST("register")
    suspend fun registerNewUser(
        @Body body: EmailPassword
    ): UserResponse

    @POST("login")
    suspend fun loginUser(
        @Body body: EmailPassword
    ): UserResponse

    @POST("login/google")
    suspend fun loginUserWithGoogle(
        @Body body: GoogleAuthRequest
    ): UserResponse
}

data class GoogleAuthRequest(
    val email: String,
    val googleAuthId: String
)
