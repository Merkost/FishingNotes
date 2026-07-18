package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.LinkOutcome
import com.mobileprism.fishing.domain.repository.NoConnectionException
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.ReauthRequiredException
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.domain.use_cases.auth.planGuestMerge
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.CATCHES_COLLECTION
import com.mobileprism.fishing.model.datasource.utils.RepositoryConstants.MARKERS_COLLECTION
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class FirebaseUserRepositoryImpl(
    private val userDatastore: UserDatastore,
    private val dbCollections: RepositoryCollections,
    private val analyticsTracker: AnalyticsTracker,
    private val catchDao: CatchDao,
    private val markerDao: MarkerDao,
    private val pendingOperationDao: PendingOperationDao,
    private val photoStorage: PhotoStorage,
    private val connectionManager: ConnectionManager,
) : UserRepository {

    private val fireBaseAuth = Firebase.auth

    override val isLoggedIn: Boolean
        get() = fireBaseAuth.currentUser != null

    override val cachedUser: User?
        get() = fireBaseAuth.currentUser?.toUser()

    override val currentUser: Flow<User?>
        get() = fireBaseAuth.authStateChanged.map { it?.toUser() }

    override val isAnonymous: Flow<Boolean>
        get() = fireBaseAuth.authStateChanged.map { it?.isAnonymous ?: true }

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
        clearLocalUserData()
        fireBaseAuth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No signed-in user"))
        if (connectionManager.getConnectionState() !is ConnectionState.Available) {
            return Result.failure(NoConnectionException())
        }
        return try {
            withContext(NonCancellable) {
                clearLocalUserData()
            }
            val markerDocs = dbCollections.getUserMapMarkersCollection().get().documents
            markerDocs.forEach { markerDoc ->
                val catchDocs = dbCollections.getUserCatchesCollection(markerDoc.id).get().documents
                catchDocs.forEach { catchDoc ->
                    runCatching { catchDoc.data<UserCatch>().downloadPhotoLinks }
                        .getOrDefault(emptyList())
                        .forEach { url -> photoStorage.deletePhoto(url) }
                    catchDoc.reference.delete()
                }
                markerDoc.reference.delete()
            }
            dbCollections.getUsersCollection().document(user.uid).delete()
            user.delete()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Result.failure(ReauthRequiredException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        val user = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No signed-in user"))
        return try {
            user.reauthenticate(GoogleAuthProvider.credential(idToken, null))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        return try {
            fireBaseAuth.signInAnonymously()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearGuestData(): Result<Unit> {
        return try {
            val anonUid = fireBaseAuth.currentUser?.uid
            withContext(NonCancellable) { clearLocalUserData() }
            if (anonUid != null) {
                runCatching { dbCollections.getUsersCollection().document(anonUid).delete() }
                runCatching { fireBaseAuth.currentUser?.delete() }
            }
            signInAnonymously()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkWithGoogle(idToken: String): Result<LinkOutcome> {
        val user = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No signed-in user"))
        return try {
            val result = user.linkWithCredential(GoogleAuthProvider.credential(idToken, null))
            val linked = result.user ?: fireBaseAuth.currentUser
            if (linked != null) addNewUser(linked.toUser())
            Result.success(LinkOutcome.Linked)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mergeGuestIntoGoogle(idToken: String): Result<LinkOutcome> {
        return try {
            val anonUid = fireBaseAuth.currentUser?.uid
            val (guestMarkers, guestCatches) = snapshotCurrentUserData()

            val result =
                fireBaseAuth.signInWithCredential(GoogleAuthProvider.credential(idToken, null))
            val linkedUser = result.user
                ?: fireBaseAuth.currentUser
                ?: return Result.failure(IllegalStateException("Sign-in failed during merge"))
            val newUid = linkedUser.uid

            val (existingMarkers, existingCatches) = snapshotCurrentUserData()
            val plan = planGuestMerge(guestMarkers, existingMarkers, guestCatches, existingCatches)

            plan.markersToCopy.forEach { marker ->
                dbCollections.getUserMapMarkersCollection().document(marker.id)
                    .set(marker.copy(userId = newUid))
            }
            plan.catchesToCopy.forEach { userCatch ->
                dbCollections.getUserCatchesCollection(userCatch.userMarkerId)
                    .document(userCatch.id)
                    .set(userCatch.copy(userId = newUid))
            }

            withContext(NonCancellable) { clearLocalUserData() }

            addNewUser(linkedUser.toUser())

            if (anonUid != null && anonUid != newUid) {
                runCatching { deleteOrphanedUserData(anonUid) }
            }

            Result.success(
                LinkOutcome.Merged(
                    catchesAdded = plan.catchesToCopy.size,
                    markersAdded = plan.markersToCopy.size,
                    alreadyPresent = plan.alreadyPresent,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun snapshotCurrentUserData(): Pair<List<UserMapMarker>, List<UserCatch>> {
        val markerDocs = dbCollections.getUserMapMarkersCollection().get().documents
        val markers = markerDocs.mapNotNull { runCatching { it.data<UserMapMarker>() }.getOrNull() }
        val catches = markerDocs.flatMap { markerDoc ->
            dbCollections.getUserCatchesCollection(markerDoc.id).get().documents
                .mapNotNull { runCatching { it.data<UserCatch>() }.getOrNull() }
        }
        return markers to catches
    }

    private suspend fun deleteOrphanedUserData(anonUid: String) {
        val userDoc = dbCollections.getUsersCollection().document(anonUid)
        val markerDocs = userDoc.collection(MARKERS_COLLECTION).get().documents
        markerDocs.forEach { markerDoc ->
            markerDoc.reference.collection(CATCHES_COLLECTION).get().documents
                .forEach { catchDoc -> catchDoc.reference.delete() }
            markerDoc.reference.delete()
        }
        userDoc.delete()
    }

    private suspend fun clearLocalUserData() {
        pendingOperationDao.deleteAll()
        catchDao.deleteAll()
        markerDao.deleteAll()
        userDatastore.clearUser()
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
