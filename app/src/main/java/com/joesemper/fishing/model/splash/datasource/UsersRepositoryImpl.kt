package com.joesemper.fishing.model.splash.datasource

import com.joesemper.fishing.model.db.datasource.DatabaseProvider
import com.joesemper.fishing.model.db.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(private val provider: DatabaseProvider): UsersRepository {

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
       provider.getCurrentUser()
    }




}