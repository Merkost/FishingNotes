package com.mobileprism.fishing.model.datasource.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot

class MarkersPagingSource(
    private val markersCollection: CollectionReference,
    private val sortField: String,
    private val sortDirection: Direction
) : PagingSource<DocumentSnapshot, UserMapMarker>() {

    override fun getRefreshKey(
        state: PagingState<DocumentSnapshot, UserMapMarker>
    ): DocumentSnapshot? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
                ?: state.closestPageToPosition(position)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, UserMapMarker> {
        return try {
            var query = markersCollection
                .orderBy(sortField, sortDirection)
                .limit(params.loadSize)

            params.key?.let { lastDoc ->
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get()
            val markers = snapshot.documents.mapNotNull { doc ->
                try { doc.data<UserMapMarker>() } catch (_: Exception) { null }
            }
            val lastDocument = snapshot.documents.lastOrNull()

            LoadResult.Page(
                data = markers,
                prevKey = null,
                nextKey = if (markers.size < params.loadSize) null else lastDocument
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
