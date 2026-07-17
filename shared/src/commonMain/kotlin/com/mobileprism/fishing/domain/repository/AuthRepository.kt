package com.mobileprism.fishing.domain.repository

interface AuthRepository {
    fun getCurrentUserId(): String
    fun getCurrentUserIdOrNull(): String?
}
