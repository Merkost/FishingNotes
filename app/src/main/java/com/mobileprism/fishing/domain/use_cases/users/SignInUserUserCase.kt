package com.mobileprism.fishing.domain.use_cases.users

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.domain.repository.AuthManager

class SignInUserUserCase(
    private val authManager: AuthManager
) {
    suspend operator fun invoke(emailPassword: EmailPassword) = authManager.loginUser(emailPassword)
}