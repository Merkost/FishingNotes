package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: Flow<User?>
    val datastoreUser: Flow<User>

    suspend fun logoutCurrentUser(): Flow<Boolean>
    suspend fun addNewUser(user: User): StateFlow<Progress>
    suspend fun addOfflineUser()
    suspend fun setUserListener(user: User)
    suspend fun setNewProfileData(user: User): Result<Unit>

}