package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.model.entity.user.UserApiResponse

interface UserRepository {
    suspend fun registerNewUser(loginPassword: LoginPassword): Result<UserApiResponse>
    suspend fun loginUser(loginPassword: LoginPassword): Result<UserApiResponse>
    suspend fun loginUserWithGoogle(email: String, userId: String): Result<UserApiResponse>
}