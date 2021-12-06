package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.utils.time.TimeManager
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserCatchesScreen(
    navController: NavController,
    viewModel: UserCatchesViewModel = getViewModel()
) {
    Scaffold(backgroundColor = Color.Transparent) {
        val catches by viewModel.currentContent.collectAsState()
        Crossfade(catches) { animatedUiState ->
            if (animatedUiState != null) {
                UserCatches(
                    catches = animatedUiState,
                    userCatchClicked = { catch -> onCatchItemClick(catch, navController) })
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun UserCatches(
    catches: List<UserCatch>,
    userCatchClicked: (UserCatch) -> Unit
) {
    val timeManager: TimeManager = get()
    val userPreferences: UserPreferences = get()
    val timeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when {
            catches.isNotEmpty() -> {
                getDatesList(catches, timeManager).forEach { catchDate ->
                    item {
                        ItemDate(text = catchDate)
                    }
                    items(items = catches
                        .filter { userCatch ->
                            timeManager.getDate(userCatch.date) == catchDate
                        }
                        .sortedByDescending { it.date }) {
                        ItemUserCatch(
                            userCatch = it,
                            userCatchClicked = userCatchClicked
                        )
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
}

private fun getDatesList(catches: List<UserCatch>, timeManager: TimeManager): List<String> {
    val dates = mutableListOf<String>()
    catches.sortedByDescending { it.date }.forEach { userCatch ->
        val date = timeManager.getDate(userCatch.date)
        if (!dates.contains(date)) {
            dates.add(date)
        }
    }
    return dates
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}