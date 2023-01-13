package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.theme.secondaryTextColor

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
@Deprecated("Updated", ReplaceWith("HeaderText(\n" +
        "    modifier,\n" +
        "    text,\n" +
        "    textStyle)")
)
fun HeaderTextOld(
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
fun HeaderText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.h5.copy(
        textAlign = TextAlign.Start,
        color = MaterialTheme.colors.onSurface
    )
) {
    Text(
        modifier = modifier,
        style = textStyle,
        text = text
    )
}

@Composable
fun HeaderTextSecondary(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start
) {
    HeaderTextOld(modifier, text, textAlign, MaterialTheme.customColors.secondaryTextColor)
}

@Composable
fun SubtitleText(
    modifier: Modifier = Modifier, text: String,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor, maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start
) {

    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle1,
        color = textColor,
        text = text,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
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
        softWrap = true,
        overflow = TextOverflow.Ellipsis
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
    style: TextStyle = LocalTextStyle.current,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor
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
fun SecondaryTextLight(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor
) {
    SecondaryText(
        textAlign = textAlign,
        modifier = modifier,
        textColor = secondaryTextColor,
        text = text,
        maxLines = maxLines
    )
}

@Composable
fun SecondaryTextSmall(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.customColors.secondaryTextColor
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
        color = MaterialTheme.customColors.secondaryTextColor,
        text = text,
        maxLines = 1
    )
}

@Composable
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: Int, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = MaterialTheme.customColors.secondaryTextColor,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
        )
        SubtitleText(text = text)
    }
}

@Composable
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: ImageVector, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.place),
            tint = MaterialTheme.customColors.secondaryTextColor,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
        )
        SubtitleText(text = text)
    }
}

@Composable
fun DividerText(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
    onIconClick: (() -> Unit)? = null
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth(),
    ) {
        val (divider1, textView, iconView, divider2) = createRefs()
        Divider(modifier = Modifier.constrainAs(divider1) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            absoluteLeft.linkTo(parent.absoluteLeft)
            absoluteRight.linkTo(textView.absoluteLeft, 16.dp)
            width = Dimension.fillToConstraints
        })

        SecondaryText(
            modifier = Modifier.constrainAs(textView) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = text
        )

        if (icon != null) {
            IconButton(
                modifier = Modifier.constrainAs(iconView) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(textView.absoluteRight)
                },
                onClick = { onIconClick?.invoke() }
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = icon,
                    tint = MaterialTheme.customColors.secondaryIconColor,
                    contentDescription = null
                )
            }

        }


        Divider(modifier = Modifier.constrainAs(divider2) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            absoluteRight.linkTo(parent.absoluteRight)
            if (icon != null) {
                absoluteLeft.linkTo(iconView.absoluteRight)
            } else {
                absoluteLeft.linkTo(textView.absoluteRight, 16.dp)
            }
            width = Dimension.fillToConstraints
        })
    }
}