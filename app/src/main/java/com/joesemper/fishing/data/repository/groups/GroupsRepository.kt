package com.joesemper.fishing.data.repository.groups

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.common.User

interface GroupsRepository {
    suspend fun getUserData(): User?
    suspend fun logoutCurrentUser(): LiveData<Boolean>
}