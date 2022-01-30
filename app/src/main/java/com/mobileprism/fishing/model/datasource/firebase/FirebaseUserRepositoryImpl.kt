package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.AppPreferences
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.model.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseUserRepositoryImpl(
    private val appPreferences: AppPreferences,
    private val dbCollections: RepositoryCollections,
    private val context: Context
) : UserRepository {

    private val fireBaseAuth = FirebaseAuth.getInstance()

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
            appPreferences.userValue.collectLatest { send(it) }
            awaitClose { }
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
                uid = uid,
                email = firebaseUser.email ?: "",
                displayName = displayName ?: "Anonymous",
                photoUrl = photoUrl.toString(),
                registerDate = Date().time
            )
        }
        //TODO("change name")
    }

    override suspend fun addNewUser(user: User): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        val userFromDatabase =
            dbCollections.getUsersCollection().document(user.uid).get().await()
                .toObject(User::class.java)

        if (userFromDatabase != null && userFromDatabase.registerDate != 0L) {
            appPreferences.saveUserValue(userFromDatabase)
            flow.tryEmit(Progress.Complete)
        } else {
            dbCollections.getUsersCollection().document(user.uid).set(user)
                .addOnCompleteListener {
                    runBlocking {
                        appPreferences.saveUserValue(user)
                    }
                    flow.tryEmit(Progress.Complete)
                }

        }
        return flow
    }

    override suspend fun setUserListener(user: User) {
        val listeners = mutableListOf<ListenerRegistration>()

        listeners.add(
            dbCollections.getUsersCollection().document(user.uid)
                .addSnapshotListener(getUserSnapshotListener())
        )
    }

    private fun getUserSnapshotListener(): EventListener<DocumentSnapshot> =
        EventListener { snapshot, error ->
            if (error != null) {
                Log.d("Fishing", "User snapshot listener", error)
                return@EventListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("Fishing", "Current data: ${snapshot.data}")
                snapshot.toObject(User::class.java)?.let { userToUpdate ->
                    runBlocking {
                        appPreferences.saveUserValue(userToUpdate)
                    }
                }

            } else {
                Log.d("Fishing", "Current data: null")
            }

        }

    suspend fun getCurrentUserFromDatabase(userId: String) = callbackFlow {
        dbCollections.getUsersCollection().document(userId).get().addOnCompleteListener {
            if (it.result.exists()) {
                trySend(true)
            }
        }
        awaitClose { }
    }

}