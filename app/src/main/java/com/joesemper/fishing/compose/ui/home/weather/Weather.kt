package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.map.FishLoading
import com.joesemper.fishing.compose.ui.home.map.getCurrentLocation
import com.joesemper.fishing.compose.ui.home.map.locationPermissionsList
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@Composable
fun Weather(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: WeatherViewModel = getViewModel()

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(locationPermissionsList)

    val lastKnownLocation = remember {
        getCurrentLocation(context = context, permissionsState = permissionsState)
    }

    val userPlaces by remember {
        val currentPlace =
            UserMapMarker(
                title = "Current location",
                latitude = lastKnownLocation.value.latitude,
                longitude = lastKnownLocation.value.longitude
            )
        mutableStateOf(mutableListOf(currentPlace))
    }

    LaunchedEffect(viewModel) {
        viewModel.getAllMarkers().collect { places ->
            userPlaces.addAll(places as List<UserMapMarker>)
        }
    }

    var selectedPlace by remember {
        mutableStateOf(userPlaces.first())
    }

    val currentWeather = remember {
        mutableStateOf<WeatherForecast?>(null)
    }

    LaunchedEffect(selectedPlace) {
        val start = System.currentTimeMillis()
        viewModel.getWeather(selectedPlace.latitude, selectedPlace.longitude).collect {
            if (System.currentTimeMillis() - start < 400) delay(400)
            currentWeather.value = it
        }
    }

    var isExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar() {

                IconButton(onClick = { upPress() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, "")
                }


                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 8.dp)
                            .clickable {
                                isExpanded = !isExpanded
                            },
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            text = selectedPlace.title
                        )
                        Icon(imageVector = Icons.Filled.ArrowDropDown, "")

                        DropdownMenu(
                            modifier = Modifier.width(200.dp),
                            expanded = isExpanded,
                            onDismissRequest = {
                                isExpanded = !isExpanded
                            }) {
                            userPlaces.forEachIndexed { index, userMapMarker ->
                                DropdownMenuItem(onClick = {
                                    selectedPlace = userPlaces[index]
                                    isExpanded = !isExpanded
                                }) {
                                    Row() {
                                        Icon(
                                            painter = painterResource(
                                                id =
                                                if (index == 0) {
                                                    R.drawable.ic_baseline_my_location_24
                                                } else {
                                                    R.drawable.ic_baseline_location_on_24
                                                }
                                            ),
                                            tint = secondaryFigmaColor,
                                            contentDescription = "Location icon",
                                            modifier = Modifier.padding(4.dp)
                                        )
                                        Text(
                                            maxLines = 1,
                                            text = userMapMarker.title
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        AnimatedVisibility (currentWeather.value != null,
                enter = fadeIn() + expandIn(expandFrom = Alignment.BottomStart)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                // verticalArrangement = Arrangement.Center
            ) {


                WeatherForecastLayout(navController, currentWeather.value!!)
            }
        }
            AnimatedVisibility (currentWeather.value == null,
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize().systemBarsPadding(false),
                     verticalArrangement = Arrangement.Center
                ) {
                WeatherLoading(modifier = Modifier.size(500.dp).align(Alignment.CenterHorizontally))
                    //Spacer(modifier = Modifier.size())
            }
        }
    }
}

@Composable
fun WeatherLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.clouds))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun WeatherForecastLayout(
    navController: NavController,
    weatherForecast: WeatherForecast
) {
    val tabs by remember(weatherForecast) {
        mutableStateOf(listOf(TabItem.ForADay(weatherForecast), TabItem.ForAWeek(weatherForecast)))
    }

    val pagerState = rememberPagerState(pageCount = tabs.size)

    WeatherTabs(tabs = tabs, pagerState = pagerState)
    Spacer(modifier = Modifier.height(8.dp))

    WeatherTabsContent(tabs = tabs, pagerState = pagerState, navController)
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun WeatherTabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = Color.White,
        contentColor = Color.Black,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            // OR Tab()
            LeadingIconTab(
                icon = { Icon(painter = painterResource(id = tab.icon), contentDescription = "") },
                text = { Text(tab.title) },
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
fun WeatherTabsContent(tabs: List<TabItem>, pagerState: PagerState, navController: NavController) {
    HorizontalPager(state = pagerState) { page ->
        tabs[page].screen(navController)
    }
}


