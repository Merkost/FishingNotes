package com.mobileprism.fishing.ui.components.state

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Motion
import com.mobileprism.fishing.ui.viewstates.BaseViewState

@Composable
fun <T> ScreenStateContent(
    state: BaseViewState<T>,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { LoadingState() },
    error: @Composable (Throwable?) -> Unit = { ErrorStateGeneric() },
    isEmpty: (T) -> Boolean = { false },
    empty: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = state,
        modifier = modifier,
        animationSpec = tween(durationMillis = Motion.medium),
        label = "ScreenStateContent",
    ) { current ->
        when (current) {
            is BaseViewState.Loading -> loading()
            is BaseViewState.Error -> error(current.error)
            is BaseViewState.Success -> {
                if (isEmpty(current.data)) empty() else content(current.data)
            }
        }
    }
}
