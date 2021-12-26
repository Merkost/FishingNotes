package com.joesemper.fishing.compose.ui.home.catch_screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.notes.ItemUserPlace
import com.joesemper.fishing.compose.ui.home.weather.*
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.Constants
import com.joesemper.fishing.utils.network.ConnectionState
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.joesemper.fishing.utils.time.toTime
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalAnimationApi
@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch?) {
    val viewModel = getViewModel<UserCatchViewModel>()

    LaunchedEffect(key1 = catch) {
        catch?.let {
            viewModel.catch.value = it
        }
    }

    BottomSheetScaffold(
        topBar = {
            CatchTopBar(
                navController = navController,
                viewModel = viewModel
            )
        },
        sheetContent = {
            BannerAdvertView(adId = stringResource(R.string.catch_admob_banner_id))
        },
        sheetShape = RectangleShape,
        sheetGesturesEnabled = false,
        sheetPeekHeight = 0.dp
    ) {
        CatchContent(
            navController = navController,
            viewModel = viewModel,
        )
    }
}

@Composable
fun CatchTopBar(navController: NavController, viewModel: UserCatchViewModel) {
    DefaultAppBar(
        title = stringResource(id = R.string.user_catch),
        subtitle = viewModel.catch.value?.let { it.date.toDateTextMonth() + " " + it.date.toTime() },
        onNavClick = { navController.popBackStack() }
    ) {
        IconButton(modifier = Modifier.padding(horizontal = 4.dp),
            onClick = {
                viewModel.deleteCatch()
                navController.popBackStack()
            }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@Composable
fun CatchContent(
    navController: NavController,
    viewModel: UserCatchViewModel,
) {

    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    viewModel.catch.value?.let { catch ->

        LaunchedEffect(key1 = catch) {
            viewModel.getMapMarker(catch.userMarkerId)
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            CatchTitleView(catch = catch, viewModel = viewModel)

            if (catch.downloadPhotoLinks.isNotEmpty() || connectionState is ConnectionState.Available) {
                CatchPhotosView(photos = catch.downloadPhotoLinks)
            }

            viewModel.mapMarker.value?.let {
                ItemUserPlace(
                    place = it,
                    userPlaceClicked = { })
            }

            DefaultNoteView(
                note = catch.description,
                onSaveNoteChange = { newNote ->
                    viewModel.updateCatch(data = mapOf("description" to newNote))
                })

            WayOfFishingView(catch = catch, viewModel = viewModel)

            CatchWeatherView(catch = catch)

            Spacer(modifier = Modifier.size(Constants.bottomBannerPadding))
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun CatchTitleView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    viewModel: UserCatchViewModel
) {

    val dialogState = remember { mutableStateOf(false) }

    if (dialogState.value) {
        FishTypeAmountAndWeightDialog(
            catch = catch,
            dialogState = dialogState,
            viewModel = viewModel
        )
    }

    DefaultCardClickable(
        modifier = modifier,
        onClick = { dialogState.value = true }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            val (title, amount, weight) = createRefs()

            HeaderText(
                modifier = Modifier.constrainAs(title) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    absoluteRight.linkTo(weight.absoluteLeft, 8.dp)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                },
                text = catch.fishType
            )

            HeaderText(
                modifier = Modifier.constrainAs(weight) {
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    top.linkTo(parent.top)
                },
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            SecondaryText(
                modifier = Modifier.constrainAs(amount) {
                    top.linkTo(title.bottom, 2.dp)
                    absoluteLeft.linkTo(title.absoluteLeft)
                },
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount} " +
                        stringResource(id = R.string.pc)
            )
        }
    }

}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun CatchPhotosView(
    modifier: Modifier = Modifier,
    photos: List<String>
) {
    Row(
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        photos.forEach {
            ItemCatchPhotoView(
                modifier = Modifier.padding(horizontal = 8.dp),
                photo = it.toUri()
            )
        }
    }

}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ItemCatchPhotoView(
    modifier: Modifier = Modifier,
    photo: Uri
) {
    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    AsyncImage(
        model = photo,
        contentDescription = null,
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable { fullScreenPhoto.value = photo },
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Low
    ) { state ->
        if (state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            AsyncImageContent()
        }
    }

    AnimatedVisibility(fullScreenPhoto.value != null) {
        FullScreenPhoto(fullScreenPhoto)
    }
}

@ExperimentalComposeUiApi
@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    viewModel: UserCatchViewModel
) {

    val dialogState = remember { mutableStateOf(false) }

    if (dialogState.value) {
        EditWayOfFishingDialog(
            catch = catch,
            dialogState = dialogState,
            viewModel = viewModel
        )
    }

    DefaultCardClickable(
        modifier = modifier,
        onClick = { dialogState.value = true }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {

            val (subtitle, rodTitle, rodValue, baitTitle, baitValue, lureTile, lureValue) = createRefs()

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
                icon = R.drawable.ic_hook,
                text = stringResource(id = R.string.way_of_fishing)
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(rodTitle) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 12.dp)
                    top.linkTo(subtitle.bottom, 12.dp)
                },
                text = stringResource(id = R.string.fish_rod)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(rodValue) {
                    absoluteLeft.linkTo(rodTitle.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight, 12.dp)
                    top.linkTo(rodTitle.bottom, 2.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingRodType.isNotBlank()) {
                    catch.fishingRodType
                } else {
                    stringResource(id = R.string.no_rod)
                }
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(baitTitle) {
                    absoluteLeft.linkTo(rodTitle.absoluteLeft)
                    top.linkTo(rodValue.bottom, 12.dp)
                },
                text = stringResource(id = R.string.bait)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(baitValue) {
                    absoluteLeft.linkTo(rodTitle.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight, 12.dp)
                    top.linkTo(baitTitle.bottom, 2.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingBait.isNotBlank()) {
                    catch.fishingBait
                } else {
                    stringResource(id = R.string.no_bait)
                }
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(lureTile) {
                    absoluteLeft.linkTo(rodTitle.absoluteLeft)
                    top.linkTo(baitValue.bottom, 12.dp)
                },
                text = stringResource(id = R.string.lure)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(lureValue) {
                    absoluteLeft.linkTo(rodTitle.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight, 12.dp)
                    top.linkTo(lureTile.bottom, 2.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingLure.isNotBlank()) {
                    catch.fishingLure
                } else {
                    stringResource(id = R.string.no_lure)
                }
            )
        }
    }
}

@Composable
fun CatchWeatherView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {

    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C.name)

    DefaultCard(
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (subtitle, pressText, windText, pressIcon, pressValue, windIcon, windValue, windDeg,
                primary, description, moonTile, moonIcon, moonValue) = createRefs()

            val guideline = createGuidelineFromAbsoluteLeft(0.5f)

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
                icon = R.drawable.weather_sunny,
                text = stringResource(id = R.string.weather)
            )

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(primary) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(guideline)
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 2.dp),
                    painter = painterResource(id = getWeatherIconByName(catch.weatherIcon)),
                    contentDescription = stringResource(id = R.string.weather)
                )

                HeaderText(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    text = getTemperature(
                        catch.weatherTemperature,
                        TemperatureValues.valueOf(temperatureUnit)
                    ) + getTemperatureNameFromUnit(temperatureUnit)
                )
            }

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(primary.bottom, 4.dp)
                    absoluteLeft.linkTo(primary.absoluteLeft)
                    absoluteRight.linkTo((primary.absoluteRight))
                },
                text = catch.weatherPrimary.replaceFirstChar { it.uppercase() }
            )

            SecondaryText(
                modifier = Modifier.constrainAs(moonTile) {
                    top.linkTo(primary.top)
                    absoluteLeft.linkTo(guideline)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                text = stringResource(id = R.string.moon_phase)
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(moonIcon) {
                        top.linkTo(moonTile.bottom, 4.dp)
                        absoluteLeft.linkTo(moonTile.absoluteLeft)
                        absoluteRight.linkTo(moonValue.absoluteLeft)
                    },
                painter = painterResource(id = getMoonIconByPhase(catch.weatherMoonPhase)),
                contentDescription = stringResource(id = R.string.moon_phase)
            )

            PrimaryText(
                modifier.constrainAs(moonValue) {
                    top.linkTo(moonIcon.top)
                    bottom.linkTo(moonIcon.bottom)
                    absoluteRight.linkTo(moonTile.absoluteRight)
                },
                text = (catch.weatherMoonPhase * 100).toInt().toString()
                        + " " + stringResource(id = R.string.percent)
            )

            SecondaryText(
                modifier = Modifier.constrainAs(pressText) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(guideline)
                    top.linkTo(description.bottom, 16.dp)
                },
                text = stringResource(id = R.string.pressure)
            )

            SecondaryText(
                modifier = Modifier.constrainAs(windText) {
                    absoluteLeft.linkTo(guideline)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(pressText.top)
                },
                text = stringResource(id = R.string.wind)
            )

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(pressIcon) {
                        top.linkTo(pressText.bottom, 4.dp)
                        absoluteLeft.linkTo(pressText.absoluteLeft)
                        absoluteRight.linkTo(pressValue.absoluteLeft, 2.dp)
                    },
                painter = painterResource(id = R.drawable.ic_gauge),
                contentDescription = stringResource(id = R.string.pressure),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primaryVariant)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(pressValue) {
                    top.linkTo(pressIcon.top)
                    bottom.linkTo(pressIcon.bottom)
                    absoluteLeft.linkTo(pressIcon.absoluteRight, 2.dp)
                    absoluteRight.linkTo(pressText.absoluteRight)
                },
                text = pressureUnit.getPressure(
                    catch.weatherPressure) + " " + pressureUnit.name,
            )

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(windIcon) {
                        top.linkTo(pressText.bottom, 4.dp)
                        absoluteLeft.linkTo(windText.absoluteLeft)
                        absoluteRight.linkTo(windValue.absoluteLeft, 2.dp)
                    },
                painter = painterResource(id = R.drawable.ic_wind),
                contentDescription = stringResource(id = R.string.wind),
            )

            PrimaryText(
                modifier = Modifier.constrainAs(windValue) {
                    top.linkTo(windIcon.top)
                    bottom.linkTo(windIcon.bottom)
                    absoluteLeft.linkTo(windIcon.absoluteRight, 2.dp)
                    absoluteRight.linkTo(windDeg.absoluteLeft, 2.dp)
                },
                text = catch.weatherWindSpeed.toInt()
                    .toString() + " " + stringResource(id = R.string.wind_speed_units)
            )

            Icon(
                modifier = Modifier
                    .constrainAs(windDeg) {
                        top.linkTo(windIcon.top)
                        bottom.linkTo(windIcon.bottom)
                        absoluteLeft.linkTo(windValue.absoluteRight, 4.dp)
                        absoluteRight.linkTo(windText.absoluteRight)
                    }
                    .rotate(catch.weatherWindDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                contentDescription = stringResource(id = R.string.wind),
            )
        }
    }
}





