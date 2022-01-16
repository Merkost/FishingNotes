package com.joesemper.fishing.compose.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.notes.CatchItemView
import com.joesemper.fishing.compose.ui.home.notes.ItemUserPlace
import com.joesemper.fishing.compose.ui.home.views.*
import com.joesemper.fishing.compose.ui.theme.primaryTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.domain.UserViewModel
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalCoilApi
@Composable
fun UserImage(user: User?, imgSize: Dp, modifier: Modifier = Modifier) {
    val linearGradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFED2939), Color(0xFFFFFF66))
    )
    user?.let {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            CoilImage(
                imageModel = if (user.photoUrl.isNullOrEmpty())
                    painterResource(R.drawable.ic_fisher) else user.photoUrl,
                contentScale = ContentScale.Crop,
                shimmerParams = ShimmerParams(
                    baseColor = Color.LightGray,
                    highlightColor = Color.White,
                    durationMillis = 650,
                    dropOff = 0.65f,
                    tilt = 20f,
                ),
                failure = {
                    Text("Image request failed")
                },
                modifier = Modifier
                    .size(imgSize)
                    .clip(CircleShape)
                    .border(2.dp, linearGradientBrush, CircleShape)

            )

        }
    }

}

@OptIn(InternalCoroutinesApi::class)
@Composable
fun ProfileAppBar(navController: NavController, viewModel: UserViewModel) {
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
            IconButton(onClick = { navController.navigate(MainDestinations.SETTINGS) }) {
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

    DefaultDialog(
        primaryText = stringResource(R.string.logout_dialog_title),
        secondaryText = stringResource(R.string.logout_dialog_message),
        negativeButtonText = stringResource(id = R.string.No),
        onNegativeClick = { dialogOnLogout.value = false },
        positiveButtonText = stringResource(id = R.string.Yes),
        onPositiveClick = {
            scope.launch {
                viewModel.logoutCurrentUser().collect { isLogout ->
                    if (isLogout) {
                        dialogOnLogout.value = false
                        navController.navigate(MainDestinations.LOGIN_ROUTE) {
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
fun CatchesCountView(
    modifier: Modifier = Modifier,
    catchesCount: Int
) {
    ConstraintLayout(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        val (icon, text, count) = createRefs()
        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
            painter = painterResource(id = R.drawable.ic_fishing),
            contentDescription = null,
            tint = secondaryTextColor
        )

        SecondaryText(
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(icon.absoluteRight, 16.dp)
            },
            text = stringResource(R.string.catches_count)
        )

        PrimaryText(
            modifier = Modifier.constrainAs(count) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            text = catchesCount.toString()
        )
    }
}

@Composable
fun PlacesCountView(
    modifier: Modifier = Modifier,
    placesCount: Int
) {
    ConstraintLayout(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    ) {
        val (icon, text, count) = createRefs()
        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
            contentDescription = null,
            tint = secondaryTextColor
        )

        SecondaryText(
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(icon.absoluteRight, 16.dp)
            },
            text = stringResource(R.string.places_count)
        )

        PrimaryText(
            modifier = Modifier.constrainAs(count) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
            },
            text = placesCount.toString()
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun BestCatchView(
    modifier: Modifier = Modifier,
    bestCatch: UserCatch?
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (title, icon, subtitle, bestCatchItem) = createRefs()

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(icon) {
                    top.linkTo(title.top)
                    bottom.linkTo(subtitle.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
            painter = painterResource(id = R.drawable.ic_fish),
            contentDescription = null,
            tint = primaryTextColor
        )

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(icon.absoluteRight, 16.dp)
            },
            text = stringResource(R.string.best_catch)
        )

        if (bestCatch != null) {
            SecondaryTextSmall(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(title.bottom)
                    absoluteLeft.linkTo(title.absoluteLeft)
                },
                text = bestCatch.date.toDateTextMonth()
            )

            CatchItemView(
                modifier = Modifier.constrainAs(bestCatchItem) {
                    top.linkTo(subtitle.bottom, 2.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    width = Dimension.fillToConstraints

                },
                catch = bestCatch,
                onClick = {}
            )
        }


    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun FavoritePlaceView(
    modifier: Modifier = Modifier,
    bestPlace: UserMapMarker?
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (title, icon, subtitle, bestCatchItem) = createRefs()

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(icon) {
                    top.linkTo(title.top)
                    bottom.linkTo(subtitle.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
            painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
            contentDescription = null,
            tint = primaryTextColor
        )

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(icon.absoluteRight, 16.dp)
            },
            text = stringResource(R.string.favorite_place)
        )

        if (bestPlace != null) {
            SecondaryTextSmall(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(title.bottom)
                    absoluteLeft.linkTo(title.absoluteLeft)
                },
                text = bestPlace.dateOfCreation.toDateTextMonth()
            )

            ItemUserPlace(
                modifier = Modifier.constrainAs(bestCatchItem) {
                    top.linkTo(subtitle.bottom, 2.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    width = Dimension.fillToConstraints

                },
                place = bestPlace,
                userPlaceClicked = { },
                navigateToMap = { }
            )
        }

    }
}

