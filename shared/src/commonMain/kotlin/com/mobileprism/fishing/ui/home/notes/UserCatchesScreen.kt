package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.ErrorView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.ui.viewmodels.UserCatchesViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject()
) {
    val catchesSortValue by notesPreferences.getCatchesSortValue.collectAsState(CatchesSortValues.Default)
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val lazyPagingItems = viewModel.catchesPaged.collectAsLazyPagingItems()

    val userCatchClicked = remember<(UserCatch) -> Unit>(navController) {
        { catch -> navController.navigate(MainDestinations.Catch(catch)) }
    }
    val navigateToNewCatch = remember(navController) {
        { navController.navigate(MainDestinations.NewCatch()) }
    }

    viewModel.setSortOrder(catchesSortValue)

    Scaffold(containerColor = Color.Transparent) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refresh()
                lazyPagingItems.refresh()
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                when (lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        items(5) {
                            CatchItemSkeleton()
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 128.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ErrorView()
                                DefaultButtonOutlined(
                                    text = stringResource(Res.string.retry),
                                    onClick = { lazyPagingItems.refresh() }
                                )
                            }
                        }
                    }
                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0) {
                            item {
                                NoContentView(
                                    modifier = Modifier.padding(top = 128.dp),
                                    text = stringResource(Res.string.no_cathces_added),
                                    icon = painterResource(Res.drawable.ic_fishing)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    DefaultButtonOutlined(
                                        text = stringResource(Res.string.new_catch_text),
                                        onClick = navigateToNewCatch
                                    )
                                }
                            }
                        } else {
                            when (catchesSortValue) {
                                CatchesSortValues.TimeAsc, CatchesSortValues.TimeDesc -> {
                                    var lastDate: String? = null
                                    for (index in 0 until lazyPagingItems.itemCount) {
                                        val catch = lazyPagingItems.peek(index)
                                        val currentDate = catch?.date?.toDateTextMonth()
                                        if (currentDate != null && currentDate != lastDate) {
                                            stickyHeader(key = "header_$currentDate") {
                                                ItemDate(text = currentDate)
                                            }
                                            lastDate = currentDate
                                        }
                                        item(key = catch?.id ?: "item_$index") {
                                            lazyPagingItems[index]?.let {
                                                CatchItemView(
                                                    catch = it,
                                                    onClick = userCatchClicked,
                                                    childModifier = Modifier
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    items(
                                        count = lazyPagingItems.itemCount,
                                        key = { lazyPagingItems.peek(it)?.id ?: "item_$it" }
                                    ) { index ->
                                        lazyPagingItems[index]?.let {
                                            CatchItemView(
                                                catch = it,
                                                onClick = userCatchClicked,
                                                childModifier = Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
