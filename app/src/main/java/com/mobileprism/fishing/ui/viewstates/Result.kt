package com.mobileprism.fishing.ui.viewstates

sealed class Result(val error: Throwable? = null) {
    object Success : Result()
    class Error(error: Throwable?) : Result(error)
}
