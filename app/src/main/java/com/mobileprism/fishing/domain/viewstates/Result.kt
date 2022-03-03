package com.mobileprism.fishing.domain.viewstates

sealed class Result(val error: Throwable? = null) {
    object Success : Result()
    class Error(error: Throwable?) : Result(error)
}
