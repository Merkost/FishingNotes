package com.joesemper.fishing.model.splash.datasource

import com.joesemper.fishing.model.db.entity.User

interface UsersRepository {
   suspend fun getCurrentUser(): User?
}
