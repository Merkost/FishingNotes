package com.joesemper.fishing.model.db.datasource

import com.joesemper.fishing.model.db.entity.User

interface DatabaseProvider {
    fun getCurrentUser(): User?

}