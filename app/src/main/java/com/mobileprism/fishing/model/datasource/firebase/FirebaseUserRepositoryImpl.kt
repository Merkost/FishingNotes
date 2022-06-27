package com.mobileprism.fishing.model.datasource.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.di.repositoryModuleFirebase
import com.mobileprism.fishing.di.repositoryModuleLocal
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.async
import com.mobileprism.fishing.utils.Constants.OFFLINE_USER_ID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.context.GlobalContext.unloadKoinModules
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseUserRepositoryImpl(
    private val userDatastore: UserDatastore,
    private val dbCollections: RepositoryCollections,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val context: Context,
) : FirebaseUserRepository {

    private val fireBaseAuth = FirebaseAuth.getInstance()

    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val savedUser = userDatastore.getUser.first()
            if (savedUser.uid == OFFLINE_USER_ID) {
                send(savedUser)
            } else {
                val authListener = FirebaseAuth.AuthStateListener {
                    runBlocking {
                        send(it.currentUser?.run { mapFirebaseUserToUser(this) })
                    }
                }

                fireBaseAuth.addAuthStateListener(authListener)
                awaitClose { fireBaseAuth.removeAuthStateListener(authListener) }
            }
        }

    override val datastoreUser: Flow<User>
        get() = userDatastore.getUser

    override val datastoreNullableUser: Flow<User?>
        get() = userDatastore.getNullableUser

    override suspend fun logoutCurrentUser() = callbackFlow {
        AuthUI.getInstance().signOut(context).addOnCompleteListener {
            if (it.isSuccessful) {
                Firebase.analytics.logEvent("logout", null)
                runBlocking { userDatastore.clearUser() }
                trySend(true)
            } else {
                trySend(false)
            }
        }
        /*when (datastoreUser.first().loginType) {
            LoginType.LOCAL -> {
                userDatastore.clearUser()
                trySend(true)
            }
            LoginType.GOOGLE -> {
                AuthUI.getInstance().signOut(context).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Firebase.analytics.logEvent("logout", null)
                        runBlocking { userDatastore.clearUser() }
                        trySend(true)
                    } else trySend(false)
                }
            }
            null -> TODO()
        }*/
        awaitClose()
    }

    private fun reloadRepositories() {
        clearPersistence()
        unloadKoinModules(repositoryModuleFirebase)
        unloadKoinModules(repositoryModuleLocal)
    }

    private fun clearPersistence() {
        Firebase.firestore.clearPersistence()
    }

    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        return with(firebaseUser) {
            User(
                uid = uid,
                email = firebaseUser.email ?: "",
                displayName = displayName ?: "Anonymous",
                photoUrl = photoUrl.toString(),
                registerDate = Date().time,
                loginType = LoginType.GOOGLE
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

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.METHOD, "Google")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)


            userDatastore.saveUser(userFromDatabase)
            flow.tryEmit(Progress.Complete)
        } else {
            dbCollections.getUsersCollection().document(user.uid).set(user)
                .addOnCompleteListener {

                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Google")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                    runBlocking {
                        userDatastore.saveUser(user)
                    }
                    flow.tryEmit(Progress.Complete)
                }

        }
        return flow
    }

    override suspend fun addOfflineUser() {
        userDatastore.saveUser(createOfflineUser())
    }

    override suspend fun setUserListener(user: User) {
        val listeners = mutableListOf<ListenerRegistration>()

        listeners.add(
            dbCollections.getUsersCollection().document(user.uid)
                .addSnapshotListener(getUserSnapshotListener())
        )
    }

    override suspend fun setNewProfileData(user: User) =
        suspendCoroutine<Result<Unit>> { continuation ->
            dbCollections.getUsersCollection().document(user.uid).set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) continuation.resume(Result.success(Unit))
                    else continuation.resume(Result.failure(it.exception ?: Throwable()))
                }
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
                        userDatastore.saveUser(userToUpdate)
                    }
                }
            } else {
                Log.d("Fishing", "Current data: null")
            }

        }

    suspend fun getCurrentUserFromDatabase(userId: String) =
        suspendCoroutine<Result<User>> { continuation ->
            dbCollections.getUsersCollection().document(userId).get().addOnCompleteListener {
                val user = it.result.toObject<User>()
                user?.let { continuation.resume(Result.success(it)) } ?: kotlin.run {
                    continuation.resume(
                        Result.failure(
                            it.exception ?: Throwable()
                        )
                    )
                }
            }
        }

    private fun createOfflineUser() = User(uid = OFFLINE_USER_ID)

}