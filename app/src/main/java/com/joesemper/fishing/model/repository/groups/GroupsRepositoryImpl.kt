package com.joesemper.fishing.model.repository.groups

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.db.DatabaseProvider
import com.joesemper.fishing.model.entity.common.User

class GroupsRepositoryImpl(private val provider: DatabaseProvider): GroupsRepository {
    override suspend fun getUserData(): User? {
        TODO("Not yet implemented")
    }

    override suspend fun logoutCurrentUser(): LiveData<Boolean> {
        TODO("Not yet implemented")
    }


}