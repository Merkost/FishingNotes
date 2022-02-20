package com.mobileprism.fishing.compose.ui.home.profile

import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.local.SQLitePersistence.clearPersistence
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.MainDestinations
import com.mobileprism.fishing.compose.ui.home.notes.CatchItemView
import com.mobileprism.fishing.compose.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.compose.ui.home.views.*
import com.mobileprism.fishing.compose.ui.theme.customColors
import com.mobileprism.fishing.compose.ui.theme.primaryTextColor
import com.mobileprism.fishing.domain.UserViewModel
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
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

