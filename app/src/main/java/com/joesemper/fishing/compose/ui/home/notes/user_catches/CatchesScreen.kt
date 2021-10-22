package com.joesemper.fishing.compose.ui.home.notes.user_catches

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.DefaultCard
import com.joesemper.fishing.compose.ui.home.ItemUserCatch
import com.joesemper.fishing.compose.ui.home.SecondaryText
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.ui.theme.primaryFigmaColor


@ExperimentalAnimationApi
@Composable
fun UserCatches(
    catches: List<UserCatch>,
    addNewCatchClicked: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ItemAddCatch { addNewCatchClicked() } }
        items(items = catches.sortedBy { it.date }) {
            ItemUserCatch(
                userCatch = it,
                userCatchClicked = userCatchClicked
            )
        }
    }
}

@Composable
fun ItemAddCatch(addCatch: () -> Unit) {
    DefaultCard {
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
                SecondaryText(
                    text = stringResource(R.string.add_new_catch),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}