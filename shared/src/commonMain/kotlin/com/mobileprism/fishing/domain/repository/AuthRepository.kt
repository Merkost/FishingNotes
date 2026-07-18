package com.mobileprism.fishing.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserId(): String
    fun getCurrentUserIdOrNull(): String?
    val currentUserIdFlow: Flow<String?>
}
