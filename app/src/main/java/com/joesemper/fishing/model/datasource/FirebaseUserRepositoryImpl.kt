package com.joesemper.fishing.model.datasource

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joesemper.fishing.compose.datastore.AppPreferences
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseUserRepositoryImpl(private val appPreferences: AppPreferences,
                                 private val context: Context) : UserRepository {

    private val fireBaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

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

    override val datastoreUser: Flow<User?>
        get() = callbackFlow {
            appPreferences.userValue.collectLatest {
                send(it)
            }
            awaitClose {  }
        }

    override suspend fun logoutCurrentUser() = callbackFlow {
        AuthUI.getInstance().signOut(context).addOnSuccessListener {
            trySend(true)
        }
        awaitClose {}
    }

    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        return with(firebaseUser) {
            User(
                uid,
                email = firebaseUser.email ?: "",
                displayName ?: "Anonymous",
                isAnonymous,
                photoUrl.toString(),
                registerDate = Date().time
            )
        }
        //TODO("change name")
    }

    override suspend fun addNewUser(user: User): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        if (user.anonymous) {
            flow.tryEmit(Progress.Complete)
        } else {
            val userFromDatabase = getUsersCollection().document(user.uid).get().await().toObject(User::class.java)

            if (userFromDatabase != null) {
                appPreferences.saveUserValue(userFromDatabase)
                flow.tryEmit(Progress.Complete)
            } else {
                getUsersCollection().document(user.uid).set(user)
                    .addOnCompleteListener {
                        runBlocking {
                            appPreferences.saveUserValue(user)
                        }
                        flow.tryEmit(Progress.Complete)
                    }
            }

        }
        return flow
    }

    suspend fun getCurrentUserFromDatabase(userId: String) = callbackFlow {
        getUsersCollection().document(userId).get().addOnCompleteListener {
            if (it.result.exists()) {
                trySend(true)
            }
        }
        awaitClose { }
    }

    private fun getUsersCollection(): CollectionReference {
        return db.collection(USERS_COLLECTION)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}