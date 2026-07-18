package com.mobileprism.fishing.ui.home.profile

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.notes.CatchItemView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.views.TextWithLeadingIcon
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.ui.utils.AnimatedResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    navController: NavController,
    isAnonymous: Boolean,
) {
    val dialogOnLogout = rememberSaveable { mutableStateOf(false) }
    AppTopBar(
        title = stringResource(Res.string.profile),
        actions = {
            if (!isAnonymous) {
                IconButton(onClick = { dialogOnLogout.value = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(Res.string.logout)
                    )
                }
            }
            IconButton(onClick = { navController.navigate(MainDestinations.Settings) }) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.settings))
            }
        },
    )
    if (!isAnonymous && dialogOnLogout.value) LogoutDialog(dialogOnLogout)
}

@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<UserViewModel>()
    DefaultDialog(
        primaryText = stringResource(Res.string.logout_dialog_title),
        secondaryText = stringResource(Res.string.logout_dialog_message) +
                "\n\n" + stringResource(Res.string.logout_dialog_guest_note),
        negativeButtonText = stringResource(Res.string.no),
        onNegativeClick = { dialogOnLogout.value = false },
        positiveButtonText = stringResource(Res.string.yes),
        onPositiveClick = {
            scope.launch {
                viewModel.logoutCurrentUser()
                dialogOnLogout.value = false
            }
        },
        onDismiss = { dialogOnLogout.value = false },
        content = {
            LottieLogout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieLogout(modifier: Modifier) {
    AnimatedResource("bye_bye", modifier)
}

@Composable
fun BestCatchView(
    modifier: Modifier = Modifier,
    bestCatch: UserCatch?,
    onCatchItemClick: (UserCatch) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TextWithLeadingIcon(
            icon = painterResource(Res.drawable.ic_cup),
            text = stringResource(Res.string.best_catch),
            style = AppTextStyle.Subtitle,
            color = FishingTheme.colorScheme.primary,
        )

        if (bestCatch != null) {
            CatchItemView(
                catch = bestCatch,
                onClick = onCatchItemClick
            )
        } else {
            NoContentView(
                text = stringResource(Res.string.no_cathces_added),
                icon = painterResource(Res.drawable.ic_fish)
            )
        }
    }
}

@Composable
fun FavoritePlaceView(
    modifier: Modifier = Modifier,
    favoritePlace: UserMapMarker?,
    userPlaceClicked: (UserMapMarker) -> Unit,
    navigateToMap: (UserMapMarker) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TextWithLeadingIcon(
            icon = painterResource(Res.drawable.ic_baseline_star_24),
            text = stringResource(Res.string.favorite_place),
            style = AppTextStyle.Subtitle,
            color = FishingTheme.colorScheme.primary,
        )

        if (favoritePlace != null) {
            ItemUserPlace(
                place = favoritePlace,
                userPlaceClicked = userPlaceClicked,
                navigateToMap = { navigateToMap(favoritePlace) }
            )
        } else {
            NoContentView(
                text = stringResource(Res.string.no_places_added),
                icon = painterResource(Res.drawable.ic_no_place_on_map)
            )
        }
    }
}
