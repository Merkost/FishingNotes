package com.mobileprism.fishing.model.api

import com.mobileprism.fishing.model.entity.user.UserApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApiService {
    @GET("register")
    suspend fun registerNewUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserApiResponse

    @GET("login")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserApiResponse

    @GET("login/google")
    suspend fun loginUserWithGoogle(
        @Query("email") email: String,
        @Query("googleAuthId") googleAuthId: String
    ): UserApiResponse
}