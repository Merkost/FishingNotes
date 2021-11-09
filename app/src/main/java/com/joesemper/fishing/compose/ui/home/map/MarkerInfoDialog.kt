package com.joesemper.fishing.compose.ui.home.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.DefaultCard
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaColor
import com.joesemper.fishing.model.entity.content.UserMapMarker

@ExperimentalMaterialApi
@Composable
fun MarkerInfoDialog(
    marker: UserMapMarker?,
    onDescriptionClick: (UserMapMarker) -> Unit
) {

    Spacer(modifier = Modifier.size(6.dp))
    DefaultCard() {
        marker?.let {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()

            ) {
                val (line, locationIcon, title, description, navigateButton, detailsButton) = createRefs()
                BottomSheetLine(modifier = Modifier.constrainAs(line) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(parent.top, 1.dp)
                })

                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "Marker",
                    tint = secondaryFigmaColor,
                    modifier = Modifier
                        .size(32.dp)
                        .constrainAs(locationIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                            top.linkTo(title.top)
                            bottom.linkTo(title.bottom)
                        }
                )

                Text(
                    text = marker.title,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(end = 56.dp)
                        .constrainAs(title) {
                            top.linkTo(parent.top, 16.dp)
                            absoluteLeft.linkTo(locationIcon.absoluteRight, 8.dp)
                        }
                )

                Text(
                    text = if (marker.description.isEmpty()) {
                        "No description"
                    } else {
                        marker.description
                    },
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.constrainAs(description) {
                        absoluteLeft.linkTo(title.absoluteLeft)
                        top.linkTo(title.bottom, 4.dp)
                    }
                )

                Button(modifier = Modifier.constrainAs(detailsButton) {
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    top.linkTo(description.bottom, 8.dp)
                    bottom.linkTo(parent.bottom, 16.dp)
                },
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        onDescriptionClick(marker)
                    }
                ) {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_shortcut_24),
                            "",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            stringResource(id = R.string.details),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                OutlinedButton(modifier = Modifier.constrainAs(navigateButton) {
                    absoluteRight.linkTo(detailsButton.absoluteLeft, 8.dp)
                    top.linkTo(detailsButton.top)
                    bottom.linkTo(detailsButton.bottom)
                }, shape = RoundedCornerShape(24.dp), onClick = { /*TODO*/ }) {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_navigation_24),
                            "",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            stringResource(id = R.string.navigate),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

    }
}