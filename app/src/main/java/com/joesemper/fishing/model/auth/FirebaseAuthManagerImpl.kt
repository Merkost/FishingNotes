package com.joesemper.fishing.model.auth

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.suspendCoroutine

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

    override suspend fun logoutCurrentUser() {
        AuthUI.getInstance().signOut(context)
    }

    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        return with(firebaseUser) {
            User(
                uid,
                displayName,
                isAnonymous,
                photoUrl.toString()
            )
        }
    }
}