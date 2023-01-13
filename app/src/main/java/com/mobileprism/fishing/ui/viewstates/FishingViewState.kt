package com.mobileprism.fishing.ui.viewstates

import androidx.annotation.StringRes
import com.mobileprism.fishing.model.entity.FishingResponse

sealed class FishingViewState<out T> {
    data class Success<out T>(val data: T) : FishingViewState<T>()
    data class Error(
        val fishingError: FishingResponse
    ) : FishingViewState<Nothing>()
    object Loading : FishingViewState<Nothing>()
}

sealed class BaseViewState<out T> {
    data class Success<out T>(val data: T) : BaseViewState<T>()
    data class Error(
        val throwable: Throwable? = null,
        @StringRes val stringRes: Int? = null,
        ) : BaseViewState<Nothing>()
    object Loading : BaseViewState<Nothing>()
}

