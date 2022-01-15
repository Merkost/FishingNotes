package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.placeholder
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.utils.CatchesSortValues
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.model.datastore.NotesPreferences
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.utils.time.toDateTextMonth
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = getViewModel(),
    notesPreferences: NotesPreferences = get()
) {
    val uiState = viewModel.uiState.collectAsState()
    val catchesSortValue by notesPreferences.catchesSortValue.collectAsState(CatchesSortValues.Default)

    Scaffold(backgroundColor = Color.Transparent) {
        val catches by viewModel.currentContent.collectAsState()
        UserCatches(
            catchesState = uiState,
            catches = catchesSortValue.sort(catches),
            userCatchClicked = { catch -> onCatchItemClick(catch, navController) },
            sortValue = catchesSortValue
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun UserCatches(
    modifier: Modifier = Modifier,
    catchesState: State<UiState>,
    sortValue: CatchesSortValues,
    userCatchClicked: (UserCatch) -> Unit,
    catches: List<UserCatch>,

    ) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        when(catchesState.value) {
            UiState.Success -> {
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
                                            onClick = userCatchClicked,
                                            childModifier = Modifier
                                        )
                                    }
                                }
                            }
                            else -> {
                                items(items = catches) {
                                    CatchItemView(
                                        catch = it,
                                        onClick = userCatchClicked,
                                        childModifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                    catches.isEmpty() -> {
                        item {
                            NoElementsView(
                                mainText = stringResource(R.string.no_cathces_added),
                                secondaryText = stringResource(R.string.add_catch_text),
                                onClickAction = { }
                            )
                        }
                    }
                }
            }
            else -> {
                items(3) {
                    CatchItemView(
                        childModifier = Modifier.placeholder(
                            true,
                            color = Color.Gray,
                            shape = CircleShape,
                            highlight = PlaceholderHighlight.fade()
                        ),
                        catch = UserCatch(),
                        onClick = userCatchClicked
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