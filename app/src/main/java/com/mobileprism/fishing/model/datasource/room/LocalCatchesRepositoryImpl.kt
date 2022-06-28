package com.mobileprism.fishing.model.datasource.room

import android.net.Uri
import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.entity.common.ContentStateOld
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.model.datasource.room.dao.CatchesDao
import kotlinx.coroutines.flow.*

class LocalCatchesRepositoryImpl(
    private val catchesDao: CatchesDao,
    private val firebaseAnalytics: FirebaseAnalytics,
) : CatchesRepository {


    override fun getAllUserCatchesState() = channelFlow<ContentStateOld<UserCatch>> {
        /*catchesDao.getAllCatches().collect {
            send(ContentStateOld)
        }*/
        // TODO:
    }

    override fun getCatchById(markerId: String, catchId: String): Flow<UserCatch?> = flow {
        catchesDao.getCatchById(catchId).collect {
            emit(it)
        }
    }

    /*@ExperimentalCoroutinesApi
    private suspend fun getUserCatchesStateListener(scope: ProducerScope<ContentStateOld<UserCatch>>)
    : OnSuccessListener<in QuerySnapshot> =
        OnSuccessListener<QuerySnapshot> { task ->
            scope.launch {
                getCatchesStateFromDoc(task.documents).collect {
                    scope.trySend(it)
                }
            }
        }*/

    override fun getAllUserCatchesList() = channelFlow {
        catchesDao.getAllCatches().collect {
            send(it)
        }
    }

    override suspend fun getCatchesByMarkerId(markerId: String) =
        Result.success(catchesDao.getCatchesByMarker(markerId).last())


    override fun subscribeOnUserCatchState(markerId: String, catchId: String): Flow<UserCatch> =
        flow {
            catchesDao.getCatchById(catchId).collect {
                it?.let { emit(it) }
            }
        }


    override fun addNewCatch(
        markerId: String,
        newCatch: UserCatch
    ) = channelFlow {
        catchesDao.addCatch(newCatch)
        send(Result.success(Unit))
    }

    override suspend fun deleteCatch(userCatch: UserCatch) {
        catchesDao.deleteCatch(userCatch)
    }

    override suspend fun updateUserCatch(
        userCatch: UserCatch,
    ) {
        catchesDao.updateCatch(userCatch)
    }

    override suspend fun updateUserCatchPhotos(
        markerId: String,
        catchId: String,
        newPhotos: List<Uri>
    ): StateFlow<Progress> {
        TODO("Not yet implemented")
    }

    private fun incrementNumOfCatches(markerId: String) {
        // TODO:
    }

    private fun decrementNumOfCatches(markerId: String) {
        // TODO:
    }

}