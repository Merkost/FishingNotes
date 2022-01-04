package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor

@Composable
fun BigText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h3,
        textAlign = textAlign,
        color = textColor,
        text = text
    )
}

@Composable
fun HeaderText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h5,
        textAlign = textAlign,
        color = textColor,
        text = text
    )
}

@Composable
fun HeaderTextSecondary(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start
) {
    HeaderText(modifier, text, textAlign, secondaryFigmaTextColor)
}

@Composable
fun SubtitleText(
    modifier: Modifier = Modifier, text: String,
    textColor: Color? = null, singleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Start
) {
    val darkTheme = isSystemInDarkTheme()

    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle1,
        color = textColor ?: if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
        text = text,
        textAlign = textAlign,
    )
}

@Composable
fun PrimaryText(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h4,
        fontSize = 18.sp,
        fontWeight = fontWeight,
        textAlign = textAlign,
        color = textColor,
        text = text,
        maxLines = maxLines,
        softWrap = true
    )
}

@Composable
fun PrimaryTextSmall(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h4,
        fontSize = 14.sp,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        color = textColor,
        text = text
    )
}

@Composable
fun PrimaryTextBold(modifier: Modifier = Modifier, text: String) {
    PrimaryText(
        modifier = modifier,
        fontWeight = FontWeight.SemiBold,
        text = text,
        maxLines = 1
    )
}

@Composable
fun SecondaryTextColored(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        style = style,
        color = color,
        text = text,
        maxLines = maxLines,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = secondaryTextColor
) {
    Text(
        textAlign = textAlign,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 18.sp,
        color = textColor,
        text = text,
        maxLines = maxLines
    )
}

@Composable
fun SecondaryTextSmall(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = secondaryTextColor
) {
    Text(
        textAlign = textAlign,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 14.sp,
        color = textColor,
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SupportText(
    modifier: Modifier = Modifier, text: String,
    style: TextStyle = MaterialTheme.typography.body1,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = style,
        color = Color.Gray,
        text = text,
        maxLines = 1
    )
}

@Composable
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: Int, text: String) {
    val darkTheme = isSystemInDarkTheme()
    //TODO: Remove dark theme checking

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
        )
        SubtitleText(text = text)
    }
}