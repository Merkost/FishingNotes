package com.joesemper.fishing.model.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.datasource.DatabaseProvider
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.MapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class UserRepositoryImpl(private val authManager: AuthManager,
                         private val databaseProvider: DatabaseProvider): UserRepository {

    override val currentUser: Flow<User?>
        get() = authManager.currentUser

    override suspend fun logoutCurrentUser() = authManager.logoutCurrentUser()
    override suspend fun addNewUser(user: User): StateFlow<Progress> = databaseProvider.addNewUser(user)
}