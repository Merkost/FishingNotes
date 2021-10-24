package com.joesemper.fishing.compose.ui.home.notes.user_catches

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.MyCard
import com.joesemper.fishing.compose.ui.home.notes.ItemAdd
import com.joesemper.fishing.model.entity.content.UserCatch
import me.vponomarenko.compose.shimmer.shimmer

val catches = listOf(
    UserCatch(),
    UserCatch(),
    UserCatch(),
    UserCatch(),
    UserCatch()
)

@ExperimentalAnimationApi
@Composable
fun UserCatchesLoading(
    addNewCatchClicked: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ItemAdd(
                icon = painterResource(R.drawable.ic_add_catch),
                text = stringResource(R.string.add_new_catch),
                onClickAction = addNewCatchClicked
            )
        }
        items(items = catches) {
            ItemCatchLoading(
                catch = it
            )
        }
    }
}


@ExperimentalAnimationApi
@Composable
fun ItemCatchLoading(catch: UserCatch) {
    MyCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .padding(5.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_no_photo_vector),
                        stringResource(R.string.place),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .shimmer(),
                        tint = Color.Gray
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .height(18.dp)
                            .shimmer()
                    ) {
                        Text(
                            "Название места",
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.background(Color.LightGray)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .height(18.dp)
                            .shimmer()
                    ) {
                        Text(
                            stringResource(R.string.amount) + ": 0",
                            color = Color.LightGray,
                            modifier = Modifier.background(Color.LightGray)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_location_on_24),
                            stringResource(R.string.place),
                            modifier = Modifier
                                .size(20.dp)
                                .shimmer(),
                            tint = Color.LightGray
                        )
                        Row(
                            modifier = Modifier
                                .height(18.dp)
                                .shimmer()
                        ) {
                            Text(
                                "Place", color = Color.LightGray,
                                modifier = Modifier.background(Color.LightGray), fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .height(18.dp)
                        .shimmer()
                ) {
                    Text(
                        text = "0.0 KG",
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        modifier = Modifier.background(Color.LightGray)
                    )
                }
                Row(
                    modifier = Modifier
                        .height(18.dp)
                        .shimmer()
                ) {
                    Text(
                        "14:06",
                        color = Color.LightGray,
                        modifier = Modifier.background(Color.LightGray),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}