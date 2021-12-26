package com.joesemper.fishing.compose.ui.home.notes

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.NotesPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.DefaultAppBar
import com.joesemper.fishing.compose.ui.home.DefaultDialog
import com.joesemper.fishing.compose.ui.home.FabMenuItem
import com.joesemper.fishing.compose.ui.home.FabWithMenu
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.theme.primaryTextColor
import com.joesemper.fishing.compose.ui.utils.CatchesSortValues
import com.joesemper.fishing.compose.ui.utils.PlacesSortValues
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun Notes(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val notesPreferences: NotesPreferences = get()
    val tabs = remember { listOf(TabItem.Places, TabItem.Catches) }
    val pagerState = rememberPagerState(0)

    val currentPlacesSort = notesPreferences.placesSortValue
        .collectAsState(PlacesSortValues.Default.name)
    val currentCatchesSort = notesPreferences.placesSortValue
        .collectAsState(PlacesSortValues.Default.name)

    val placesFilterDialog = remember { mutableStateOf(false) }
    val catchesFilterDialog = remember { mutableStateOf(false) }

    if (placesFilterDialog.value) PlacesFilterDialog(
        currentSort = currentPlacesSort,
        onDismiss = { placesFilterDialog.value = false },
        onSelectedValue = { newValue ->
            coroutineScope.launch {
                notesPreferences.savePlacesSortValue(newValue.name)
                placesFilterDialog.value = false
            }
        }
    )

    if (catchesFilterDialog.value) CatchesFilterDialog(
        currentSort = currentCatchesSort,
        onDismiss = { catchesFilterDialog.value = false },
        onSelectedValue = { newValue ->
            coroutineScope.launch {
                notesPreferences.saveCatchesSortValue(newValue.name)
                catchesFilterDialog.value = false
            }
        }
    )

    Scaffold(
        topBar = {
            DefaultAppBar(
                onNavClick = { navController.popBackStack() },
                title = stringResource(id = R.string.notes),
                actions = {
                    AnimatedVisibility(pagerState.currentPage == 0 && !pagerState.isScrollInProgress) {
                        PlacesActions() {
                            placesFilterDialog.value = true
                        }
                    }
                    AnimatedVisibility(pagerState.currentPage == 1 && !pagerState.isScrollInProgress) {
                        CatchesActions() {
                            catchesFilterDialog.value = true
                        }
                    }
                    /*when (pagerState.currentPage) {
                        0 -> {
                            PlacesActions() {
                                placesFilterDialog.value = true
                            }
                        }
                        1 -> {
                            Row {}
                        }
                    }*/
                }
            )
        },
        floatingActionButton = {
            FabWithMenu(
                items = listOf(
                    FabMenuItem(
                        icon = R.drawable.ic_add_catch,
                        onClick = { onAddNewCatchClick(navController) }
                    ),
                    FabMenuItem(
                        icon = R.drawable.ic_baseline_add_location_24,
                        onClick = { onAddNewPlaceClick(navController) }
                    )
                )
            )
        },
    ) {
        Column() {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState, navController)
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlacesFilterDialog(
    currentSort: State<String>,
    onDismiss: () -> Unit,
    onSelectedValue: (placesSore: PlacesSortValues) -> Unit
) {
    DefaultDialog(onDismiss = onDismiss) {

        val radioOptions = PlacesSortValues.values().asList()
        val context = LocalContext.current

        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                PlacesSortValues.valueOf(
                    currentSort.value
                )
            )
        }

        Column {
            radioOptions.forEach { placesSortValue ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (placesSortValue == selectedOption),
                            onClick = {
                                onOptionSelected(placesSortValue)
                                onSelectedValue(placesSortValue)
                            }
                        )
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (placesSortValue == selectedOption),
                        modifier = Modifier.padding(all = Dp(value = 8F)),
                        onClick = {
                            onOptionSelected(placesSortValue)
                            onSelectedValue(placesSortValue)
                            Toast.makeText(context, placesSortValue.name, Toast.LENGTH_LONG)
                                .show()
                        }
                    )
                    Text(
                        text = placesSortValue.name,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatchesFilterDialog(
    currentSort: State<String>,
    onDismiss: () -> Unit,
    onSelectedValue: (catchesSort: CatchesSortValues) -> Unit
) {
    DefaultDialog(onDismiss = onDismiss) {

        val radioOptions = CatchesSortValues.values().asList()
        val context = LocalContext.current

        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                CatchesSortValues.valueOf(
                    currentSort.value
                )
            )
        }

        Column {
            radioOptions.forEach { catchesSortValue ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (catchesSortValue == selectedOption),
                            onClick = {
                                onOptionSelected(catchesSortValue)
                                onSelectedValue(catchesSortValue)
                            }
                        )
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = (catchesSortValue == selectedOption),
                        modifier = Modifier.padding(all = Dp(value = 8F)),
                        onClick = {
                            onOptionSelected(catchesSortValue)
                            onSelectedValue(catchesSortValue)
                            Toast.makeText(context, catchesSortValue.name, Toast.LENGTH_LONG)
                                .show()
                        }
                    )
                    Text(
                        text = catchesSortValue.name,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlacesActions(onFilterClick: () -> Unit) {
    Row {
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterAlt, Icons.Default.FilterAlt.name)
        }
    }
}

@Composable
fun CatchesActions(onFilterClick: () -> Unit) {
    Row {
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterAlt, Icons.Default.FilterAlt.name)
        }
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = primaryTextColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon), contentDescription = "",
                        tint = MaterialTheme.colors.primaryVariant
                    )
                },
                text = {
                    Text(
                        stringResource(tab.titleRes),
                        color = MaterialTheme.colors.onSurface
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState, navController: NavController) {
    HorizontalPager(
        state = pagerState, count = tabs.size
    ) { page ->
        tabs[page].screen(navController)
    }
}


private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate(
        MainDestinations.NEW_CATCH_ROUTE,
        Arguments.PLACE to UserMapMarker()
    )
}

private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${addNewPlace}")
}


@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Preview(showBackground = true)
@Composable
fun TabsContentPreview() {
    val tabs = listOf(
        TabItem.Places,
        TabItem.Catches,
    )
    val pagerState = rememberPagerState(1)
    TabsContent(tabs = tabs, pagerState = pagerState, rememberNavController())
}




