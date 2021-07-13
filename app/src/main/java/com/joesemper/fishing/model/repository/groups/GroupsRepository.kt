package com.joesemper.fishing.model.repository.groups

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.entity.common.User

interface GroupsRepository {
    suspend fun getUserData(): User?
    suspend fun logoutCurrentUser(): LiveData<Boolean>
}