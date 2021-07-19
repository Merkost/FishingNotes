package com.joesemper.fishing.model.common

sealed class Progress {
    class Loading(val progress: Int? = 0): Progress()
    object Complete: Progress()
    class Error(val error: Throwable): Progress()
}