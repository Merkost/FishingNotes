package com.joesemper.fishing.compose.ui.home

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.catch_screen.EditNoteDialog
import com.joesemper.fishing.compose.ui.theme.*
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@ExperimentalComposeUiApi
@Composable
fun DefaultDialog(
    primaryText: String? = null,
    secondaryText: String? = null,
    neutralButtonText: String = "",
    onNeutralClick: (() -> Unit) = { },
    negativeButtonText: String = "",
    onNegativeClick: () -> Unit = { },
    positiveButtonText: String = "",
    onPositiveClick: () -> Unit = { },
    onDismiss: () -> Unit = { },
    content: @Composable() (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        DefaultCard(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .animateContentSize()
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize()
                ) {
                    primaryText?.let {
                        PrimaryText(
                            text = primaryText,
                        )
                    }
                    secondaryText?.let {
                        Spacer(Modifier.size(4.dp))
                        SecondaryText(text = secondaryText, textAlign = TextAlign.Start)
                    }

                    content?.let {
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .wrapContentSize()
                                .animateContentSize()
                        ) {
                            content()
                        }
                    }
                }
                if (neutralButtonText.isNotEmpty() || negativeButtonText.isNotEmpty()
                    || positiveButtonText.isNotEmpty()) {

                    Spacer(modifier = Modifier.size(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (neutralButtonText.isNotEmpty()) {
                            DefaultButtonSecondaryText(
                                text = neutralButtonText,
                                onClick = onNeutralClick,
                                shape = RoundedCornerShape(24.dp)
                            )
                        } else Spacer(modifier = Modifier.size(1.dp))
                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (negativeButtonText.isNotEmpty()) {
                                DefaultButtonText(
                                    text = negativeButtonText,
                                    onClick = onNegativeClick,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }
                            if (positiveButtonText.isNotEmpty())
                                DefaultButton(
                                    text = positiveButtonText,
                                    onClick = onPositiveClick,
                                    shape = RoundedCornerShape(24.dp)
                                )

                        }
                    }
                }
                }

        }
    }
}

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
        modifier = modifier, content = content
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(6.dp),
    padding: Dp = 4.dp,
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding),
        content = content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DefaultCardClickable(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(6.dp),
    padding: Dp = 4.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        onClick = onClick,
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

@Composable
fun SimpleOutlinedTextField(
    textState: MutableState<String>,
    label: String,
    singleLine: Boolean = true,
) {
    var text by rememberSaveable { textState }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = singleLine
    )
}

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
fun SubtitleText(modifier: Modifier = Modifier, text: String,
                 textColor: Color? = null, singleLine: Boolean = true) {
    val darkTheme = isSystemInDarkTheme()

    Text(
        modifier = modifier,
        style = MaterialTheme.typography.subtitle1,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        color = textColor ?: if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
        text = text
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
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: () -> Unit,
    title: String,
    subtitle: String? = null,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = {
            Column() {
                Text(text = title)
                if (subtitle != null) {
                    SecondaryTextSmall(
                        text = subtitle,
                        textColor = MaterialTheme.colors.onPrimary
                    )
                }

            }

        },
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
        //TODO: Refactoring in Theme.kt
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
    textStyle: TextStyle = MaterialTheme.typography.button,
    shape: Shape = MaterialTheme.shapes.small,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        shape = shape,
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
                style = textStyle,
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
    style: TextStyle = MaterialTheme.typography.button,
    icon: Int? = null,
    shape: Shape = MaterialTheme.shapes.small,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        shape = shape,
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
                style = style,
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
    shape: Shape = MaterialTheme.shapes.small,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        shape = shape,
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
                style = MaterialTheme.typography.button,
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

@ExperimentalComposeUiApi
@Composable
fun DefaultNoteView(
    modifier: Modifier = Modifier,
    note: String,
    onSaveNoteChange: (String) -> Unit
) {

    val dialogState = remember { mutableStateOf(false) }

    if (dialogState.value) {
        EditNoteDialog(
            note = note,
            dialogState = dialogState,
            onSaveNoteChange = onSaveNoteChange
        )
    }

    DefaultCardClickable(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            dialogState.value = true
        }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            val (subtitle, text) = createRefs()

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
                icon = R.drawable.ic_baseline_sticky_note_2_24,
                text = stringResource(id = R.string.note)
            )

            if (note.isBlank()) {
                SecondaryText(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .constrainAs(text) {
                            top.linkTo(subtitle.bottom, 16.dp)
                            absoluteLeft.linkTo(subtitle.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                            width = Dimension.fillToConstraints
                        },
                    text = stringResource(id = R.string.no_description)
                )
            } else {
                PrimaryText(
                    modifier = Modifier.constrainAs(text) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        absoluteLeft.linkTo(subtitle.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                        width = Dimension.fillToConstraints
                    },
                    text = note
                )
            }
        }
    }
}

@Composable
fun DefaultIconButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = icon,
            contentDescription = null,
            tint = primaryTextColor
        )
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

@Composable
fun ButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.primaryVariant
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color = secondaryTextColor)
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier,
                painter = icon,
                contentDescription = null
            )
        }
        PrimaryText(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textColor = MaterialTheme.colors.primaryVariant
        )

    }
}

@Composable
fun FilledButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primaryVariant,
            contentColor = MaterialTheme.colors.onPrimary
        ),
        shape = RoundedCornerShape(24.dp),
//        border = BorderStroke(1.dp, color = secondaryTextColor)
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier,
                painter = icon,
                contentDescription = null
            )
        }
        PrimaryText(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textColor = MaterialTheme.colors.onPrimary
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


