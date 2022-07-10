package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.model.entity.user.UserApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @GET("register")
    suspend fun registerNewUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserApiResponse

    @POST("login")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserApiResponse

    @POST("login/google")
    suspend fun loginUserWithGoogle(
        @Body body: GoogleAuthRequest
    ): UserApiResponse
}

data class GoogleAuthRequest(
    val email: String,
    val googleAuthId: String
)
