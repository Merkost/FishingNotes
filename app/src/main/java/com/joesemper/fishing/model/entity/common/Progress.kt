package com.joesemper.fishing.model.entity.common

sealed class Progress {
    class Loading(val percents: Int = 0) : Progress()
    object Complete: Progress()
    class Error(val error: Throwable): Progress()
}