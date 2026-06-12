package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobileprism.fishing.ui.utils.placeholder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.User
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
    val imgSize = remember { 100.dp }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileAppBar(
                navController = navController,
                backgroundColor = MaterialTheme.colorScheme.surface
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            UserImage(
                user = user,
                imgSize = imgSize,
                icon = Icons.Default.Edit,
            ) {
                navController.navigate(MainDestinations.EditProfile)
            }

            Spacer(modifier = Modifier.height(16.dp))

            UserNameSection(user)

            Spacer(modifier = Modifier.height(24.dp))

            StatsRow(
                catchesCount = catchesState?.size,
                placesCount = placesState?.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            FavoritePlaceView(
                modifier = Modifier.padding(horizontal = 16.dp),
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

            Spacer(modifier = Modifier.height(8.dp))

            BestCatchView(
                modifier = Modifier.padding(horizontal = 16.dp),
                bestCatch = bestCatch,
                onCatchItemClick = {
                    navController.navigate(MainDestinations.Catch(it))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserNameSection(user: User?) {
    user?.let {
        Text(
            text = if (it.displayName.isEmpty()) stringResource(Res.string.anonymous) else it.displayName,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        SecondaryText(
            text = "${stringResource(Res.string.register_date)}: ${it.registerDate.toDateTextMonth()}"
        )
    }
}

@Composable
private fun StatsRow(
    catchesCount: Int?,
    placesCount: Int?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            count = catchesCount,
            label = stringResource(Res.string.catches),
            icon = {
                Icon(
                    painterResource(Res.drawable.ic_fishing),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            count = placesCount,
            label = stringResource(Res.string.places),
            icon = {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    count: Int?,
    label: String,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count?.toString() ?: "-",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .placeholder(
                        visible = count == null,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
