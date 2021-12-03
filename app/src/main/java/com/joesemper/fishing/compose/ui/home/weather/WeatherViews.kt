package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.compose.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.HeaderText
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.theme.primaryTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryColor
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.Weather
import com.joesemper.fishing.model.mappers.getWeatherIconByName

@Composable
fun PrimaryWeatherItem(
    modifier: Modifier = Modifier,
    weather: Weather,
    temperature: Float,
    textTint: Color = MaterialTheme.colors.primaryVariant,
    iconTint: Color = Color.Unspecified,
    temperatureUnit: String
) {

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (temp, icon, description) = createRefs()

        //val guideline = createGuidelineFromAbsoluteLeft(0.5f)
        createHorizontalChain(icon, temp, chainStyle = ChainStyle.Spread)

        Icon(
            modifier = Modifier
                .size(64.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top, 8.dp)
                    absoluteRight.linkTo(temp.absoluteLeft)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
            painter = painterResource(id = getWeatherIconByName(weather.icon)),
            contentDescription = stringResource(id = R.string.weather),
            tint = iconTint
        )
        PrimaryText(
            modifier = Modifier.constrainAs(description) {
                top.linkTo(icon.bottom, 4.dp)
                absoluteLeft.linkTo(icon.absoluteLeft)
                absoluteRight.linkTo(icon.absoluteRight)
            },
            text = weather.description.replaceFirstChar { it.uppercase() },
            textColor = textTint
        )

        HeaderText(
            modifier = Modifier
                .constrainAs(temp) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(icon.absoluteRight)
                    absoluteRight.linkTo(parent.absoluteRight,)
                    bottom.linkTo(parent.bottom, 8.dp)
                },
            text = getTemperature(
                temperature,
                TemperatureValues.valueOf(temperatureUnit)
            ) + getTemperatureFromUnit(temperatureUnit),
            textColor = textTint
        )

    }
}

@Composable
fun WeatherPlaceSelectItem(
    modifier: Modifier = Modifier,
    selectedPlace: UserMapMarker,
    userPlaces: List<UserMapMarker>,
    onItemClick: (UserMapMarker) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isExpanded.value = !isExpanded.value
            },
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherAppBarText(
                text = selectedPlace?.title ?: "Не удалось определить местоположение",
                textColor = Color.White
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, "", tint = Color.White)
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
        modifier = Modifier.requiredWidthIn(200.dp, 500.dp),
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
                        tint = secondaryColor,
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
        text = text,
        overflow = TextOverflow.Ellipsis,
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

@Composable
fun WeatherHeaderText(
    modifier: Modifier = Modifier,
    color: Color = primaryTextColor,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.body1,
        color = color
    )
}

@Composable
fun WeatherPrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = primaryTextColor
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        fontSize = 20.sp
    )
}

@Composable
fun WeatherLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.clouds))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition,
        progress,
        modifier = modifier
    )
}

@Composable
fun WeatherEmptyView(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
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
























