package com.mobileprism.fishing.model.entity

data class FishingResponse(
    val success: Boolean = false,
    val fishingCode: FishingCodes,
    val httpCode: Int = 0,
    val description: String = "",
)

enum class FishingCodes {
    SUCCESS,
    USER_NOT_FOUND,
    USERNAME_NOT_FOUND,
    INVALID_CREDENTIALS,
    OTP_NOT_FOUND,
    UNKNOWN_ERROR,


}