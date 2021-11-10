package com.joesemper.fishing.compose.ui.home.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.HeaderText
import com.joesemper.fishing.compose.ui.home.PrimaryText
import com.joesemper.fishing.compose.ui.home.SecondaryTextSmall
import com.joesemper.fishing.compose.ui.theme.backgroundWhiteColor
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor

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
            .width(100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.weather)
        )
        SecondaryTextSmall(
            text = text,
            textAlign = TextAlign.Center
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
) {
    ConstraintLayout(
        modifier = modifier
            .height(50.dp)
            .width(150.dp),
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
            tint = secondaryFigmaTextColor,

            )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(header) {
                    top.linkTo(icon.top)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
            text = title
        )
        PrimaryText(
            modifier = Modifier
                .constrainAs(meaning) {
                    absoluteLeft.linkTo(header.absoluteLeft)
                    bottom.linkTo(icon.bottom)
                },
            text = text
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
                tint = secondaryFigmaTextColor
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
            .height(50.dp)
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
            text = temperature
        )
        Icon(
            modifier = Modifier
                .size(38.dp)
                .constrainAs(param) {
                    top.linkTo(main.top)
                    bottom.linkTo(main.bottom)
                    absoluteLeft.linkTo(main.absoluteRight)
                },
            painter = painterResource(id = R.drawable.ic_temperature_celsius),
            contentDescription = stringResource(id = R.string.temperature),
            tint = secondaryFigmaTextColor
        )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(max) {
                    absoluteLeft.linkTo(param.absoluteRight)
                    top.linkTo(param.top)
                },
            text = maxTemperature
        )
        SecondaryTextSmall(
            modifier = Modifier
                .constrainAs(min) {
                    absoluteLeft.linkTo(param.absoluteRight)
                    bottom.linkTo(param.bottom)
                },
            text = minTemperature
        )

    }
}
























