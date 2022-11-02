package com.mobileprism.fishing.model.entity

import androidx.annotation.StringRes
import com.mobileprism.fishing.R

data class FishingResponse(
    val success: Boolean = false,
    val fishingCode: FishingCodes = FishingCodes.UNKNOWN_ERROR,
    val httpCode: Int = 0,
    val description: String = "",
)

enum class FishingCodes(@StringRes val stringRes: Int) {
    SUCCESS(R.string.success),
    UNKNOWN_ERROR(R.string.fc_unknown_error),
    USER_NOT_FOUND(R.string.fc_user_not_found),
    USERNAME_NOT_FOUND(R.string.fc_username_not_found),
    INVALID_CREDENTIALS(R.string.fc_invalid_credentials),
    OTP_NOT_FOUND(R.string.fc_invalid_otp),
    NETWORK_ERROR(R.string.fc_network_error)

    ;


}