package com.mobileprism.fishing.model.entity.common

sealed class LiteProgress {
    // TODO: kill
    object Loading: LiteProgress()
    object Complete: LiteProgress()
    class Error(val error: Throwable?): LiteProgress()
}