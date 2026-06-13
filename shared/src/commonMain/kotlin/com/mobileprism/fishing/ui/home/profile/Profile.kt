package com.mobileprism.fishing.ui.home.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Phishing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.AvatarWithBadge
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.home.views.StatRow
import com.mobileprism.fishing.ui.home.views.StatTile
import com.mobileprism.fishing.ui.home.views.StatTileSkeleton
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileAppBar(navController = navController)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            AvatarWithBadge(
                contentDescription = stringResource(Res.string.edit_profile_photo),
                onEdit = { navController.navigate(MainDestinations.EditProfile) },
            ) {
                SubcomposeAsyncImage(
                    model = user.photoUrl,
                    contentDescription = stringResource(Res.string.user_photo),
                    contentScale = ContentScale.Crop,
                    error = {
                        Image(
                            modifier = Modifier.padding(8.dp),
                            painter = painterResource(Res.drawable.ic_fisher),
                            contentDescription = stringResource(Res.string.fisher)
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            UserNameSection(user)

            StatRow(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                val catchCount = catchesState
                if (catchCount != null) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Phishing,
                        title = stringResource(Res.string.catches),
                        value = catchCount.size.toString(),
                    )
                } else {
                    StatTileSkeleton(modifier = Modifier.weight(1f))
                }
                val placeCount = placesState
                if (placeCount != null) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Place,
                        title = stringResource(Res.string.places),
                        value = placeCount.size.toString(),
                    )
                } else {
                    StatTileSkeleton(modifier = Modifier.weight(1f))
                }
            }

            FavoritePlaceView(
                modifier = Modifier.padding(horizontal = Spacing.lg),
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
                modifier = Modifier.padding(horizontal = Spacing.lg),
                bestCatch = bestCatch,
                onCatchItemClick = {
                    navController.navigate(MainDestinations.Catch(it))
                }
            )
        }
    }
}

@Composable
private fun UserNameSection(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        Text(
            text = if (user.displayName.isEmpty()) stringResource(Res.string.anonymous) else user.displayName,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        SecondaryText(
            text = stringResource(Res.string.register_date_value, user.registerDate.toDateTextMonth())
        )
    }
}
