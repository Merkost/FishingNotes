package com.mobileprism.fishing.ui.theme

import androidx.compose.ui.graphics.Color
import com.mobileprism.fishing.ui.home.views.contrastingCheckTint
import kotlin.test.Test
import kotlin.test.assertEquals

class ContrastingCheckTintTest {

    @Test
    fun darkSwatchReturnsWhite() {
        val tint = contrastingCheckTint(Color(0xFF1565C0))
        assertEquals(Color.White, tint)
    }

    @Test
    fun lightYellowSwatchReturnsDark() {
        val tint = contrastingCheckTint(Color(0xFFFFCA28))
        assertEquals(Color.Black, tint)
    }

    @Test
    fun pureWhiteReturnsDark() {
        val tint = contrastingCheckTint(Color.White)
        assertEquals(Color.Black, tint)
    }

    @Test
    fun pureBlackReturnsWhite() {
        val tint = contrastingCheckTint(Color.Black)
        assertEquals(Color.White, tint)
    }

    @Test
    fun midGreenReturnsWhite() {
        val tint = contrastingCheckTint(Color(0xFF66BB6A))
        assertEquals(Color.White, tint)
    }
}
