package com.mobileprism.fishing.ui.components.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : Any> PagedListScaffold(
    items: LazyPagingItems<T>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    skeleton: @Composable LazyItemScope.() -> Unit,
    emptyState: @Composable LazyItemScope.() -> Unit,
    modifier: Modifier = Modifier,
    skeletonCount: Int = 5,
    contentPadding: PaddingValues = PaddingValues(
        start = Spacing.screenH,
        end = Spacing.screenH,
        top = Spacing.sm,
        bottom = Spacing.fabClearance,
    ),
    groupingKey: ((T) -> String)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            when (val refresh = items.loadState.refresh) {
                is LoadState.Loading -> {
                    items(skeletonCount) { skeleton() }
                }

                is LoadState.Error -> {
                    item {
                        ErrorStateGeneric(
                            modifier = Modifier.fillParentMaxSize(),
                            onRetry = { items.retry() },
                        )
                    }
                }

                is LoadState.NotLoading -> {
                    if (items.itemCount == 0) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                emptyState()
                            }
                        }
                    } else if (groupingKey != null) {
                        var lastKey: String? = null
                        for (index in 0 until items.itemCount) {
                            val peeked = items.peek(index)
                            val key = peeked?.let(groupingKey)
                            if (key != null && key != lastKey) {
                                stickyHeader(key = "header_$key") {
                                    AppText(
                                        text = key,
                                        style = AppTextStyle.Support,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = Spacing.xs),
                                    )
                                }
                                lastKey = key
                            }
                            item(key = "item_$index") {
                                items[index]?.let { value ->
                                    itemContent(value)
                                }
                            }
                        }
                    } else {
                        items(count = items.itemCount) { index ->
                            items[index]?.let { value ->
                                itemContent(value)
                            }
                        }
                    }
                }
            }

            when (items.loadState.append) {
                is LoadState.Loading -> item { ListAppendLoader() }
                is LoadState.Error -> item {
                    ListAppendLoader(isError = true, onRetry = { items.retry() })
                }
                else -> Unit
            }
        }
    }
}
