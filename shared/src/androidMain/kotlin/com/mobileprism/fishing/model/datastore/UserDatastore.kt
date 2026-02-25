package com.mobileprism.fishing.model.datastore

import com.mobileprism.fishing.domain.entity.common.User
import kotlinx.coroutines.flow.Flow

interface UserDatastore {
    val getUser: Flow<User>
    suspend fun saveUser(user: User)
}