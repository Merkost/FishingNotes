package com.joesemper.fishing.model.entity.common

sealed class LiteProgress {
    object Loading: LiteProgress()
    object Complete: LiteProgress()
    class Error(val error: Throwable?): LiteProgress()
}