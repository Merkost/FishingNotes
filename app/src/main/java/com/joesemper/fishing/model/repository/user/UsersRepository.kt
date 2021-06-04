package com.joesemper.fishing.model.repository.user

import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
   suspend fun getCurrentUser(): User?
   suspend fun logoutCurrentUser()
}
