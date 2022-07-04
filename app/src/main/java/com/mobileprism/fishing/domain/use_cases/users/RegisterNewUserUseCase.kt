package com.mobileprism.fishing.domain.use_cases.users

import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.repository.AuthManager

class RegisterNewUserUseCase(
    private val authManager: AuthManager
) {
    suspend operator fun invoke(loginPassword: LoginPassword) =
        authManager.registerNewUser(loginPassword)

}