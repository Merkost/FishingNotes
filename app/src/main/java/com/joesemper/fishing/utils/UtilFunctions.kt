package com.joesemper.fishing.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.pow
import kotlin.math.roundToInt


fun getNewCatchId() = getRandomString(10)
fun getNewMarkerId() = getRandomString(15)
fun getNewPhotoId() = getRandomString(12)

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun BottomSheetBehavior<ConstraintLayout>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<ConstraintLayout>.halfExpand() {
    this.state = BottomSheetBehavior.STATE_HALF_EXPANDED
}

fun BottomSheetBehavior<ConstraintLayout>.hide() {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}

fun BottomSheetBehavior<ConstraintLayout>.collapse() {
    this.state = BottomSheetBehavior.STATE_COLLAPSED
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}


