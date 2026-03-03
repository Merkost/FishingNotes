package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobileprism.fishing.ui.utils.placeholder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<UserViewModel>()
    val user by viewModel.currentUser.collectAsState()
    val placesState by viewModel.currentPlaces.collectAsState()
    val catchesState by viewModel.currentCatches.collectAsState()
    val favoritePlace by viewModel.favoritePlace.collectAsState()
    val bestCatch by viewModel.bestCatch.collectAsState()
    val imgSize: Dp = remember { 120.dp }
    val avatarOverlap = imgSize / 2

    val imageBorderStroke = remember {
        BorderStroke(
            2.dp,
            Brush.linearGradient(colors = listOf(Color(0xFFED2939), Color(0xFFFFFF66)))
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ProfileAppBar(navController) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(avatarOverlap + 25.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = avatarOverlap),
                shape = AbsoluteRoundedCornerShape(25.dp, 25.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = avatarOverlap + 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserText(user, modifier = Modifier)

                    SecondaryText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = "${stringResource(Res.string.register_date)}: " +
                                user.registerDate.toDateTextMonth()
                    )

                    StatsRow(
                        modifier = Modifier.padding(top = 24.dp),
                        catchesCount = catchesState?.size,
                        placesCount = placesState?.size
                    )

                    FavoritePlaceView(
                        modifier = Modifier.padding(top = 24.dp),
                        favoritePlace = favoritePlace,
                        userPlaceClicked = {
                            navController.navigate(MainDestinations.Place(it))
                        },
                        navigateToMap = {
                            navController.navigate(
                                MainDestinations.Map(isAddingNewPlace = false, place = it)
                            )
                        }
                    )

                    BestCatchView(
                        modifier = Modifier.padding(top = 16.dp),
                        bestCatch = bestCatch,
                        onCatchItemClick = {
                            navController.navigate(MainDestinations.Catch(it))
                        }
                    )
                }
            }

            UserImage(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                imgSize = imgSize,
                icon = Icons.Default.Edit,
                borderStroke = imageBorderStroke
            ) {
                navController.navigate(MainDestinations.EditProfile)
            }
        }
    }
}

@Composable
fun StatsRow(
    modifier: Modifier = Modifier,
    catchesCount: Int?,
    placesCount: Int?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatchesNumber(
            modifier = Modifier.weight(1f),
            userCatchesNum = catchesCount
        )

        HorizontalDivider(
            modifier = Modifier
                .height(32.dp)
                .width(1.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        PlacesNumber(
            modifier = Modifier.weight(1f),
            userPlacesNum = placesCount
        )
    }
}

@Composable
fun UserText(user: User?, modifier: Modifier) {
    user?.let {
        Text(
            modifier = modifier,
            text = if (user.displayName.isEmpty()) stringResource(Res.string.anonymous) else user.displayName,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlacesNumber(modifier: Modifier = Modifier, userPlacesNum: Int?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Icon(
            Icons.Default.Place, stringResource(Res.string.place),
            modifier = Modifier
                .size(25.dp)
                .placeholder(
                    visible = userPlacesNum == null,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
        )
        Text(
            text = userPlacesNum?.toString() ?: "",
        )
    }
}

@Composable
fun CatchesNumber(modifier: Modifier = Modifier, userCatchesNum: Int?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Icon(
            painterResource(Res.drawable.ic_fishing), stringResource(Res.string.place),
            modifier = Modifier
                .size(25.dp)
                .placeholder(
                    visible = userCatchesNum == null,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
        )
        Text(
            text = userCatchesNum?.toString() ?: "",
        )
    }
}
