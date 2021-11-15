package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.HeaderText
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryTextColored
import com.joesemper.fishing.compose.ui.home.SecondaryTextSmall
import com.joesemper.fishing.compose.ui.theme.*
import com.joesemper.fishing.model.entity.content.UserMapMarker

@Composable
fun WeatherParameterItem(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int,
    text: String,
    isExpanded: Boolean
) {
    Surface(
        modifier = if (isExpanded) {
            Modifier
                .height(100.dp)
                .width(100.dp)
        } else {
            Modifier
                .height(100.dp)
                .wrapContentWidth()
        },


        color = color,
        border = BorderStroke(0.1.dp, secondaryFigmaTextColor),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                tint = secondaryFigmaTextColor,
                painter = painterResource(id = icon),
                contentDescription = ""
            )
            if (isExpanded) {
                WeatherText(text = text)
            }
        }
    }
}


@Composable
fun WeatherParameterItemMeaning(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int? = null,
    text: String? = null,
    iconRotation: Int = 0,
) {
    Surface(
        color = color,
        border = BorderStroke(0.5.dp, secondaryFigmaTextColor),
        modifier = modifier.size(100.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(iconRotation.toFloat()),
                    tint = secondaryFigmaTextColor,
                    painter = painterResource(id = icon),
                    contentDescription = ""
                )
            }
            text?.let {
                WeatherText(text = text)
            }

        }
    }
}

@Composable
fun WeatherText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(vertical = 4.dp),
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
fun PrimaryWeatherParameterMeaning(modifier: Modifier = Modifier, icon: Int, text: String) {
    Column(
        modifier = modifier
            .height(100.dp)
            .width(150.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(56.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.weather),
            tint = Color.White
        )
        SecondaryTextColored(
            text = text,
            color = secondaryWhiteColor
        )
    }
}

@Composable
fun WeatherParameterMeaning(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    primaryIconId: Int,
    iconId: Int? = null,
    iconRotation: Int = 0,
    lightTint: Boolean = false
) {
    ConstraintLayout(
        modifier = modifier
            .height(50.dp)
            .width(120.dp),
    ) {
        val (icon, header, meaning, unit) = createRefs()
        Icon(
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
            painter = painterResource(id = primaryIconId),
            contentDescription = stringResource(id = R.string.temperature),
            tint = if (lightTint) secondaryWhiteColor else secondaryFigmaTextColor
        )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(header) {
                    top.linkTo(icon.top)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
            text = title,
            color = if (lightTint) secondaryWhiteColor else secondaryFigmaTextColor
        )
        PrimaryText(
            modifier = Modifier
                .constrainAs(meaning) {
                    absoluteLeft.linkTo(header.absoluteLeft)
                    bottom.linkTo(icon.bottom)
                },
            text = text,
            textColor = if (lightTint) primaryWhiteColor else primaryFigmaTextColor
        )
        iconId?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation.toFloat())
                    .constrainAs(unit) {
                        top.linkTo(meaning.top)
                        bottom.linkTo(meaning.bottom)
                        absoluteLeft.linkTo(meaning.absoluteRight, 4.dp)
                    },
                painter = painterResource(id = it),
                contentDescription = stringResource(id = R.string.temperature),
                tint = if (lightTint) primaryWhiteColor else primaryFigmaTextColor
            )
        }
    }
}

@Composable
fun WeatherTemperatureMeaning(
    modifier: Modifier = Modifier,
    temperature: String,
    minTemperature: String,
    maxTemperature: String
) {
    ConstraintLayout(
        modifier = modifier
            .height(70.dp)
            .width(100.dp),
    ) {
        val (main, param, min, max) = createRefs()
        HeaderText(
            modifier = Modifier
                .constrainAs(main) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            text = temperature,
            textColor = primaryWhiteColor
        )
        Icon(
            modifier = Modifier
                .size(48.dp)
                .constrainAs(param) {
                    top.linkTo(main.top)
                    bottom.linkTo(main.bottom)
                    absoluteLeft.linkTo(main.absoluteRight)
                },
            painter = painterResource(id = R.drawable.ic_temperature_celsius),
            contentDescription = stringResource(id = R.string.temperature),
            tint = secondaryWhiteColor
        )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(max) {
                    absoluteLeft.linkTo(param.absoluteRight)
                    top.linkTo(param.top)
                },
            text = maxTemperature,
            color = primaryWhiteColor
        )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(min) {
                    absoluteLeft.linkTo(param.absoluteRight)
                    bottom.linkTo(param.bottom)
                },
            text = minTemperature,
            color = primaryWhiteColor
        )

    }
}

@Composable
fun WeatherPlaceSelectItem(
    modifier: Modifier = Modifier,
    selectedPlace: UserMapMarker,
    userPlaces: List<UserMapMarker>,
    scrollState: LazyListState,
    onItemClick: (UserMapMarker) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }

    val color = animateColorAsState(
        targetValue = if (scrollState.firstVisibleItemScrollOffset > 0) {
            primaryTextColor
        } else {
            Color.White
        },
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable {
                    isExpanded.value = !isExpanded.value
                },
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherAppBarText(
                text = selectedPlace.title,
                textColor = color.value
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, "")
            WeatherDropdownMenu(
                userPlaces = userPlaces,
                isExpanded = isExpanded,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun WeatherDropdownMenu(
    userPlaces: List<UserMapMarker>,
    isExpanded: MutableState<Boolean>,
    onItemClick: (UserMapMarker) -> Unit
) {

    DropdownMenu(
        modifier = Modifier.requiredWidthIn(200.dp, 400.dp),
        expanded = isExpanded.value,
        onDismissRequest = {
            isExpanded.value = !isExpanded.value
        }) {
        userPlaces.forEachIndexed { index, userMapMarker ->
            DropdownMenuItem(onClick = {
                onItemClick(userMapMarker)
                isExpanded.value = !isExpanded.value
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp,
                        Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(
                            id =
                            if (index == 0) {
                                R.drawable.ic_baseline_my_location_24
                            } else {
                                R.drawable.ic_baseline_location_on_24
                            }
                        ),
                        tint = secondaryFigmaColor,
                        contentDescription = "Location icon",
                        modifier = Modifier.padding(2.dp)
                    )
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = userMapMarker.title
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherAppBarText(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String
) {
    Text(
        modifier = modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        softWrap = true,
        text = text
    )
}

@Composable
fun WeatherLocationIcon(
    color: Color
) {
    Icon(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
        tint = color,
        contentDescription = ""
    )
}
























