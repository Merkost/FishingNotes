package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.components.state.EmptyStateNoCatches
import com.mobileprism.fishing.ui.components.state.PagedListScaffold
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.viewmodels.UserCatchesViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.add_new_catch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject(),
) {
    val catchesSortValue by notesPreferences.getCatchesSortValue
        .collectAsState(CatchesSortValues.Default)
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lazyPagingItems = viewModel.catchesPaged.collectAsLazyPagingItems()

    val onCatchClick = remember<(UserCatch) -> Unit>(navController) {
        { catch -> navController.navigate(MainDestinations.Catch(catch)) }
    }
    val onAddCatch = remember(navController) {
        { navController.navigate(MainDestinations.NewCatch()) }
    }

    LaunchedEffect(catchesSortValue) {
        viewModel.setSortOrder(catchesSortValue)
    }

    val isTimeSorted = catchesSortValue == CatchesSortValues.TimeAsc ||
        catchesSortValue == CatchesSortValues.TimeDesc

    PagedListScaffold(
        items = lazyPagingItems,
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refresh()
            lazyPagingItems.refresh()
        },
        skeleton = { CatchItemSkeleton() },
        emptyState = {
            EmptyStateNoCatches(
                action = {
                    AppButton(
                        text = stringResource(Res.string.add_new_catch),
                        onClick = onAddCatch,
                    )
                },
            )
        },
        groupingKey = if (isTimeSorted) { catch -> catch.date.toDateTextMonth() } else null,
        itemContent = { catch ->
            CatchItemView(
                catch = catch,
                onClick = onCatchClick,
                childModifier = Modifier,
            )
        },
    )
}
