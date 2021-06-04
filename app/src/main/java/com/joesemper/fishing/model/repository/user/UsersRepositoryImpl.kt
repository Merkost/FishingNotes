package com.joesemper.fishing.model.repository.user

import com.joesemper.fishing.model.repository.db.DatabaseProvider
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(private val provider: DatabaseProvider): UsersRepository {

    override suspend fun getCurrentUser(): User?  {
       return provider.getCurrentUser()
    }

    override suspend fun logoutCurrentUser()  {
        provider.logoutCurrentUser()
    }




}