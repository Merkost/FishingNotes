package com.joesemper.fishing.data.repository

import androidx.lifecycle.LiveData
import com.joesemper.fishing.data.entity.common.User

interface GroupsRepository {
    suspend fun getUserData(): User?
    suspend fun logoutCurrentUser(): LiveData<Boolean>
}