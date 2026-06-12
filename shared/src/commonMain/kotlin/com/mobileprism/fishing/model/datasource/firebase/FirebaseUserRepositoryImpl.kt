package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class FirebaseUserRepositoryImpl(
    private val userDatastore: UserDatastore,
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
) : UserRepository {

    private val fireBaseAuth = Firebase.auth

    override val isLoggedIn: Boolean
        get() = fireBaseAuth.currentUser != null

    override val cachedUser: User?
        get() = fireBaseAuth.currentUser?.toUser()

    override val currentUser: Flow<User?>
        get() = fireBaseAuth.authStateChanged.map { it?.toUser() }

    private fun dev.gitlive.firebase.auth.FirebaseUser.toUser(): User = User(
        uid = uid,
        email = email ?: "",
        displayName = displayName ?: "Anonymous",
        photoUrl = photoURL ?: "",
        registerDate = Clock.System.now().toEpochMilliseconds()
    )

    override val datastoreUser: Flow<User>
        get() = flow { userDatastore.getUser.collect { emit(it) } }

    override suspend fun logoutCurrentUser() {
        fireBaseAuth.signOut()
    }

    override suspend fun addNewUser(user: User): Result<Unit> {
        return try {
            val userRef = dbCollections.getUsersCollection().document(user.uid)
            val existingUser = dbCollections.db.runTransaction {
                val snapshot = get(userRef)
                if (snapshot.exists) {
                    val existing = snapshot.data<User>()
                    if (existing.registerDate != 0L) {
                        existing
                    } else {
                        set(userRef, user)
                        null
                    }
                } else {
                    set(userRef, user)
                    null
                }
            }

            if (existingUser != null) {
                analyticsTracker.logEvent(AnalyticsEvent.Login(method = "Google"))
                userDatastore.saveUser(existingUser)
            } else {
                analyticsTracker.logEvent(AnalyticsEvent.SignUp(method = "Google"))
                userDatastore.saveUser(user)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setUserListener(user: User) {
        try {
            dbCollections.getUsersCollection().document(user.uid).snapshots.collect { snapshot ->
                if (snapshot.exists) {
                    try {
                        val userToUpdate = snapshot.data<User>()
                        userDatastore.saveUser(userToUpdate)
                    } catch (_: Exception) { }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
