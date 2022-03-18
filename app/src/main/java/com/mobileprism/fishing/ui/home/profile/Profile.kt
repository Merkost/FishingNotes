package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun Profile(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel = getViewModel<UserViewModel>()

    val user by viewModel.currentUser.collectAsState()

    val imgSize: Dp = 120.dp
    val bgHeight: Dp = 180.dp

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = { ProfileAppBar(navController) }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val (background, card, image, name, places, catches, registerDate) = createRefs()

            val bgGl = createGuidelineFromTop(80.dp)
            val verticalCenterGl = createGuidelineFromAbsoluteLeft(0.5f)

            val placesState by viewModel.currentPlaces.collectAsState()
            val catchesState by viewModel.currentCatches.collectAsState()

            Surface(//shape = RoundedCornerShape(0.dp,0.dp,15.dp,15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bgHeight)
                    .constrainAs(background) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                color = MaterialTheme.colors.primary
            ) {}

            UserImage(user, imgSize, modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    bottom.linkTo(parent.bottom)
                    centerAround(bgGl)
                }
                .zIndex(2f))

            UserText(user, modifier = Modifier
                .constrainAs(name) {
                    top.linkTo(image.bottom)
                    absoluteLeft.linkTo(card.absoluteLeft)
                    absoluteRight.linkTo(card.absoluteRight)
                }
                .zIndex(2f))

            SecondaryText(
                modifier = Modifier
                    .constrainAs(registerDate) {
                        top.linkTo(name.bottom, 2.dp)
                        absoluteLeft.linkTo(name.absoluteLeft)
                        absoluteRight.linkTo(name.absoluteRight)
                    }
                    .zIndex(2f),
                text = "${stringResource(id = R.string.register_date)}: " +
                        (user?.registerDate?.toDateTextMonth() ?: "")
            )

            Card(
                modifier = Modifier
                    .constrainAs(card) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .zIndex(1f),
                shape = AbsoluteRoundedCornerShape(25.dp, 25.dp),
                elevation = 10.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Divider()
                Column(
                    modifier = Modifier
                        .padding(top = 120.dp, bottom = 120.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    //val bestCatch by viewModel.bestCatch.collectAsState()
                    val favoritePlace by viewModel.favoritePlace.collectAsState()

                    /*BestCatchView(
                        bestCatch = bestCatch,
                        onCatchItemClick = {
                            navController.navigate(
                                MainDestinations.CATCH_ROUTE,
                                Arguments.CATCH to it
                            )
                        }
                    )*/

                    //Spacer(modifier = Modifier.size(16.dp))

                    FavoritePlaceView(
                        favoritePlace = favoritePlace,
                        userPlaceClicked = {
                            navController.navigate(
                                MainDestinations.PLACE_ROUTE,
                                Arguments.PLACE to it
                            )
                        },
                        navigateToMap = {
                            navController.navigate(
                                "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                                Arguments.PLACE to it
                            )
                        }
                    )
                }
            }

            PlacesNumber(
                modifier = Modifier
                    .constrainAs(places) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(verticalCenterGl, imgSize / 2)
                        absoluteRight.linkTo(parent.absoluteRight)
                        bottom.linkTo(image.bottom)
                    }
                    .zIndex(3f),
                userPlacesNum = placesState?.size
            )

            CatchesNumber(
                modifier = Modifier
                    .constrainAs(catches) {
                        top.linkTo(bgGl)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(verticalCenterGl, imgSize / 2)
                        bottom.linkTo(image.bottom)
                    }
                    .zIndex(3f),
                userCatchesNum = catchesState?.size
            )
        }

    }
}

@Composable
fun NoPlacesStats() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        SecondaryText(text = "Добавьте места и уловы чтобы увидеть статистику!")
        LottieMyStats(modifier = Modifier.fillMaxWidth())
    }

}

@Composable
fun LottieMyStats(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.stats))
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
fun UserText(user: User?, modifier: Modifier) {
    user?.let {
        Text(
            modifier = modifier,
            text = when (user.displayName.isEmpty()) {
                true -> stringResource(R.string.anonymous)
                false -> user.displayName
            }, style = MaterialTheme.typography.h6,
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
            Icons.Default.Place, stringResource(R.string.place),
            modifier = Modifier
                .size(25.dp)
                .placeholder(
                    visible = userPlacesNum == null,
                    color = Color.LightGray,
                    // optional, defaults to RectangleShape
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = Color.White,
                    )
                ),
        )
        Text(
            text = userPlacesNum?.toString() ?: "",
            /*modifier = Modifier
                .placeholder(
                    visible = userPlacesNum == null,
                    color = Color.LightGray,
                    // optional, defaults to RectangleShape
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = Color.White,
                    )
                )*/
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
            painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
            modifier = Modifier
                .size(25.dp)
                .placeholder(
                    visible = userCatchesNum == null,
                    color = Color.LightGray,
                    // optional, defaults to RectangleShape
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = Color.White,
                    )
                )
        )
        Text(
            text = userCatchesNum?.toString() ?: "",
            /*modifier = Modifier.placeholder(
                visible = userCatchesNum == null,
                color = Color.LightGray,
                // optional, defaults to RectangleShape
                shape = CircleShape,
                highlight = PlaceholderHighlight.shimmer(
                    highlightColor = Color.White,
                )
            )*/
        )
    }
}


/*@ExperimentalAnimationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@InternalCoroutinesApi
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun ProfilePreview() {
    //FigmaTheme {
    Profile(rememberNavController())
    //}
}*/

