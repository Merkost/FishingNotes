package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mobileprism.fishing.domain.entity.common.SortDirection
import com.mobileprism.fishing.domain.entity.common.toGitliveDirection
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import kotlinx.coroutines.flow.Flow

class FirebaseMarkersPagedRepository(
    private val coreRepo: FirebaseMarkersRepositoryImpl,
    private val authRepository: AuthRepository,
    private val dbCollections: RepositoryCollections
) : MarkersRepositoryPaged, MarkersRepository by coreRepo {

    override fun getAllUserMarkersListPaged(
        sortField: String,
        sortDirection: SortDirection
    ): Flow<PagingData<UserMapMarker>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            MarkersPagingSource(
                dbCollections.getUserMapMarkersCollection(),
                sortField,
                sortDirection.toGitliveDirection()
            )
        }
    ).flow
}
