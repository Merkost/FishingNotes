package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.model.auth.AuthState
import com.mobileprism.fishing.model.entity.user.Token
import com.mobileprism.fishing.model.entity.user.UserData
import kotlinx.coroutines.flow.Flow

interface UserDatastore {
    val getAuthState: Flow<AuthState>

    suspend fun setToken(newToken: String)
    val currentToken: Flow<Token>

    val getUser: Flow<UserData>
    suspend fun saveUser(user: UserData)
    val getFirebaseUser: Flow<FishingFirebaseUser?>
    suspend fun saveFirebaseUser(firebaseUser: FishingFirebaseUser)

    suspend fun logout()

}