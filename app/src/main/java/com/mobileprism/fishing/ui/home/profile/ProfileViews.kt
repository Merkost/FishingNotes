package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
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
import com.airbnb.lottie.compose.*
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.custom.DefaultDialog
import com.mobileprism.fishing.ui.home.notes.CatchItemView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.theme.primaryTextColor
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.compose.getViewModel

@ExperimentalCoilApi
@Composable
fun UserImage(
    modifier: Modifier = Modifier,
    photoUrl: String,
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
                    elevation = 12.dp
                ) {
                    IconButton(modifier = Modifier, onClick = onIconClick) {
                        Icon(icon, icon.name)
                    }
                }
            }

            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = stringResource(id = R.string.user_photo),
                contentScale = ContentScale.Crop,
                error = {
                    Image(
                        painter = painterResource(R.drawable.no_photo),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(id = R.string.fisher)
                    )
                },
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                modifier = Modifier
                    .size(imgSize)
                    .clip(shape)
                    .border(borderStroke ?: BorderStroke(0.dp, Color.White.copy(0f)), shape)
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
                navController.navigate(MainDestinations.SETTINGS)
            }) {
                Icon(Icons.Default.Settings, stringResource(R.string.settings))
            }
        },
        elevation = 0.dp,
    )
    if (dialogOnLogout.value) LogoutDialog(dialogOnLogout, navController)
}

@OptIn(ExperimentalComposeUiApi::class)
@InternalCoroutinesApi
@Composable
fun LogoutDialog(dialogOnLogout: MutableState<Boolean>, navController: NavController) {
    val scope = rememberCoroutineScope()
    val viewModel = getViewModel<UserViewModel>()
    val context = LocalContext.current

    DefaultDialog(
        primaryText = stringResource(R.string.logout_dialog_title),
        secondaryText = stringResource(R.string.logout_dialog_message),
        negativeButtonText = stringResource(id = R.string.no),
        onNegativeClick = { dialogOnLogout.value = false },
        positiveButtonText = stringResource(id = R.string.yes),
        onPositiveClick = {
            viewModel.logoutCurrentUser()
            navController.navigate(MainDestinations.AUTH_ROUTE) {
                popUpTo(MainDestinations.HOME_ROUTE) {
                    inclusive = true
                }
            }
            /*scope.launch {
                dialogOnLogout.value = false*/
                /*viewModel.logoutCurrentUser()*//*.collect { isLogout ->
                    if (isLogout) {
                        dialogOnLogout.value = false

                        unloadKoinModules(repositoryModuleLocal)
                        unloadKoinModules(repositoryModuleFirebase)
                    }
                }
            }*/
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 8.dp, bottom = 4.dp),
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
                    tint = primaryTextColor
                )

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryText(
                        modifier = Modifier,
                        text = title
                    )

                    SecondaryTextSmall(
                        modifier = Modifier,
                        text = subtitle
                    )
                }


            }
        }
    }

}

@ExperimentalMaterialApi
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

@ExperimentalMaterialApi
@ExperimentalAnimationApi
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
            subtitle = favoritePlace?.dateOfCreation?.toDateTextMonth()
                ?: stringResource(R.string.not_avalable),
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

