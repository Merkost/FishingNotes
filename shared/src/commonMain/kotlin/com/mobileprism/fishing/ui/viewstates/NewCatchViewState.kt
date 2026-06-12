package com.mobileprism.fishing.ui.viewstates

import androidx.compose.runtime.Immutable

@Immutable
sealed class NewCatchViewState {
    object Editing : NewCatchViewState()
    object SavingNewCatch : NewCatchViewState()
    object Complete : NewCatchViewState()
    data class Error(val error: Throwable?) : NewCatchViewState()
}
