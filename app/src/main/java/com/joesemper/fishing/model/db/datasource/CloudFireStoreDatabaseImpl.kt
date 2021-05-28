package com.joesemper.fishing.model.db.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joesemper.fishing.model.db.entity.User

class CloudFireStoreDatabaseImpl : DatabaseProvider {

    private val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    override fun getCurrentUser(): User? = currentUser?.run { User(uid, displayName) }
}