package com.joesemper.fishing.compose.ui.home

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.*
import com.joesemper.fishing.utils.getTimeByMilliseconds

@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(
        elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content
    )
}

@Composable
fun MyCard(content: @Composable () -> Unit) {
    Card(
        elevation = 8.dp, modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), content = content
    )
}

@Composable
fun DefaultCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 8.dp,
        modifier = Modifier
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
    textAlign: TextAlign = TextAlign.Start
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
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h6,
        maxLines = 1,
        textAlign = TextAlign.Start,
        color = secondaryFigmaTextColor,
        text = text
    )
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
fun PrimaryText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        fontSize = 18.sp,
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
        fontSize = 18.sp,
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

@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Filled.ArrowBack,
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

@ExperimentalAnimationApi
@Composable
fun ItemPhoto(
    photo: Uri,
    clickedPhoto: (Uri) -> Unit,
    deletedPhoto: (Uri) -> Unit,
    deleteEnabled: Boolean = true
) {

    val fullScreenPhoto = remember {
        mutableStateOf<Uri?>(null)
    }

    Crossfade(photo) { pic ->
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            Image(painter = rememberImagePainter(data = pic),
                contentDescription = Constants.ITEM_PHOTO,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        clickedPhoto(pic)
                        fullScreenPhoto.value = pic
                    })
            if (deleteEnabled) {
                Surface( //For making delete button background half transparent
                    color = Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(25.dp)
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        tint = Color.White,
                        contentDescription = stringResource(R.string.delete_photo),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { deletedPhoto(pic) })
                }
            }
        }
    }

    fullScreenPhoto.value?.let {
        FullScreenPhoto(fullScreenPhoto)
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemCatchPhotos(
    modifier: Modifier = Modifier,
    photo: Uri? = null,
    photosCount: Int = 0
) {
    Crossfade(photo) { pic ->
        Box(
            modifier = modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            if (pic != null) {
                Image(
                    painter = rememberImagePainter(
                        data = pic
                    ),
                    contentDescription = Constants.ITEM_PHOTO,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_no_photo_vector),
                    contentDescription = Constants.ITEM_PHOTO,
                    tint = secondaryFigmaTextColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                )
            }

            if (photosCount > 1) {
                Surface( //For making delete button background half transparent
                    color = Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(25.dp)
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                ) {
                    Text(
                        text = "X$photosCount",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemUserCatch(userCatch: UserCatch, userCatchClicked: (UserCatch) -> Unit) {
    val photo = if (userCatch.downloadPhotoLinks.isNotEmpty()) {
        userCatch.downloadPhotoLinks.first()
    } else {
        null
    }

    DefaultCard() {
        ConstraintLayout(modifier = Modifier
            .padding(2.dp)
            .clickable {
                userCatchClicked(userCatch)
            }) {
            val (photos, fish, weight, kg, description, icon, place, date) = createRefs()
            val guideline = createGuidelineFromAbsoluteLeft(104.dp)

            ItemCatchPhotos(
                modifier = Modifier.constrainAs(photos) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                photo = photo?.toUri(),
                photosCount = userCatch.downloadPhotoLinks.count()
            )

            PrimaryText(
                modifier = Modifier.constrainAs(fish) {
                    absoluteLeft.linkTo(guideline, 4.dp)
                    top.linkTo(parent.top)
                },
                text = userCatch.fishType
            )

            SecondaryText(
                modifier = Modifier.constrainAs(kg) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(parent.top)
                },
                text = stringResource(id = R.string.kg)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(weight) {
                    absoluteRight.linkTo(kg.absoluteLeft, 1.dp)
                    top.linkTo(parent.top)
                },
                text = userCatch.fishWeight.toString()
            )

            SecondaryText(
                modifier = Modifier.constrainAs(description) {
                    absoluteLeft.linkTo(guideline, 4.dp)
                    linkTo(fish.bottom, icon.top, bottomMargin = 2.dp, bias = 0F)
                },
                text = if (userCatch.description.isNotBlank()) {
                    userCatch.description
                } else {
                    stringResource(id = R.string.no_description)
                }
            )

            Icon(
                modifier = Modifier.constrainAs(icon) {
                    absoluteLeft.linkTo(guideline)
                    bottom.linkTo(parent.bottom)
                },
                painter = painterResource(
                    id = R.drawable.ic_baseline_location_on_24
                ),
                contentDescription = stringResource(R.string.icon),
                tint = secondaryFigmaColor
            )

            SecondaryTextColored(
                modifier = Modifier.constrainAs(place) {
                    top.linkTo(icon.top)
                    bottom.linkTo(icon.bottom)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
                text = userCatch.placeTitle
            )

            SecondaryTextColored(
                modifier = Modifier.constrainAs(date) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(place.top)
                },
                text = getTimeByMilliseconds(userCatch.date)
            )
        }
    }
}

