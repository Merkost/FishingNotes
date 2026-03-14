package com.mobileprism.fishing.utils

import android.content.Context
import android.widget.Toast
import com.mobileprism.fishing.R

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun showErrorToast(context: Context, text: String? = null) {
    Toast.makeText(context, text ?: context.getString(R.string.error_occured), Toast.LENGTH_SHORT).show()
}
