package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensTest {

    @Test
    fun spacingScaleValuesMatchSpec() {
        assertEquals(0.dp, Spacing.none)
        assertEquals(2.dp, Spacing.xxs)
        assertEquals(4.dp, Spacing.xs)
        assertEquals(8.dp, Spacing.sm)
        assertEquals(12.dp, Spacing.md)
        assertEquals(16.dp, Spacing.lg)
        assertEquals(24.dp, Spacing.xl)
        assertEquals(32.dp, Spacing.xxl)
        assertEquals(48.dp, Spacing.xxxl)
    }

    @Test
    fun spacingSemanticAliasesMatchSpec() {
        assertEquals(16.dp, Spacing.screenH)
        assertEquals(24.dp, Spacing.sectionGap)
        assertEquals(8.dp, Spacing.listItemGap)
        assertEquals(16.dp, Spacing.cardPadding)
        assertEquals(88.dp, Spacing.fabClearance)
    }

    @Test
    fun elevationLadderAndAliasesMatchSpec() {
        assertEquals(0.dp, Elevation.level0)
        assertEquals(1.dp, Elevation.level1)
        assertEquals(3.dp, Elevation.level2)
        assertEquals(6.dp, Elevation.level3)
        assertEquals(8.dp, Elevation.level4)
        assertEquals(12.dp, Elevation.level5)
        assertEquals(Elevation.level1, Elevation.card)
        assertEquals(Elevation.level2, Elevation.raisedCard)
        assertEquals(Elevation.level3, Elevation.dialog)
        assertEquals(Elevation.level4, Elevation.bottomSheet)
        assertEquals(Elevation.level3, Elevation.fab)
    }

    @Test
    fun emphasisAlphasMatchSpec() {
        assertEquals(0.38f, Emphasis.disabled)
        assertEquals(0.60f, Emphasis.medium)
        assertEquals(0.74f, Emphasis.hint)
        assertEquals(0.12f, Emphasis.divider)
        assertEquals(0.08f, Emphasis.pressedOverlay)
        assertEquals(0.32f, Emphasis.scrim)
    }

    @Test
    fun motionDurationsMatchSpec() {
        assertEquals(150, Motion.short)
        assertEquals(250, Motion.medium)
        assertEquals(400, Motion.long)
    }

    @Test
    fun shapeScaleIsCanonical() {
        assertEquals(RoundedCornerShape(2.dp), Shapes.extraSmall)
        assertEquals(RoundedCornerShape(8.dp), Shapes.small)
        assertEquals(RoundedCornerShape(12.dp), Shapes.medium)
        assertEquals(RoundedCornerShape(16.dp), Shapes.large)
        assertEquals(RoundedCornerShape(24.dp), Shapes.extraLarge)
    }

    @Test
    fun bottomSheetShapeIsTopRoundedOnly() {
        val expected = RoundedCornerShape(
            topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
        )
        assertEquals(expected, ShapeTokens.bottomSheet)
    }

    @Test
    fun errorRolesMatchSpecOnEveryScheme() {
        assertEquals(Color(0xFFBA1A1A), BlueLightColorScheme.error)
        assertEquals(Color(0xFFBA1A1A), GreenLightColorScheme.error)
        assertEquals(Color(0xFFFFB4AB), BlueDarkColorScheme.error)
        assertEquals(Color(0xFFFFB4AB), GreenDarkColorScheme.error)
        assertEquals(Color(0xFFFFDAD6), BlueLightColorScheme.errorContainer)
        assertEquals(Color(0xFF93000A), BlueDarkColorScheme.errorContainer)
    }

    @Test
    fun brandGradientsAreThemeDriven() {
        val light = BrandGradients.primaryVertical(BlueLightColorScheme)
        val dark = BrandGradients.primaryVertical(BlueDarkColorScheme)
        assertEquals(false, light == dark, "gradient must follow the color scheme")
    }
}
