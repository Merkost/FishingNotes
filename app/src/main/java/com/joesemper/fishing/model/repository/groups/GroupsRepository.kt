package com.joesemper.fishing.model.repository.groups

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.entity.user.User

interface GroupsRepository {
    suspend fun getUserData(): User?
    suspend fun logoutCurrentUser(): LiveData<Boolean>
}