package com.mobileprism.fishing.utils

import android.util.Log

actual class Logger actual constructor() {
    actual fun log(message: String?) {
       Log.d("Fishing", message ?: "No error message")
    }
}