package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.utils.enums.CatchesSortValues
import com.mobileprism.fishing.ui.viewmodels.UserCatchesViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject()
) {
    val uiState = viewModel.uiState.collectAsState()
    val catchesSortValue by notesPreferences.getCatchesSortValue.collectAsState(CatchesSortValues.Default)

    Scaffold(containerColor = Color.Transparent) {
        val catches by viewModel.currentContent.collectAsState()
        UserCatches(
            catchesState = uiState,
            catches = catchesSortValue.sort(catches),
            userCatchClicked = { catch -> navController.navigate(MainDestinations.Catch(catch)) },
            sortValue = catchesSortValue,
            navigateToNewCatch = { navController.navigate(MainDestinations.NewCatch()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserCatches(
    modifier: Modifier = Modifier,
    catchesState: State<UiState>,
    sortValue: CatchesSortValues,
    userCatchClicked: (UserCatch) -> Unit,
    catches: List<UserCatch>,
    navigateToNewCatch: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        when (catchesState.value) {
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
                                NoContentView(
                                    modifier = Modifier.padding(top = 128.dp),
                                    text = stringResource(id = R.string.no_cathces_added),
                                    icon = painterResource(id = R.drawable.ic_fishing)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                            Column(modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                                DefaultButtonOutlined(
                                    text = stringResource(R.string.new_catch_text),
                                    onClick = navigateToNewCatch
                                )
                            }


                        }
                    }
                }
            }
            UiState.InProgress -> {
                items(3) {
                    CatchItemView(
                        childModifier = Modifier.placeholder(
                            true,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                            highlight = PlaceholderHighlight.fade()
                        ),
                        catch = UserCatch(),
                        onClick = {}
                    )
                }
            }
            else -> {
                item {
                    NoContentView(
                        modifier = Modifier.padding(top = 128.dp),
                        text = stringResource(id = R.string.no_cathces_added),
                        icon = painterResource(id = R.drawable.ic_fishing)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Column(modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        DefaultButtonOutlined(
                            text = stringResource(R.string.new_catch_text),
                            onClick = navigateToNewCatch
                        )
                    }
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
