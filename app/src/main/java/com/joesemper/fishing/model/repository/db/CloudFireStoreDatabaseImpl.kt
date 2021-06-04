package com.joesemper.fishing.model.repository.db

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joesemper.fishing.model.entity.user.User

class CloudFireStoreDatabaseImpl(private val context: Context) : DatabaseProvider {

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    override suspend fun getCurrentUser(): User? {
        return currentUser?.run { User(uid, displayName, isAnonymous, photoUrl.toString()) }
    }

    override suspend fun logoutCurrentUser() {
        AuthUI.getInstance().signOut(context)
    }


}