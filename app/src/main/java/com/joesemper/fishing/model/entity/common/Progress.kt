package com.joesemper.fishing.model.entity.common

sealed class Progress {
    class Loading(val message: String = ""): Progress()
    object Complete: Progress()
    class Error(val error: Throwable): Progress()
}