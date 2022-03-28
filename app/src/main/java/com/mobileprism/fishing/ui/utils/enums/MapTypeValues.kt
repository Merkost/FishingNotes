package com.mobileprism.fishing.ui.utils.enums

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.theme.primaryBlueColor
import com.mobileprism.fishing.ui.theme.primaryFigmaColor

enum class MapTypeValues(override val stringRes: Int): StringOperation {
    GoogleMap(R.string.google_map),
    YandexMap(R.string.yandex_map);
}
