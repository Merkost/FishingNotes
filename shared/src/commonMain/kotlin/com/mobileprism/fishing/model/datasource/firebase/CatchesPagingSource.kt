package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobileprism.fishing.domain.entity.content.UserCatch
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where

class CatchesPagingSource(
    private val db: FirebaseFirestore,
    private val userId: String,
    private val sortField: String,
    private val sortDirection: Direction
) : PagingSource<DocumentSnapshot, UserCatch>() {

    override fun getRefreshKey(
        state: PagingState<DocumentSnapshot, UserCatch>
    ): DocumentSnapshot? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
                ?: state.closestPageToPosition(position)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, UserCatch> {
        return try {
            var query = db.collectionGroup("catches")
                .where { "userId" equalTo userId }
                .orderBy(sortField, sortDirection)
                .limit(params.loadSize)

            params.key?.let { lastDoc ->
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get()
            val catches = snapshot.documents.mapNotNull { doc ->
                try { doc.data<UserCatch>() } catch (_: Exception) { null }
            }
            val lastDocument = snapshot.documents.lastOrNull()

            LoadResult.Page(
                data = catches,
                prevKey = null,
                nextKey = if (catches.size < params.loadSize) null else lastDocument
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
