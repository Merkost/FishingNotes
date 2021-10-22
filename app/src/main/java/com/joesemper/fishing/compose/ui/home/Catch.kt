package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.ItemPhoto
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.mappers.getMoonIconByPhase
import com.joesemper.fishing.model.mappers.getWeatherIconByName
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.utils.getDateAndTimeByMilliseconds
import com.joesemper.fishing.utils.hPaToMmHg
import org.koin.androidx.compose.getViewModel


@ExperimentalAnimationApi
@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch?) {
    val viewModel = getViewModel<UserCatchViewModel>()
    catch?.let {
        viewModel.catch.value = it
    }

    Scaffold(topBar = {
        CatchTopBar(navController, viewModel)
    }) {
        CatchContent(navController, viewModel = viewModel)
    }
}

@Composable
fun CatchTopBar(navController: NavController, viewModel: UserCatchViewModel) {
    DefaultAppBar(
        title = stringResource(id = R.string.user_catch),
        onNavClick = { navController.popBackStack() }
    ) {
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = "", tint = Color.White)
        }
        IconButton(modifier = Modifier.padding(horizontal = 4.dp),
            onClick = {
                viewModel.deleteCatch()
                navController.popBackStack()
            }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun CatchContent(navController: NavController, viewModel: UserCatchViewModel) {
    viewModel.catch.value?.let { catch ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            var mapMarker by remember {
                mutableStateOf<UserMapMarker?>(null)
            }

            viewModel.getMapMarker(catch.userMarkerId)
                .collectAsState(initial = UserMapMarker()).value?.let {
                    mapMarker = it
                }

            ConstraintLayout(
                modifier = Modifier.padding(8.dp),
            ) {

                val (title, date, fish, photos, note, place, fishingWay, rod, bait, lure) = createRefs()

                SubtitleWithIcon(
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    icon = R.drawable.ic_hook,
                    text = stringResource(R.string.main)
                )

                SecondaryTextColored(
                    modifier = Modifier.constrainAs(date) {
                        top.linkTo(parent.top)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    text = getDateAndTimeByMilliseconds(catch.date)
                )

                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(fish) {
                        top.linkTo(title.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = catch.fishType,
                    label = stringResource(id = R.string.fish_species),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fish),
                            contentDescription = ""
                        )
                    },
                    trailingIcon = {
                        Row(modifier = Modifier.padding(end = 2.dp)) {
                            PrimaryText(text = catch.fishWeight.toString())
                            SecondaryText(text = stringResource(id = R.string.kg))
                        }

                    },
                    helperText = catch.fishAmount.toString() + stringResource(id = R.string.pc)
                )

                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(note) {
                        top.linkTo(fish.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.description.isBlank()) {
                        stringResource(id = R.string.no_description)
                    } else {
                        catch.description
                    },
                    label = stringResource(id = R.string.note)
                )

                SimpleUnderlineTextField(modifier = Modifier.constrainAs(place) {
                    top.linkTo(photos.bottom, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
                    text = catch.placeTitle,
                    label = stringResource(id = R.string.place),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                            tint = secondaryFigmaColor,
                            contentDescription = ""
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { /*TODO*/ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_chevron_right_24),
                                tint = secondaryFigmaTextColor,
                                contentDescription = ""
                            )
                        }
                    })

                if (catch.downloadPhotoLinks.isNotEmpty()) {
                    LazyRow(modifier = Modifier.constrainAs(photos) {
                        top.linkTo(note.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    }) {
                        items(items = catch.downloadPhotoLinks) {
                            ItemPhoto(
                                photo = it.toUri(),
                                clickedPhoto = {},
                                deletedPhoto = {},
                                deleteEnabled = false
                            )
                        }

                    }
                } else {
                    Spacer(modifier = Modifier
                        .padding(1.dp)
                        .constrainAs(photos) {
                            top.linkTo(note.bottom, 16.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                        })
                }

                SubtitleWithIcon(
                    modifier = Modifier.constrainAs(fishingWay) {
                        top.linkTo(place.bottom, 32.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    icon = R.drawable.ic_fishing_rod,
                    text = stringResource(id = R.string.way_of_fishing)
                )

                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(rod) {
                        top.linkTo(fishingWay.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingRodType.isNotBlank()) {
                        catch.fishingRodType
                    } else {
                        stringResource(id = R.string.no_rod)
                    },
                    label = stringResource(id = R.string.fish_rod)
                )

                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(bait) {
                        top.linkTo(rod.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingBait.isNotBlank()) {
                        catch.fishingBait
                    } else {
                        stringResource(id = R.string.no_bait)
                    },
                    label = stringResource(id = R.string.bait)
                )

                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(lure) {
                        top.linkTo(bait.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = if (catch.fishingLure.isNotBlank()) {
                        catch.fishingLure
                    } else {
                        stringResource(id = R.string.no_lure)
                    },
                    label = stringResource(id = R.string.lure)
                )
            }

            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 32.dp,
                    end = 8.dp,
                    bottom = 16.dp
                )
            ) {
                SubtitleWithIcon(
                    icon = R.drawable.weather_sunny,
                    modifier = Modifier,
                    text = stringResource(id = R.string.weather)
                )
                Spacer(Modifier.size(8.dp))
                SimpleUnderlineTextField(modifier = Modifier,
                    text = catch.weatherPrimary,
                    label = stringResource(id = R.string.description),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(getWeatherIconByName(catch.weatherIcon)),
                            contentDescription = ""
                        )
                    }
                )

                Spacer(Modifier.size(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SimpleUnderlineTextField(modifier = Modifier.weight(1f, true),
                        text = catch.weatherTemperature.toString(),
                        label = stringResource(id = R.string.temperature),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_thermometer),
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            SecondaryText(text = stringResource(id = R.string.celsius))
                        }
                    )

                    Spacer(modifier = Modifier.padding(4.dp))

                    SimpleUnderlineTextField(modifier = Modifier.weight(1f, true),
                        text = hPaToMmHg(catch.weatherPressure).toString(),
                        label = stringResource(id = R.string.pressure),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_gauge),
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            SecondaryText(text = stringResource(id = R.string.pressure_units) + " ")
                        }
                    )
                }

                Spacer(Modifier.size(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SimpleUnderlineTextField(modifier = Modifier.weight(1f, true),
                        text = catch.weatherWindSpeed.toString(),
                        label = stringResource(id = R.string.wind),
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.rotate(catch.weatherWindDeg.toFloat()),
                                painter = painterResource(id = R.drawable.ic_arrow_up),
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            SecondaryText(text = stringResource(id = R.string.wind_speed_units))
                        }
                    )

                    Spacer(modifier = Modifier.padding(4.dp))

                    SimpleUnderlineTextField(modifier = Modifier.weight(1f, true),
                        text = (catch.weatherMoonPhase * 100).toInt().toString(),
                        label = stringResource(id = R.string.moon_phase),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = getMoonIconByPhase(catch.weatherMoonPhase)),
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            SecondaryText(text = stringResource(id = R.string.percent))
                        }
                    )
                }
            }

        }
    }
}
