package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R

@Composable
fun FabWithMenu(
    modifier: Modifier = Modifier,
    items: List<FabMenuItem>,
    shouldShowBlur: MutableState<Boolean>,
    ) {
    val toState = remember { mutableStateOf(MultiFabState.COLLAPSED) }
    val transition = updateTransition(targetState = toState, label = "")
        .also { shouldShowBlur.value = (it.targetState.value == MultiFabState.EXPANDED) }

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
        backgroundColor = MaterialTheme.colors.primary,
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