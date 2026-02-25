package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import kotlinx.coroutines.flow.Flow

class FirebaseCatchesPagedRepository(
    private val coreRepo: FirebaseCatchesRepositoryImpl,
    private val authRepository: AuthRepository
) : CatchesRepository,
    CatchesRepositoryRead by coreRepo,
    CatchesRepositoryUpdate by coreRepo {

    override fun getAllUserCatchesPaged(
        sortField: String,
        sortDirection: Query.Direction
    ): Flow<PagingData<UserCatch>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            CatchesPagingSource(
                com.google.firebase.Firebase.firestore,
                authRepository.getCurrentUserId(),
                sortField,
                sortDirection
            )
        }
    ).flow
}
