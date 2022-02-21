package com.mobileprism.fishing.domain.viewstates

import androidx.annotation.StringRes

sealed class Resource<T>(val data: T?, @StringRes val textRes: Int? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(@StringRes textRes: Int, data: T? = null) : Resource<T>(data, textRes)
}
