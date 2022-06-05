package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FirebaseUserRepository {
    val currentUser: Flow<User?>
    val datastoreUser: Flow<User>
    val datastoreNullableUser: Flow<User?>

    //val datastoreAppType: Flow<AppType>

    suspend fun logoutCurrentUser(): Flow<Boolean>
    suspend fun addNewUser(user: User): StateFlow<Progress>
    suspend fun setUserListener(user: User)
    suspend fun setNewProfileData(user: User): Result<Unit>

}
