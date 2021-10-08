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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.ui.theme.backgroundWhiteColor
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor

@Composable
fun WeatherParameterItem(
    modifier: Modifier = Modifier,
    color: Color = backgroundWhiteColor,
    icon: Int,
    text: String,
    isExpanded: Boolean
) {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .width(100.dp),
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