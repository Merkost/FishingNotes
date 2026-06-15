package com.mobileprism.fishing.ui.components.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.FishingTheme
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

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
    key: ((T) -> Any)? = null,
    onDelete: ((T) -> Unit)? = null,
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
                            val group = peeked?.let(groupingKey)
                            if (group != null && group != lastKey) {
                                stickyHeader(key = "header_$group") {
                                    AppText(
                                        text = group,
                                        style = AppTextStyle.Support,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = Spacing.xs),
                                    )
                                }
                                lastKey = group
                            }
                            val rowKey = peeked?.let { key?.invoke(it) } ?: "item_$index"
                            item(key = rowKey) {
                                items[index]?.let { value ->
                                    SwipeableRow(value, onDelete, itemContent)
                                }
                            }
                        }
                    } else {
                        items(
                            count = items.itemCount,
                            key = key?.let { keyOf -> { index -> items.peek(index)?.let(keyOf) ?: index } },
                        ) { index ->
                            items[index]?.let { value ->
                                SwipeableRow(value, onDelete, itemContent)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LazyItemScope.SwipeableRow(
    value: T,
    onDelete: ((T) -> Unit)?,
    content: @Composable LazyItemScope.(T) -> Unit,
) {
    if (onDelete == null) {
        content(value)
        return
    }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.EndToStart) {
                onDelete(value)
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { DeleteSwipeBackground(active = dismissState.targetValue != SwipeToDismissBoxValue.Settled) },
    ) {
        this@SwipeableRow.content(value)
    }
}

@Composable
private fun DeleteSwipeBackground(active: Boolean) {
    val background = if (active) FishingTheme.colorScheme.errorContainer else Color.Transparent
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(FishingTheme.shapes.large)
            .background(background)
            .padding(horizontal = Spacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (active) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.delete),
                tint = FishingTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
