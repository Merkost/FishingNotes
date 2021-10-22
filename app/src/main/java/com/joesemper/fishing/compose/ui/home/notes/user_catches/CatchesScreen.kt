package com.joesemper.fishing.compose.ui.home.notes.user_catches

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.ItemAdd
import com.joesemper.fishing.compose.ui.home.notes.ItemUserCatch
import com.joesemper.fishing.model.entity.content.UserCatch


@ExperimentalAnimationApi
@Composable
fun UserCatches(
    catches: List<UserCatch>,
    addNewCatchClicked: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ItemAdd(
                icon = painterResource(R.drawable.ic_add_catch),
                text = stringResource(R.string.add_new_catch),
                onClickAction = addNewCatchClicked
            )
        }
        items(items = catches.sortedByDescending { it.date }) {
            ItemUserCatch(
                userCatch = it,
                userCatchClicked = userCatchClicked
            )
        }
    }
}