package com.mobileprism.fishing.utils

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.ShapeTokens

object Constants {

    const val TIME_TO_EXIT = 2000L
    val defaultFabBottomPadding: Dp = 16.dp

    val bottomBannerPadding: Dp = 80.dp

    @Deprecated(
        "Use ShapeTokens.bottomSheet",
        ReplaceWith("ShapeTokens.bottomSheet", "com.mobileprism.fishing.ui.theme.ShapeTokens")
    )
    val modalBottomSheetCorners: Shape = ShapeTokens.bottomSheet

    const val MAX_PHOTOS: Int = 3
    const val WIND_ROTATION = 45f

    const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    const val ITEM_PHOTO = "ITEM_PHOTO"

    const val CURRENT_PLACE_ITEM_ID = "Current_place"

}
