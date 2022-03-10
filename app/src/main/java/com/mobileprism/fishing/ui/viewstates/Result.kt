package com.mobileprism.fishing.ui.viewstates

// TODO: Replace with kotlin same class
sealed class Result(val error: Throwable? = null) {
    object Success : Result()
    class Error(error: Throwable?) : Result(error)
}
