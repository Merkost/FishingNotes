package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.notes.CatchItemView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserImage(
    modifier: Modifier = Modifier,
    user: User,
    imgSize: Dp,
    shape: Shape = CircleShape,
    icon: ImageVector? = null,
    onIconClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
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
                modifier = Modifier
                    .size(imgSize)
                    .clip(shape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        shape
                    )
            )

            icon?.let {
                Card(
                    modifier = Modifier.size(32.dp),
                    shape = shape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    IconButton(modifier = Modifier, onClick = onIconClick) {
                        Icon(
                            icon, icon.name,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAppBar(
    navController: NavController,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    val dialogOnLogout = rememberSaveable { mutableStateOf(false) }
    DefaultAppBar(
        title = stringResource(Res.string.profile),
        backgroundColor = backgroundColor,
        actions = {
            IconButton(onClick = { dialogOnLogout.value = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(Res.string.logout)
                )
            }
            IconButton(onClick = {
                navController.navigate(MainDestinations.Settings)
            }) {
                Icon(Icons.Default.Settings, stringResource(Res.string.settings))
            }
        },
        elevation = 0.dp,
    )
    if (dialogOnLogout.value) LogoutDialog(dialogOnLogout)
}

@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<UserViewModel>()
    DefaultDialog(
        primaryText = stringResource(Res.string.logout_dialog_title),
        secondaryText = stringResource(Res.string.logout_dialog_message),
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
private fun SectionHeader(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@Composable
fun BestCatchView(
    modifier: Modifier = Modifier,
    bestCatch: UserCatch?,
    onCatchItemClick: (UserCatch) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            icon = painterResource(Res.drawable.ic_cup),
            title = stringResource(Res.string.best_catch)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            icon = painterResource(Res.drawable.ic_baseline_star_24),
            title = stringResource(Res.string.favorite_place)
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
