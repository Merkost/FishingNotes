package com.mobileprism.fishing.model.api

import android.os.Parcelable
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.model.entity.user.UserResponse
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register")
    suspend fun registerNewUser(
        @Body body: EmailPassword
    ): Response<UserResponse>

    @POST("login/email")
    suspend fun loginWithEmail(
        @Body body: EmailPassword
    ): Response<UserResponse>

    @POST("login/username")
    suspend fun loginWithUsername(
        @Body body: UsernamePassword
    ): Response<UserResponse>

    @POST("login/google")
    suspend fun loginUserWithGoogle(
        @Body body: GoogleAuthRequest
    ): Response<UserResponse>

}

@Parcelize
data class GoogleAuthRequest(
    val email: String,
    val googleAuthId: String,
    val firebaseAuthId: String?,
) : Parcelable
