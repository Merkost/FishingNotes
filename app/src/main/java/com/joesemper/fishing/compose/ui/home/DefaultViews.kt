package com.joesemper.fishing.compose.ui.home

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.*

@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(
        elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content
    )
}

@Composable
fun MyCard(shape: CornerBasedShape = RoundedCornerShape(8.dp), content: @Composable () -> Unit) {
    Card(
        elevation = 8.dp, shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), content = content
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp), content = content
    )
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
                Icon(
                    Icons.Default.Place,
                    stringResource(R.string.place),
                    tint = secondaryFigmaColor
                )
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
fun SubtitleWithIcon(modifier: Modifier = Modifier, icon: Int, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(8.dp))
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = secondaryFigmaTextColor,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.size(8.dp))
        SubtitleText(text = text)
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
fun HeaderText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = primaryFigmaTextColor
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h6,
        maxLines = 1,
        textAlign = TextAlign.Start,
        color = primaryFigmaTextColor,
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
fun SubtitleText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle1,
        maxLines = 1,
        color = secondaryFigmaTextColor,
        text = text
    )
}

@Composable
fun PrimaryText(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    text: String
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 18.sp,
        fontWeight = fontWeight,
        maxLines = 1,
        color = primaryFigmaTextColor,
        text = text
    )
}

@Composable
fun PrimaryTextBold(modifier: Modifier = Modifier, text: String) {
    PrimaryText(
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        text = text
    )
}

@Composable
fun SecondaryText(modifier: Modifier = Modifier, text: String) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 18.sp,
        color = secondaryFigmaTextColor,
        text = text
    )
}

@Composable
fun SecondaryTextColored(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = primaryFigmaColor
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = color,
        text = text
    )
}

@Composable
fun SupportText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body2,
        color = supportFigmaTextColor,
        text = text
    )
}

@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: () -> Unit,
    title: String,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { onNavClick() }) {
                Icon(
                    imageVector = navIcon,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        elevation = 4.dp,
        actions = actions
    )
}

@Composable
fun FullScreenPhoto(photo: MutableState<Uri?>) {
    Dialog(
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
        onDismissRequest = { photo.value = null }) {
        Image(
            modifier = Modifier
                .padding(64.dp)
                .wrapContentSize()
                .clickable {
                    photo.value = null
                },
            painter = rememberImagePainter(data = photo.value),
            contentDescription = stringResource(id = R.string.catch_photo)
        )
    }
}

@Composable
fun SimpleUnderlineTextField(
    modifier: Modifier = Modifier,
    text: String,
    label: String = "",
    trailingIcon: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    helperText: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, start = 8.dp),
            textAlign = TextAlign.Start,
            color = secondaryFigmaTextColor,
            style = MaterialTheme.typography.body2,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            value = text,
            textStyle = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = primaryFigmaTextColor,
                backgroundColor = backgroundGreenColor,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            onValueChange = { },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon
        )
        helperText?.let {
            SecondaryTextColored(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .align(Alignment.End),
                text = it
            )
        }
    }
}

@Composable
fun FabWithMenu(
    modifier: Modifier = Modifier,
    items: List<FabMenuItem>
) {
    val toState = remember { mutableStateOf(MultiFabState.COLLAPSED) }
    val transition = updateTransition(targetState = toState, label = "")

    val size = transition.animateDp(label = "") { state ->
        if (state.value == MultiFabState.EXPANDED) 48.dp else 0.dp
    }
    val rotation = transition.animateFloat(label = "") { state ->
        if (state.value == MultiFabState.EXPANDED) 45f else 0f
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        items.forEach {
            FabMenuItem(item = it, modifier = Modifier.size(size.value))
        }

        FloatingActionButton(onClick = {
            if (transition.currentState.value == MultiFabState.EXPANDED) {
                transition.currentState.value = MultiFabState.COLLAPSED
            } else transition.currentState.value = MultiFabState.EXPANDED
        }) {
            Icon(
                modifier = Modifier.rotate(rotation.value),
                tint = Color.White,
                painter = painterResource(id = R.drawable.ic_baseline_plus),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun FabMenuItem(item: FabMenuItem, modifier: Modifier = Modifier) {
    FloatingActionButton(
        backgroundColor = primaryFigmaColor,
        modifier = modifier,
        onClick = item.onClick
    ) {
        Icon(
            tint = Color.White,
            painter = painterResource(id = item.icon),
            contentDescription = ""
        )
    }
}

class FabMenuItem(
    val icon: Int,
    val text: String = "",
    val onClick: () -> Unit
)

enum class MultiFabState {
    COLLAPSED, EXPANDED
}


