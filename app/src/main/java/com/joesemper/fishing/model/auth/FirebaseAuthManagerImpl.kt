package com.joesemper.fishing.model.auth

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joesemper.fishing.model.entity.common.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class FirebaseAuthManagerImpl(private val context: Context) : AuthManager {

    private val fireBaseAuth = FirebaseAuth.getInstance()

    @ExperimentalCoroutinesApi
    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val authListener = FirebaseAuth.AuthStateListener {
                runBlocking {
                    send(it.currentUser?.run { mapFirebaseUserToUser(this) })
                }
            }

            fireBaseAuth.addAuthStateListener(authListener)
            awaitClose { fireBaseAuth.removeAuthStateListener(authListener) }
        }

    @ExperimentalCoroutinesApi
    override suspend fun logoutCurrentUser() = callbackFlow {
        AuthUI.getInstance().signOut(context).addOnSuccessListener {
            trySend(true)
        }
        awaitClose{}
    }

    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        return with(firebaseUser) {
            User(
                uid,
                displayName ?: "Anonymous",
                isAnonymous,
                photoUrl.toString()
            )
        }
        TODO("change name")
    }
}