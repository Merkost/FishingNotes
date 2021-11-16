package com.joesemper.fishing.compose.ui.home

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.theme.*
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(
        elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content
    )
}

@Composable
fun MyCard(
    shape: CornerBasedShape = RoundedCornerShape(8.dp), modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        elevation = 8.dp, shape = shape,
        modifier = modifier.fillMaxWidth(), content = content
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    padding: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        backgroundColor = Color.White,
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding), content = content
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
    val darkTheme = isSystemInDarkTheme()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(8.dp))
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.place),
            tint = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
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
    val darkTheme = isSystemInDarkTheme()

    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle1,
        color = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
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
        color = MaterialTheme.colors.onSurface,
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
fun SecondaryTextColored(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = primaryFigmaColor,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = color,
        text = text,
        maxLines = maxLines
    )
}

@Composable
fun SecondaryText(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 18.sp,
        color = secondaryFigmaTextColor,
        text = text,
        maxLines = maxLines
    )
}

@Composable
fun SupportText(
    modifier: Modifier = Modifier, text: String,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = supportFigmaTextColor,
        text = text,
        maxLines = 1
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

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun FullScreenPhoto(photo: MutableState<Uri?>) {

    /*val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(1f) }*/

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = 0.8f - abs(offsetY.value).div(600)
    val backgroundColor = animateColorAsState(
        targetValue = Color.Black.copy(if (alpha < 0) 0f else alpha)
    )

    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true, usePlatformDefaultWidth = false
        ),
        onDismissRequest = { photo.value = null }) {
        Surface(
            Modifier
                .fillMaxSize(), color = backgroundColor.value
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(0, offsetY.value.roundToInt())
                    }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch {
                                offsetY.snapTo(offsetY.value + delta)
                            }
                        },
                        orientation = Orientation.Vertical,
                        onDragStarted = {

                        },
                        onDragStopped = {

                            if (offsetY.value >= 400f || offsetY.value <= -400f) photo.value =
                                null else
                                coroutineScope.launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            delayMillis = 0
                                        )
                                    )
                                }
                        }
                    )
                    .clickable {
                        photo.value = null
                    }
                /*}.pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale.value *= zoom
                        rotationState.value += rotation
                    }
                }.graphicsLayer(
                    // adding some zoom limits (min 50%, max 200%)
                    scaleX = maxOf(.5f, minOf(1f, scale.value)),
                    scaleY = maxOf(.5f, minOf(1f, scale.value)),
                    rotationZ = rotationState.value
                )*/,
                painter = rememberImagePainter(data = photo.value),
                contentDescription = stringResource(id = R.string.catch_photo)
            )
        }

    }
}

@Composable
fun SimpleUnderlineTextField(
    modifier: Modifier = Modifier,
    text: String,
    label: String = "",
    trailingIcon: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    onClick: () -> Unit = { },
    helperText: String? = null
) {
    val darkTheme = isSystemInDarkTheme()
    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, start = 8.dp),
            textAlign = TextAlign.Start,
            color = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
            style = MaterialTheme.typography.body2,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            value = text,
            textStyle = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
            /*colors = TextFieldDefaults.textFieldColors(
                textColor = primaryFigmaTextColor,
                backgroundColor = backgroundGreenColor,
                cursorColor = Color.Black,
                focusedIndicatorColor = primaryFigmaTextColor,
                unfocusedIndicatorColor = primaryFigmaTextColor
            ),*/
            onValueChange = { },
            shape = RoundedCornerShape(2.dp),
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
fun DefaultButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int? = null,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_shortcut_24),
                    "",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            SecondaryTextColored(
                color = Color.White,
                text = text,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DefaultButtonOutlined(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_shortcut_24),
                    "",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            PrimaryText(
                text = text,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun DefaultButtonText(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int? = null,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_shortcut_24),
                    "",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            SecondaryTextColored(
                text = text,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DefaultButtonSecondaryText(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int? = null,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_shortcut_24),
                    "",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            SupportText(
                text = text,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1
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
                tint = MaterialTheme.colors.onPrimary,
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
            tint = MaterialTheme.colors.onPrimary,
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


