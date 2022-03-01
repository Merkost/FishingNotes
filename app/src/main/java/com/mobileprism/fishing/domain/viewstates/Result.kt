package com.mobileprism.fishing.domain.viewstates

import androidx.annotation.StringRes

sealed class Result(val error: Throwable? = null) {
    object Success : Result()
    class Error(error: Throwable?) : Result(error)
}
