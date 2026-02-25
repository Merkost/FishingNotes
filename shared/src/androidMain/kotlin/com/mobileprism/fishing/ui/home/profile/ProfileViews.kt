package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.notes.CatchItemView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.home.views.SecondaryTextSmall
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@ExperimentalCoilApi
@Composable
fun UserImage(
    modifier: Modifier = Modifier,
    user: User,
    imgSize: Dp,
    shape: Shape = CircleShape,
    icon: ImageVector? = null,
    borderStroke: BorderStroke? = null,
    onIconClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            icon?.let {
                Card(
                    modifier = Modifier
                        .zIndex(3f)
                        .size(34.dp),
                    shape = shape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    IconButton(modifier = Modifier, onClick = onIconClick) {
                        Icon(icon, icon.name)
                    }
                }
            }

            SubcomposeAsyncImage(
                model = user.photoUrl,
                contentDescription = stringResource(id = R.string.user_photo),
                contentScale = ContentScale.Crop,
                error = {
                    Image(
                        modifier = Modifier.padding(8.dp),
                        painter = painterResource(R.drawable.ic_fisher),
                        contentDescription = stringResource(id = R.string.fisher)
                    )
                },
                modifier = Modifier
                    .size(imgSize)
                    .clip(shape)
                    .border(borderStroke ?: BorderStroke(0.dp, Color.Transparent), shape)
            )
        }
    }
}

@OptIn(InternalCoroutinesApi::class)
@Composable
fun ProfileAppBar(navController: NavController) {

    val dialogOnLogout = rememberSaveable { mutableStateOf(false) }
    DefaultAppBar(
        title = stringResource(R.string.profile),
        actions = {
            IconButton(onClick = { dialogOnLogout.value = true }) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
            IconButton(onClick = {
                navController.navigate(MainDestinations.Settings)
            }) {
                Icon(Icons.Default.Settings, stringResource(R.string.settings))
            }
        },
        elevation = 0.dp,
    )
    if (dialogOnLogout.value) LogoutDialog(dialogOnLogout, navController)
}

@InternalCoroutinesApi
@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>, navController: NavController) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<UserViewModel>()
    val context = LocalContext.current

    DefaultDialog(
        primaryText = stringResource(R.string.logout_dialog_title),
        secondaryText = stringResource(R.string.logout_dialog_message),
        negativeButtonText = stringResource(id = R.string.no),
        onNegativeClick = { dialogOnLogout.value = false },
        positiveButtonText = stringResource(id = R.string.yes),
        onPositiveClick = {
            scope.launch {
                viewModel.logoutCurrentUser().collect { isLogout ->
                    if (isLogout) {
                        dialogOnLogout.value = false

                        navController.navigate(MainDestinations.Login) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bye_bye))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun ProfileItemsTitleView(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.customColors.backgroundSecondaryColor
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier
                    .size(24.dp),
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryText(
                    modifier = Modifier,
                    text = title
                )

                SecondaryTextSmall(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = subtitle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ProfileItemsTitleView(
            title = stringResource(id = R.string.best_catch),
            subtitle = bestCatch?.date?.toDateTextMonth() ?: stringResource(R.string.not_avalable),
            icon = painterResource(id = R.drawable.ic_cup)
        )

        if (bestCatch != null) {
            CatchItemView(
                modifier = Modifier.padding(horizontal = 8.dp),
                catch = bestCatch,
                onClick = onCatchItemClick
            )
        } else {
            NoContentView(
                text = stringResource(id = R.string.no_cathces_added),
                icon = painterResource(id = R.drawable.ic_fish)
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
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ProfileItemsTitleView(
            title = stringResource(id = R.string.favorite_place),
            subtitle = favoritePlace?.title.orEmpty(),
            icon = painterResource(id = R.drawable.ic_baseline_star_24)
        )

        if (favoritePlace != null) {
            ItemUserPlace(
                modifier = Modifier.padding(horizontal = 8.dp),
                place = favoritePlace,
                userPlaceClicked = userPlaceClicked,
                navigateToMap = { navigateToMap(favoritePlace) }
            )
        } else {
            NoContentView(
                text = stringResource(id = R.string.no_places_added),
                icon = painterResource(id = R.drawable.ic_no_place_on_map)
            )
        }

    }
}

