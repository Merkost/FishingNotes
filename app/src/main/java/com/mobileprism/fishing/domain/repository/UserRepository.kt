package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.model.entity.user.UserApiResponse

interface UserRepository {
    suspend fun registerNewUser(emailPassword: EmailPassword): Result<UserApiResponse>
    suspend fun loginUser(emailPassword: EmailPassword): Result<UserApiResponse>
    suspend fun loginUserWithGoogle(email: String, userId: String): Result<UserApiResponse>
}