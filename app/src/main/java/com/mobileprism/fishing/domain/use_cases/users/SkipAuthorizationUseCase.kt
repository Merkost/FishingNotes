package com.mobileprism.fishing.domain.use_cases.users

import com.mobileprism.fishing.domain.repository.AuthManager

class SkipAuthorizationUseCase(
    private val authManager: AuthManager
) {
    suspend operator fun invoke() = authManager.skipAuthorization()
}