package com.joesemper.fishing.compose.ui.home.notes

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.theme.*
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.compose.ui.theme.*
import com.joesemper.fishing.utils.getTimeByMilliseconds

@ExperimentalComposeUiApi
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

    AnimatedVisibility(fullScreenPhoto.value != null) {
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

    DefaultCard(modifier = Modifier
        .clickable {
            userCatchClicked(userCatch)
        }) {
        ConstraintLayout(modifier = Modifier.padding(8.dp)) {
            val (photos, fish, weight, kg, description, icon, place, date) = createRefs()
            val guideline = createGuidelineFromAbsoluteLeft(110.dp)

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
                    absoluteRight.linkTo(kg.absoluteLeft, 4.dp)
                    top.linkTo(parent.top)
                },
                text = userCatch.fishWeight.toString()
            )

            SecondaryText(
                modifier = Modifier.constrainAs(description) {
                    absoluteLeft.linkTo(fish.absoluteLeft)
                    linkTo(fish.bottom, icon.top, bottomMargin = 2.dp, bias = 0F)
                },
                text = if (userCatch.description.isNotBlank()) {
                    userCatch.description
                } else {
                    stringResource(id = R.string.no_description)
                }
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(icon) {
                        absoluteLeft.linkTo(guideline)
                        bottom.linkTo(parent.bottom)
                    },
                painter = painterResource(
                    id = R.drawable.ic_baseline_location_on_24
                ),
                contentDescription = stringResource(R.string.icon),
                tint = secondaryFigmaTextColor
            )

            SecondaryTextColored(
                modifier = Modifier.constrainAs(place) {
                    top.linkTo(icon.top)
                    bottom.linkTo(icon.bottom)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
                text = userCatch.placeTitle
            )

            SupportText(
                modifier = Modifier.constrainAs(date) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(place.top)
                },
                text = getTimeByMilliseconds(userCatch.date)
            )
        }
    }
}

@Composable
fun ItemUserPlace(place: UserMapMarker, userPlaceClicked: (UserMapMarker) -> Unit) {
    DefaultCard(modifier = Modifier.clickable {
        userPlaceClicked(place)
    }) {
        ConstraintLayout(
            modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            val (icon, title, description, amount, fishIcon) = createRefs()

            Icon(
                modifier = Modifier
                    .padding(5.dp)
                    .size(32.dp)
                    .constrainAs(icon) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                painter = painterResource(R.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(R.string.place),
                tint = secondaryFigmaColor
            )

            PrimaryText(
                modifier = Modifier.constrainAs(title) {
                    linkTo(icon.absoluteRight, amount.absoluteLeft, bias = 0f)
                    top.linkTo(parent.top)
                },
                text = place.title
            )

            ItemCounter(
                modifier = Modifier.constrainAs(fishIcon) {
                    bottom.linkTo(parent.bottom)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                text = "0",
                icon = R.drawable.ic_fish
            )

            SecondaryText(
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(title.bottom)
                    absoluteLeft.linkTo(title.absoluteLeft)
                },
                text = if (place.description.isNotBlank()) {
                    place.description
                } else {
                    stringResource(id = R.string.no_description)
                }
            )

        }
    }
}

@Composable
fun ItemAdd(
    icon: Painter,
    text: String,
    onClickAction: () -> Unit
) {
    DefaultCard {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clickable { onClickAction() }
                .padding(5.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Icon(
                    painter = icon,
                    contentDescription = stringResource(R.string.new_catch),
                    modifier = Modifier
                        .weight(2f)
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp),
                    tint = primaryFigmaColor
                )
                SecondaryText(
                    text = text,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ItemDate(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            shape = RoundedCornerShape(24.dp), color = cardColor
        ) {
            SecondaryTextColored(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = text,
                color = Color.White
            )
        }
    }
}

@Composable
fun NoElementsView(
    modifier: Modifier = Modifier,
    mainText: String,
    secondaryText: String,
    onClickAction: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        SecondaryText(text = mainText)
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryTextColored(
            modifier = Modifier.clickable {
                onClickAction()
            },
            text = secondaryText
        )
    }
}

@Composable
fun ItemPlace(place: UserMapMarker, userPlaceClicked: (UserMapMarker) -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .height(75.dp)
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {
                userPlaceClicked(place)
            }
    ) {
        val (icon, title, description, amount, fishIcon, divider) = createRefs()

        Icon(
            modifier = Modifier
                .padding(5.dp)
                .size(32.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                },
            painter = painterResource(R.drawable.ic_baseline_location_on_24),
            contentDescription = stringResource(R.string.place),
            tint = secondaryFigmaColor
        )

        PrimaryText(
            modifier = Modifier.constrainAs(title) {
                linkTo(icon.absoluteRight, amount.absoluteLeft, bias = 0f)
                top.linkTo(parent.top)
            },
            text = place.title
        )

        Icon(
            modifier = Modifier.constrainAs(fishIcon) {
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                top.linkTo(title.top)
                bottom.linkTo(title.bottom)
            },
            painter = painterResource(id = R.drawable.ic_fish),
            tint = secondaryFigmaTextColor,
            contentDescription = stringResource(id = R.string.fish_catch)
        )

        PrimaryText(
            modifier = Modifier.constrainAs(amount) {
                absoluteRight.linkTo(fishIcon.absoluteLeft, 2.dp)
                top.linkTo(title.top)
                bottom.linkTo(title.bottom)
            },
            text = place.catchesCount.toString()
        )

        SecondaryText(
            modifier = Modifier.constrainAs(description) {
                top.linkTo(title.bottom)
                absoluteLeft.linkTo(title.absoluteLeft)
            },
            text = if (place.description.isNotBlank()) {
                place.description
            } else {
                stringResource(id = R.string.no_description)
            }
        )

        Divider(
            startIndent = 42.dp,
            modifier = Modifier.constrainAs(divider) {
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
            })
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemCatch(userCatch: UserCatch, userCatchClicked: (UserCatch) -> Unit) {
    val photo = if (userCatch.downloadPhotoLinks.isNotEmpty()) {
        userCatch.downloadPhotoLinks.first()
    } else {
        null
    }

    Surface() {
        ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                userCatchClicked(userCatch)
            }) {
            val (photos, fish, weight, kg, description, icon, place, date, divider) = createRefs()
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
                    absoluteLeft.linkTo(fish.absoluteLeft)
                    linkTo(fish.bottom, icon.top, bottomMargin = 2.dp, bias = 0F)
                },
                text = if (userCatch.description.isNotBlank()) {
                    userCatch.description
                } else {
                    stringResource(id = R.string.no_description)
                }
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(icon) {
                        absoluteLeft.linkTo(guideline)
                        bottom.linkTo(parent.bottom)
                    },
                painter = painterResource(
                    id = R.drawable.ic_baseline_location_on_24
                ),
                contentDescription = stringResource(R.string.icon),
                tint = secondaryFigmaTextColor
            )

            SecondaryTextColored(
                modifier = Modifier.constrainAs(place) {
                    top.linkTo(icon.top)
                    bottom.linkTo(icon.bottom)
                    absoluteLeft.linkTo(icon.absoluteRight)
                },
                text = userCatch.placeTitle
            )

            SupportText(
                modifier = Modifier.constrainAs(date) {
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                    top.linkTo(place.top)
                },
                text = getTimeByMilliseconds(userCatch.date)
            )

            Divider(
                modifier = Modifier.constrainAs(divider) {
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                }
            )
        }
    }
}

@Composable
fun ItemCounter(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int
) {
    Row(modifier = modifier.padding(2.dp)) {
        Icon(
            tint = secondaryFigmaTextColor,
            painter = painterResource(id = icon),
            contentDescription = "",
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SecondaryText(text = text)
    }

}

@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.fillMaxSize(),
        colorFilter = ColorFilter.tint(
            surfaceGreenColor,
            BlendMode.ColorDodge
        ),
        painter = painterResource(id = R.drawable.ic_pattern_background),
        contentDescription = "",
        alpha = 0.1f,
        contentScale = ContentScale.FillWidth
    )
}
