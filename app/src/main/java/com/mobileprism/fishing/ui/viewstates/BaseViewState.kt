package com.mobileprism.fishing.ui.viewstates

import androidx.annotation.StringRes
import com.mobileprism.fishing.model.entity.FishingResponse

sealed class BaseViewState<out T> {
    data class Success<out T>(val data: T) : BaseViewState<T>()
    data class Error(
        val fishingError: FishingResponse? = null,
        val error: Throwable? = null,
    ) : BaseViewState<Nothing>()
    object Loading : BaseViewState<Nothing>()
}

