package com.mobileprism.fishing.model.api

import android.os.Parcelable
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.model.entity.user.UserResponse
import kotlinx.parcelize.Parcelize
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register")
    suspend fun registerNewUser(
        @Body body: EmailPassword
    ): UserResponse

    @POST("login/email")
    suspend fun loginWithEmail(
        @Body body: EmailPassword
    ): UserResponse

    @POST("login/username")
    suspend fun loginWithUsername(
        @Body body: UsernamePassword
    ): UserResponse

    @POST("login/google")
    suspend fun loginUserWithGoogle(
        @Body body: GoogleAuthRequest
    ): UserResponse
}

@Parcelize
data class GoogleAuthRequest(
    val email: String,
    val googleAuthId: String
) : Parcelable
