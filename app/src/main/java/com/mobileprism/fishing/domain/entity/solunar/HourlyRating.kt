package com.mobileprism.fishing.domain.entity.solunar

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HourlyRating(
    @SerialName("0") val `0`: Int = 0,
    @SerialName("1") val `1`: Int = 0,
    @SerialName("2") val `2`: Int = 0,
    @SerialName("3") val `3`: Int = 0,
    @SerialName("4") val `4`: Int = 0,
    @SerialName("5") val `5`: Int = 0,
    @SerialName("6") val `6`: Int = 0,
    @SerialName("7") val `7`: Int = 0,
    @SerialName("8") val `8`: Int = 0,
    @SerialName("9") val `9`: Int = 0,
    @SerialName("10") val `10`: Int = 0,
    @SerialName("11") val `11`: Int = 0,
    @SerialName("12") val `12`: Int = 0,
    @SerialName("13") val `13`: Int = 0,
    @SerialName("14") val `14`: Int = 0,
    @SerialName("15") val `15`: Int = 0,
    @SerialName("16") val `16`: Int = 0,
    @SerialName("17") val `17`: Int = 0,
    @SerialName("18") val `18`: Int = 0,
    @SerialName("19") val `19`: Int = 0,
    @SerialName("20") val `20`: Int = 0,
    @SerialName("21") val `21`: Int = 0,
    @SerialName("22") val `22`: Int = 0,
    @SerialName("23") val `23`: Int = 0,

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
