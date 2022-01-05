package com.joesemper.fishing.model.entity.solunar

data class HourlyRating(
    val `0`: Int = 0,
    val `1`: Int = 0,
    val `2`: Int = 0,
    val `3`: Int = 0,
    val `4`: Int = 0,
    val `5`: Int = 0,
    val `6`: Int = 0,
    val `7`: Int = 0,
    val `8`: Int = 0,
    val `9`: Int = 0,
    val `10`: Int = 0,
    val `11`: Int = 0,
    val `12`: Int = 0,
    val `13`: Int = 0,
    val `14`: Int = 0,
    val `15`: Int = 0,
    val `16`: Int = 0,
    val `17`: Int = 0,
    val `18`: Int = 0,
    val `19`: Int = 0,
    val `20`: Int = 0,
    val `21`: Int = 0,
    val `22`: Int = 0,
    val `23`: Int = 0,

) {
    operator fun get(currentHour24: Int) {
        `currentHour24`
    }
}