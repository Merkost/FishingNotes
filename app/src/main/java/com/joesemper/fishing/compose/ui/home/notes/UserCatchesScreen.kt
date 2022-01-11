package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.views.DefaultButtonOutlined
import com.joesemper.fishing.compose.ui.home.views.NoContentView
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.utils.CatchesSortValues
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.model.datastore.NotesPreferences
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.time.toDateTextMonth
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = getViewModel()
) {
    val notesPreferences: NotesPreferences = get()
    val catchesSortValue by notesPreferences.catchesSortValue
        .collectAsState(CatchesSortValues.Default)

    Scaffold(backgroundColor = Color.Transparent) {
        val catches: List<UserCatch> = viewModel.currentContent
        Crossfade(catches) { animatedUiState ->
            UserCatches(
                catches = catchesSortValue.sort(animatedUiState),
                userCatchClicked = { catch -> onCatchItemClick(catch, navController) },
                sortValue = catchesSortValue,
                navigateToNewCatch = {
                    navController.navigate(
                        MainDestinations.NEW_CATCH_ROUTE,
                        Arguments.PLACE to UserMapMarker()
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun UserCatches(
    modifier: Modifier = Modifier,
    catches: List<UserCatch>,
    sortValue: CatchesSortValues,
    userCatchClicked: (UserCatch) -> Unit,
    navigateToNewCatch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            catches.isNotEmpty() -> {
                when (sortValue) {
                    CatchesSortValues.TimeAsc, CatchesSortValues.TimeDesc -> {
                        getDatesList(catches).forEach { catchDate ->
                            stickyHeader {
                                ItemDate(text = catchDate)
                            }
                            items(
                                items = catches
                                    .filter { userCatch ->
                                        userCatch.date.toDateTextMonth() == catchDate
                                    }
                                    .sortedByDescending { it.date },
                                key = {
                                    it
                                }
                            ) {
                                CatchItemView(
                                    catch = it,
                                    onClick = userCatchClicked
                                )
                            }
                        }
                    }
                    else -> {
                        items(items = catches) {
                            CatchItemView(
                                catch = it,
                                onClick = userCatchClicked
                            )
                        }
                    }
                }
            }
            catches.isEmpty() -> {
                item {
                    NoContentView(
                        modifier = Modifier.padding(top = 128.dp),
                        text = stringResource(id = R.string.no_cathces_added),
                        icon = painterResource(id = R.drawable.ic_fishing)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    DefaultButtonOutlined(
                        text = stringResource(R.string.new_catch_text),
                        onClick = navigateToNewCatch
                    )
                }
            }
        }
    }
}

fun getDatesList(catches: List<UserCatch>): List<String> {
    val dates = mutableListOf<String>()
    catches.forEach { userCatch ->
        val date = userCatch.date.toDateTextMonth()
        if (!dates.contains(date)) {
            dates.add(date)
        }
    }
    return dates
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}