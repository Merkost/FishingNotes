package com.mobileprism.fishing.model.datasource.room

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.UserDatastore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocalUserRepositoryImpl(
    private val userDatastore: UserDatastore,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val context: Context,
) : FirebaseUserRepository {

    override val currentUser: Flow<User?>
        get() = datastoreNullableUser

    override val datastoreUser: Flow<User>
        get() = userDatastore.getUser

    override val datastoreNullableUser: Flow<User?>
        get() = userDatastore.getNullableUser

    override suspend fun logoutCurrentUser() = callbackFlow {
        AuthUI.getInstance().signOut(context).addOnCompleteListener {
            if (it.isSuccessful) {
                Firebase.analytics.logEvent("logout", null)
                reloadRepositories()
                trySend(true)
            } else trySend(false)
        }
        awaitClose {}
    }

    private fun reloadRepositories() {
        //clearPersistence()
        //GlobalContext.unloadKoinModules(repositoryModuleFirebase)
        //GlobalContext.loadKoinModules(repositoryModuleFirebase)
    }

    override suspend fun addNewUser(user: User): StateFlow<Progress> {
        val flow = MutableStateFlow<Progress>(Progress.Loading())

        /* val userFromDatabase =
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

         }*/
        return flow
    }

    override suspend fun addOfflineUser() {

    }

    override suspend fun setUserListener(user: User) {

    }


    override suspend fun setNewProfileData(user: User): Result<Unit> {
        val result = Result.success(userDatastore.saveUser(user))
        return suspendCoroutine<Result<Unit>> {
            it.resume(result)
        }
    }

}