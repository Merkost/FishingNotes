package com.joesemper.fishing.model.repository.db

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow

interface DatabaseProvider {
    suspend fun getCurrentUser(): User?
    suspend fun logoutCurrentUser()
}