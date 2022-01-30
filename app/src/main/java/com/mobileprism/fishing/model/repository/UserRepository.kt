package com.mobileprism.fishing.model.repository

import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.common.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: Flow<User?>
    val datastoreUser: Flow<User?>

    suspend fun logoutCurrentUser(): Flow<Boolean>
    suspend fun addNewUser(user: User): StateFlow<Progress>
    suspend fun setUserListener(user: User)

}