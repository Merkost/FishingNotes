package com.mobileprism.fishing.ui.viewstates

import androidx.annotation.StringRes

sealed class BaseViewState<out T> {
    data class Success<out T>(val data: T) : BaseViewState<T>()
    data class Error(
        val error: Throwable? = null,
        val text: String? = null,
        @StringRes val stringRes: Int? = null,
    ) : BaseViewState<Nothing>()
    class Loading(val progress: Int? = null) : BaseViewState<Nothing>()
}

