package com.mobileprism.fishing.model.entity.solunar

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
    operator fun get(currentHour24: Int): Int {
        return when(currentHour24)  {
            0 -> this.`0`
            1 -> this.`1`
            2 -> this.`2`
            3 -> this.`3`
            4 -> this.`4`
            5 -> this.`5`
            6 -> this.`6`
            7 -> this.`7`
            8 -> this.`8`
            9 -> this.`9`
            10 -> this.`10`
            11 -> this.`11`
            12 -> this.`12`
            13 -> this.`13`
            14 -> this.`14`
            15 -> this.`15`
            16 -> this.`16`
            17 -> this.`17`
            18 -> this.`18`
            19 -> this.`19`
            20 -> this.`20`
            21 -> this.`21`
            22 -> this.`22`
            23 -> this.`23`
            else -> this.`0`
        }
    }
}