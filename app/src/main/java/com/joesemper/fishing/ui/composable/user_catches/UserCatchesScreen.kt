package com.joesemper.fishing.ui.composable.user_catches

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.ui.composable.MyCard
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor


@ExperimentalAnimationApi
@Composable
fun UserCatches(
    catches: List<UserCatch>,
    addNewCatchClicked: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ItemAddCatch { addNewCatchClicked() } }
        items(items = catches) {
            ItemCatch(
                catch = it,
                userCatchClicked
            )
        }
    }
}

@Composable
fun ItemAddCatch(addCatch: () -> Unit) {
    MyCard {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(110.dp)
                .fillMaxWidth()
                .clickable { addCatch() }
                .padding(5.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Icon(
                    painterResource(R.drawable.ic_add_catch),
                    stringResource(R.string.new_catch),
                    modifier = Modifier
                        .weight(2f)
                        .align(Alignment.CenterHorizontally)
                        .size(50.dp),
                    tint = primaryFigmaColor
                )
                Text(stringResource(R.string.add_new_catch), modifier = Modifier.weight(1f))
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ItemCatch(catch: UserCatch, userCatchClicked: (UserCatch) -> Unit) {

    val photo = if (catch.downloadPhotoLinks.isEmpty()) {
        "https://www.industrialempathy.com/img/remote/ZiClJf-1920w.jpg"
    } else {
        catch.downloadPhotoLinks.first()
    }

    MyCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .clickable { userCatchClicked(catch) }
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
            ) {
                Box(modifier = Modifier
                    .size(75.dp)
                    .padding(5.dp)) {
                    Icon(
//                        painter = rememberImagePainter(photo),
                        painterResource(R.drawable.ic_no_photo_vector),
                        stringResource(R.string.place),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        tint = secondaryFigmaColor
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxHeight()
                ) {
                        Text(catch.title, fontWeight = FontWeight.Bold, modifier = Modifier.height(18.dp))
                        Text(stringResource(R.string.amount) + ": " + catch.fishAmount, modifier = Modifier.height(18.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_location_on_24),
                            stringResource(R.string.place),
                            modifier = Modifier.size(20.dp),
                            tint = secondaryFigmaColor
                        )
                        Text("Place", color = primaryFigmaColor, fontSize = 12.sp)
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = catch.fishWeight.toString() + " KG", fontWeight = FontWeight.Bold)
                Text(catch.time, color = primaryFigmaColor, fontSize = 12.sp)
            }
        }
    }
}