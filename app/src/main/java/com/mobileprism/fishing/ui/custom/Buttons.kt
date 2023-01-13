package com.mobileprism.fishing.ui.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R

@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    function: @Composable () -> Unit
) = OutlinedButton(
    enabled = enabled,
    onClick = onClick,
    modifier = modifier,
    shape = CircleShape,
    colors = colors
) { function() }


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginWithGoogleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp), elevation = 4.dp,
        onClick = onClick,
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(8.dp)
                .padding(end = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.googleg_standard_color_18),
                contentDescription = stringResource(R.string.google_login),
                modifier = Modifier
                    .background(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colors.onSecondary
                    )
                    .size(32.dp)
                    .padding(4.dp)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.sign_with_google),
                    color = MaterialTheme.colors.onSecondary,
                    style = MaterialTheme.typography.button
                )
            }

        }
    }
}

@Composable
fun FishingTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) = TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.large,
    border = border,
    content = content
)