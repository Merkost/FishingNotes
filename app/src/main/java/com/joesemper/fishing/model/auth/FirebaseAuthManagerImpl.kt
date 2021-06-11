package com.joesemper.fishing.model.auth

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseAuthManagerImpl(private val context: Context) : AuthManager {

    private val fireBaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = fireBaseAuth.currentUser

    override val user: Flow<User?>
        get() = flow {
            emit(currentUser?.run { User(uid, displayName, isAnonymous, photoUrl.toString()) })
        }

    override suspend fun getCurrentUser() = user

    override suspend fun logoutCurrentUser() {
        AuthUI.getInstance().signOut(context)
    }
}