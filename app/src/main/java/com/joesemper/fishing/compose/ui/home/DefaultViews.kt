package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.Shapes
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.primaryFigmaTextColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaTextColor

@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content)
}

@Composable
fun MyCard(modifier: Modifier = Modifier, shape: CornerBasedShape = Shapes.small, elevation: Dp = 8.dp, content: @Composable () -> Unit) {
    Card(elevation = elevation, shape = shape,
        modifier = Modifier.fillMaxWidth().padding(4.dp), content = content)
}

@Composable
fun UserProfile(user: User?) {
    user?.let { nutNullUser ->
        Crossfade(nutNullUser, animationSpec = tween(500)) { animatedUser ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(
                        data = animatedUser.userPic,
                        builder = {
                            transformations(CircleCropTransformation())
                            //crossfade(500)
                        }
                    ),
                    contentDescription = stringResource(R.string.fisher),
                    modifier = Modifier.padding(9.dp),
                )
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        animatedUser.userName.split(" ")[0],
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.button.fontSize
                    )
                    Text(
                        "@" + stringResource(R.string.fisher),
                        fontSize = MaterialTheme.typography.caption.fontSize
                    )
                }
            }
        }
    } ?: Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_fisher),
            contentDescription = stringResource(R.string.fisher),
            Modifier
                .fillMaxHeight()
                .padding(10.dp)
        )
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                stringResource(R.string.fisher),
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.button.fontSize
            )
            Text(
                "@" + stringResource(R.string.fisher),
                fontSize = MaterialTheme.typography.caption.fontSize
            )
        }
    }
}

@Composable
fun PlaceInfo(user: User?, place: UserMapMarker, placeClicked: (UserMapMarker) -> Unit) {
    MyCardNoPadding {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .padding(horizontal = 5.dp)
                .clickable { placeClicked(place) }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Place, stringResource(R.string.place), tint = secondaryFigmaColor)
                Spacer(modifier = Modifier.width(150.dp))
                UserProfile(user)
            }
            Text(place.title, fontWeight = FontWeight.Bold)
            if (!place.description.isNullOrEmpty()) Text(place.description!!)
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
fun CatchInfo(catch: UserCatch, user: User?) {
    MyCardNoPadding {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    catch.title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Row( modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxHeight()) {
                UserProfile(user) }
            }
            if (!catch.description.isNullOrEmpty()) Text(
                catch.description, modifier = Modifier.fillMaxWidth(),
                fontSize = MaterialTheme.typography.button.fontSize
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(catch.time, fontSize = MaterialTheme.typography.caption.fontSize)
                Text(catch.date, fontSize = MaterialTheme.typography.caption.fontSize)
            }
        }
    }
}

@Composable
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: Int, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(8.dp))
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = primaryFigmaColor,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(text)
    }
}

@Composable
fun SimpleOutlinedTextField(textState: MutableState<String>, label: String) {
    var text by rememberSaveable { textState }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
fun HeaderText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h5,
        maxLines = 1,
        color = primaryFigmaTextColor,
        text = text
    )
}

@Composable
fun SubtitleText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle2,
        maxLines = 1,
        color = secondaryFigmaTextColor,
        text = text
    )
}

@Composable
fun PrimaryText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        maxLines = 1,
        color = primaryFigmaTextColor,
        text = text
    )
}

@Composable
fun SecondaryText(modifier: Modifier = Modifier, text: String) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = secondaryFigmaTextColor,
        text = text
    )
}

@Composable
fun SecondaryTextColored(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body2,
        color = primaryFigmaColor,
        text = text
    )
}