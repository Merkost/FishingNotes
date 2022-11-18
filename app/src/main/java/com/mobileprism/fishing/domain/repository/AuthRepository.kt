package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun registerNewUser(emailPassword: EmailPassword): Flow<ResultWrapper<UserResponse>>
    suspend fun loginUser(emailPassword: EmailPassword): Flow<ResultWrapper<UserResponse>>
    suspend fun loginUser(usernamePassword: UsernamePassword): Flow<ResultWrapper<UserResponse>>
    suspend fun loginUserWithGoogle(email: String, userId: String, firebaseAuthId: String?): Flow<ResultWrapper<UserResponse>>
}