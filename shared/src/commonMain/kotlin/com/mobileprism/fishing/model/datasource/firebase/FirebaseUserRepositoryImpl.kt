package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FirebaseUserRepositoryImpl(
    private val userDatastore: UserDatastore,
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
) : UserRepository {

    private val fireBaseAuth = Firebase.auth

    override val currentUser: Flow<User?>
        get() = fireBaseAuth.authStateChanged.map { firebaseUser ->
            firebaseUser?.let {
                User(
                    uid = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: "Anonymous",
                    photoUrl = it.photoURL ?: "",
                    registerDate = Clock.System.now().toEpochMilliseconds()
                )
            }
        }

    override val datastoreUser: Flow<User>
        get() = flow { userDatastore.getUser.collect { emit(it) } }

    override suspend fun logoutCurrentUser() = flow {
        fireBaseAuth.signOut()
        emit(true)
    }

    override suspend fun addNewUser(user: User): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        try {
            val userDoc = dbCollections.getUsersCollection().document(user.uid).get()
            val userFromDatabase = if (userDoc.exists) {
                userDoc.data<User>()
            } else null

            if (userFromDatabase != null && userFromDatabase.registerDate != 0L) {
                analyticsTracker.logEvent(AnalyticsEvent.Login(method = "Google"))
                userDatastore.saveUser(userFromDatabase)
                flow.tryEmit(Progress.Complete)
            } else {
                try {
                    dbCollections.getUsersCollection().document(user.uid).set(user)
                    analyticsTracker.logEvent(AnalyticsEvent.SignUp(method = "Google"))
                    userDatastore.saveUser(user)
                    flow.tryEmit(Progress.Complete)
                } catch (e: Exception) {
                    println("Fishing: Failed to set new user: ${e.message}")
                    flow.tryEmit(Progress.Complete)
                }
            }
        } catch (e: Exception) {
            println("Fishing: addNewUser failed: ${e.message}")
            flow.tryEmit(Progress.Complete)
        }

        return flow
    }

    override suspend fun setUserListener(user: User) {
        dbCollections.getUsersCollection().document(user.uid).snapshots.collect { snapshot ->
            if (snapshot.exists) {
                try {
                    val userToUpdate = snapshot.data<User>()
                    userDatastore.saveUser(userToUpdate)
                } catch (e: Exception) {
                    println("Fishing: User snapshot deserialization error: ${e.message}")
                }
            }
        }
    }

    override suspend fun setNewProfileData(user: User): Result<Unit> {
        return try {
            dbCollections.getUsersCollection().document(user.uid).set(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
