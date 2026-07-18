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
import com.mobileprism.fishing.domain.use_cases.auth.GuestMergePlan
import com.mobileprism.fishing.domain.use_cases.auth.planGuestMerge
import com.mobileprism.fishing.model.datasource.local.dao.CatchDao
import com.mobileprism.fishing.model.datasource.local.dao.MarkerDao
import com.mobileprism.fishing.model.datasource.local.dao.PendingOperationDao
import com.mobileprism.fishing.model.datasource.local.mapper.toDomain
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import org.kimplify.cedar.logging.Cedar
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

    private companion object {
        const val LOG_TAG = "FirebaseUserRepository"
        const val METHOD_GOOGLE = "Google"
        const val METHOD_ANONYMOUS = "Anonymous"
    }

    override val isLoggedIn: Boolean
        get() = fireBaseAuth.currentUser != null

    override val cachedUser: User?
        get() = fireBaseAuth.currentUser?.toUser()

    override val currentUser: Flow<User?>
        get() = fireBaseAuth.authStateChanged.map { it?.toUser() }

    override val isAnonymous: Flow<Boolean>
        get() = merge(fireBaseAuth.authStateChanged, fireBaseAuth.idTokenChanged)
            .map { it?.isAnonymous ?: true }
            .distinctUntilChanged()

    private fun dev.gitlive.firebase.auth.FirebaseUser.toUser(): User = User(
        uid = uid,
        email = email ?: "",
        displayName = displayName ?: "",
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
            deleteRemoteUserContent(user.uid)
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
            val result = fireBaseAuth.signInAnonymously()
            val guest = result.user ?: fireBaseAuth.currentUser
            if (guest != null) {
                addNewUser(guest.toUser(), METHOD_ANONYMOUS).onFailure {
                    Cedar.tag(LOG_TAG).e("Failed to persist guest user document: ${it.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteRemoteUserContent(uid: String) {
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
        dbCollections.getUsersCollection().document(uid).delete()
    }

    override suspend fun clearGuestData(): Result<Unit> {
        val user = fireBaseAuth.currentUser ?: return signInAnonymously()
        return try {
            deleteRemoteUserContent(user.uid)
            withContext(NonCancellable) {
                clearLocalUserData()
                user.delete()
                signInAnonymously()
            }
        } catch (e: CancellationException) {
            throw e
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
            if (linked != null) {
                addNewUser(linked.toUser(), METHOD_GOOGLE).onFailure {
                    Cedar.tag(LOG_TAG).e("Failed to persist linked user document: ${it.message}")
                }
            }
            Result.success(LinkOutcome.Linked)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mergeGuestIntoGoogle(idToken: String): Result<LinkOutcome> {
        return try {
            if (fireBaseAuth.currentUser?.isAnonymous != true) {
                return resumeGuestMerge()
            }

            val (guestMarkers, guestCatches) = snapshotCurrentUserData()

            val result =
                fireBaseAuth.signInWithCredential(GoogleAuthProvider.credential(idToken, null))
            val linkedUser = result.user
                ?: fireBaseAuth.currentUser
                ?: return Result.failure(IllegalStateException("Sign-in failed during merge"))

            val (existingMarkers, existingCatches) = snapshotCurrentUserData()
            val plan = planGuestMerge(guestMarkers, existingMarkers, guestCatches, existingCatches)

            applyMergePlan(plan, linkedUser.uid)

            withContext(NonCancellable) { clearLocalUserData() }

            addNewUser(linkedUser.toUser(), METHOD_GOOGLE).onFailure {
                Cedar.tag(LOG_TAG).e("Failed to persist merged user document: ${it.message}")
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

    private suspend fun resumeGuestMerge(): Result<LinkOutcome> {
        val currentUser = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No signed-in user"))
        val currentUid = currentUser.uid

        val pendingMarkers = markerDao.getAllOnce()
            .map { it.toDomain() }
            .filter { it.userId != currentUid }
        val pendingCatches = catchDao.getAllOnce()
            .map { it.toDomain() }
            .filter { it.userId != currentUid }

        if (pendingMarkers.isEmpty() && pendingCatches.isEmpty()) {
            return Result.failure(IllegalStateException("No guest data to merge"))
        }

        Cedar.tag(LOG_TAG).d(
            "Resuming interrupted merge from local cache: " +
                "${pendingMarkers.size} markers, ${pendingCatches.size} catches"
        )

        val (existingMarkers, existingCatches) = snapshotCurrentUserData()
        val plan = planGuestMerge(pendingMarkers, existingMarkers, pendingCatches, existingCatches)

        applyMergePlan(plan, currentUid)

        withContext(NonCancellable) { clearLocalUserData() }

        addNewUser(currentUser.toUser(), METHOD_GOOGLE).onFailure {
            Cedar.tag(LOG_TAG).e("Failed to persist merged user document: ${it.message}")
        }

        return Result.success(
            LinkOutcome.Merged(
                catchesAdded = plan.catchesToCopy.size,
                markersAdded = plan.markersToCopy.size,
                alreadyPresent = plan.alreadyPresent,
            )
        )
    }

    private suspend fun applyMergePlan(plan: GuestMergePlan, targetUid: String) {
        plan.markersToCopy.forEach { marker ->
            dbCollections.getUserMapMarkersCollection().document(marker.id)
                .set(marker.copy(userId = targetUid))
        }
        plan.catchesToCopy.forEach { userCatch ->
            dbCollections.getUserCatchesCollection(userCatch.userMarkerId)
                .document(userCatch.id)
                .set(userCatch.copy(userId = targetUid))
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

    private suspend fun clearLocalUserData() {
        pendingOperationDao.deleteAll()
        catchDao.deleteAll()
        markerDao.deleteAll()
        userDatastore.clearUser()
    }

    override suspend fun addNewUser(user: User): Result<Unit> {
        val method =
            if (fireBaseAuth.currentUser?.isAnonymous == true) METHOD_ANONYMOUS else METHOD_GOOGLE
        return addNewUser(user, method)
    }

    private suspend fun addNewUser(user: User, method: String): Result<Unit> {
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
                analyticsTracker.logEvent(AnalyticsEvent.Login(method = method))
                userDatastore.saveUser(existingUser)
            } else {
                analyticsTracker.logEvent(AnalyticsEvent.SignUp(method = method))
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
