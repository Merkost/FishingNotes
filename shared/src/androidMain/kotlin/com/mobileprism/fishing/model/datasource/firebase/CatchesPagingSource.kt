package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.tasks.await

class CatchesPagingSource(
    private val db: FirebaseFirestore,
    private val userId: String,
    private val sortField: String,
    private val sortDirection: Query.Direction
) : PagingSource<DocumentSnapshot, UserCatch>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, UserCatch>): DocumentSnapshot? = null

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, UserCatch> {
        return try {
            var query = db.collectionGroup("catches")
                .whereEqualTo("userId", userId)
                .orderBy(sortField, sortDirection)
                .limit(params.loadSize.toLong())

            params.key?.let { lastDoc ->
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get().await()
            val catches = snapshot.toObjects(UserCatch::class.java)
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
