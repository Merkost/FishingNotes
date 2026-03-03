@file:JvmName("ModifierAndroid")

package com.mobileprism.fishing.ui.utils

import android.content.Context
import com.mobileprism.fishing.R
import com.mobileprism.fishing.utils.showToast
fun showError(applicationContext: Context, text: String?) {
    showToast(applicationContext.applicationContext,
        text ?: applicationContext.resources.getString(R.string.error_occured))
}
