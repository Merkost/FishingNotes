package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
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
    val imgSize: Dp = remember { 120.dp }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ProfileAppBar(navController) },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        ConstraintLayout(
            modifier = modifier.fillMaxSize()
        ) {
            val (image, card, editButton) = createRefs()

            val placesState by viewModel.currentPlaces.collectAsState()
            val catchesState by viewModel.currentCatches.collectAsState()

            val imageBorderStroke = remember {
                BorderStroke(
                    2.dp,
                    Brush.linearGradient(colors = listOf(Color(0xFFED2939), Color(0xFFFFFF66)))
                )
            }

            UserImage(modifier = Modifier
                .constrainAs(image) {
                    centerHorizontallyTo(parent)
                    centerAround(card.top)
                    width = Dimension.fillToConstraints
                }
                .zIndex(2f),

                // TODO: photoUrl
                photoUrl = "",
                imgSize = imgSize,
                icon = Icons.Default.Edit,
                borderStroke = imageBorderStroke) {
                navController.navigate(MainDestinations.EDIT_PROFILE)
            }

            Card(
                modifier = Modifier
                    .constrainAs(card) {
                        top.linkTo(parent.top, imgSize)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }
                    .fillMaxSize()
                    .zIndex(1f),
                shape = AbsoluteRoundedCornerShape(25.dp, 25.dp),
                elevation = 8.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val favoritePlace by viewModel.favoritePlace.collectAsState()

                    val (name, places, catches, registerDate, favorite) = createRefs()
                    val verticalCenterGl = createGuidelineFromAbsoluteLeft(0.5f)

                    UserText(user, modifier = Modifier
                        .constrainAs(name) {
                            top.linkTo(parent.top, (imgSize / 2 + 8.dp))
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        }
                        .zIndex(2f))

                    /*SecondaryText(
                        modifier = Modifier
                            .constrainAs(registerDate) {
                                top.linkTo(name.bottom, 2.dp)
                                absoluteLeft.linkTo(name.absoluteLeft)
                                absoluteRight.linkTo(name.absoluteRight)
                            }
                            .zIndex(2f),
                        text = "${stringResource(id = R.string.register_date)}: " +
                                (user.dateTimeRegistered.format())
                    )
*/
                    FavoritePlaceView(
                        modifier = Modifier.constrainAs(favorite) {
                            top.linkTo(registerDate.bottom, 32.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                        },
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

                    PlacesNumber(
                        modifier = Modifier
                            .constrainAs(places) {
                                top.linkTo(parent.top, 16.dp)
                                absoluteLeft.linkTo(verticalCenterGl, imgSize / 2)
                                absoluteRight.linkTo(parent.absoluteRight)
                            }
                            .zIndex(3f),
                        userPlacesNum = placesState?.size
                    )

                    CatchesNumber(
                        modifier = Modifier
                            .constrainAs(catches) {
                                top.linkTo(parent.top, 16.dp)
                                absoluteLeft.linkTo(parent.absoluteLeft)
                                absoluteRight.linkTo(verticalCenterGl, imgSize / 2)
                            }
                            .zIndex(3f),
                        userCatchesNum = catchesState?.size
                    )
                }
            }
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
fun UserText(user: UserData, modifier: Modifier) {
    Text(
        modifier = modifier,
        text = user.login,
        style = MaterialTheme.typography.h6,
        textAlign = TextAlign.Center
    )
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

