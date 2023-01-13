package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FirebaseUserRepository {
    val currentUser: Flow<FishingFirebaseUser?>
    val datastoreUser: Flow<FishingFirebaseUser>
    val datastoreNullableUser: Flow<FishingFirebaseUser?>

    //val datastoreAppType: Flow<AppType>

    suspend fun logoutCurrentUser(): Flow<Boolean>
    suspend fun addNewUser(user: FishingFirebaseUser): StateFlow<Progress>
    suspend fun addOfflineUser()
    suspend fun setUserListener(user: FishingFirebaseUser)
    suspend fun setNewProfileData(user: FishingFirebaseUser): Result<Unit>

}
