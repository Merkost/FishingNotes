package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mobileprism.fishing.domain.entity.common.SortDirection
import com.mobileprism.fishing.domain.entity.common.toGitliveDirection
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import kotlinx.coroutines.flow.Flow

class FirebaseCatchesPagedRepository(
    private val coreRepo: FirebaseCatchesRepositoryImpl,
    private val authRepository: AuthRepository,
    private val dbCollections: RepositoryCollections
) : CatchesRepository,
    CatchesRepositoryRead by coreRepo,
    CatchesRepositoryUpdate by coreRepo {

    override fun getAllUserCatchesPaged(
        sortField: String,
        sortDirection: SortDirection
    ): Flow<PagingData<UserCatch>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            CatchesPagingSource(
                dbCollections.db,
                authRepository.getCurrentUserId(),
                sortField,
                sortDirection.toGitliveDirection()
            )
        }
    ).flow
}
