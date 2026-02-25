package com.mobileprism.fishing.ui.utils

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.mobileprism.fishing.R
import com.mobileprism.fishing.utils.showToast
import java.text.DecimalFormatSymbols

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}

fun String.toDoubleExOrNull() : Double? {
    val decimalSymbol = DecimalFormatSymbols.getInstance().decimalSeparator
    return if (decimalSymbol == ',') {
        replace(decimalSymbol, '.').toDoubleOrNull()
    } else {
        toDoubleOrNull()
    }
}

fun showError(applicationContext: Context, text: String?) {
    showToast(applicationContext.applicationContext,
        text ?: applicationContext.resources.getString(R.string.error_occured))
}