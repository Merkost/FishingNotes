package com.joesemper.fishing.compose.ui.home

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.weather.*
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.joesemper.fishing.utils.time.toTime
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@ExperimentalAnimationApi
@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch?) {
    val viewModel = getViewModel<UserCatchViewModel>()
    catch?.let {
        viewModel.catch.value = it
    }

    Scaffold(topBar = {
        CatchTopBar(
            navController = navController,
            viewModel = viewModel
        )
    }) {
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
    viewModel.catch.value?.let { catch ->
        var mapMarker by remember {
            mutableStateOf<UserMapMarker?>(null)
        }

        viewModel.getMapMarker(catch.userMarkerId)
            .collectAsState(initial = UserMapMarker()).value?.let {
                mapMarker = it
            }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            CatchTitleView(catch = catch)
            CatchPhotosView(catch = catch)
            CatchPlaceView(place = mapMarker)
            CatchNoteView(catch = catch)
            WayOfFishingView(catch = catch)
            CatchWeatherView(catch = catch)
        }
    }
}

@Composable
fun CatchTitleView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {

        val (title, amount, weight, weightUnit) = createRefs()

        createHorizontalChain(title, weight, weightUnit, chainStyle = ChainStyle.Packed)

        HeaderText(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                },
            text = catch.fishType
        )

        SecondaryText(
            modifier = Modifier.constrainAs(amount) {
                top.linkTo(title.bottom, 2.dp)
                absoluteLeft.linkTo(title.absoluteLeft)
                absoluteRight.linkTo(weightUnit.absoluteRight)
            },
            text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount} ${stringResource(id = R.string.pc)}"
        )

        HeaderTextSecondary(
            modifier = Modifier.constrainAs(weightUnit) {
                top.linkTo(parent.top)
            },
            text = stringResource(id = R.string.kg)
        )

        HeaderText(
            modifier = Modifier.constrainAs(weight) {
                top.linkTo(weightUnit.top)
                absoluteRight.linkTo(weightUnit.absoluteLeft, 2.dp)
            },
            text = catch.fishWeight.toString()
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun CatchPhotosView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    val context = LocalContext.current
    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SubtitleWithIcon(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            icon = R.drawable.ic_baseline_image_24,
            text = stringResource(id = R.string.photos)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (catch.downloadPhotoLinks.isNotEmpty()) {
                catch.downloadPhotoLinks.forEach {
                    ItemCatchPhotoView(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        photo = it.toUri()
                    )
                }
            } else {
                SecondaryTextSmall(text = stringResource(id = R.string.no_photo_selected))
            }
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

@Composable
fun CatchNoteView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    DefaultCard(modifier = modifier.padding(horizontal = 8.dp)) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtitleWithIcon(
                modifier = Modifier,
                icon = R.drawable.ic_baseline_sticky_note_2_24,
                text = stringResource(id = R.string.note)
            )

            if (catch.description.isNotBlank()) {
                PrimaryText(text = catch.description)
            } else {
                SecondaryText(text = stringResource(id = R.string.no_description))
            }
        }
    }
}

@Composable
fun CatchPlaceView(
    modifier: Modifier = Modifier,
    place: UserMapMarker?
) {
    place?.let {
        DefaultCard(modifier = modifier.padding(horizontal = 8.dp)) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {

                val (navigationIcon, placeTitle, placeIcon, description) = createRefs()

                Icon(
                    modifier = Modifier.constrainAs(placeIcon) {
                        top.linkTo(placeTitle.top)
                        bottom.linkTo(description.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    },
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = stringResource(id = R.string.location),
                    tint = Color(place.markerColor)
                )

                PrimaryText(
                    modifier = Modifier.constrainAs(placeTitle) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(placeIcon.absoluteRight, 8.dp)
                    },
                    text = place.title
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(placeTitle.bottom, 2.dp)
                        absoluteLeft.linkTo(placeTitle.absoluteLeft)
                    },
                    text = if (place.description.isBlank()) {
                        stringResource(id = R.string.no_description)
                    } else {
                        place.description
                    }

                )

                IconButton(
                    modifier = Modifier.constrainAs(navigationIcon) {
                        top.linkTo(placeTitle.top)
                        bottom.linkTo(description.bottom)
                        absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    },
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_map_24),
                        contentDescription = stringResource(id = R.string.map),
                        tint = secondaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    DefaultCard(modifier = modifier.padding(horizontal = 8.dp)) {
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
                    absoluteLeft.linkTo(parent.absoluteLeft)
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
                    top.linkTo(rodTitle.bottom, 2.dp)
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
                    top.linkTo(baitTitle.bottom, 2.dp)
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
                    top.linkTo(lureTile.bottom, 2.dp)
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
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg.name)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C.name)

    DefaultCard(
        modifier = modifier.padding(horizontal = 8.dp)
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
                    absoluteLeft.linkTo(parent.absoluteLeft)
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
                    ) + getTemperatureFromUnit(temperatureUnit)
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
                text = getPressure(
                    catch.weatherPressure,
                    PressureValues.valueOf(pressureUnit)
                ) + " " + pressureUnit,
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





