package com.mobileprism.fishing.ui.sweep

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyComponentRemovalTest {

    private val uiRoot = File("src/commonMain/kotlin/com/mobileprism/fishing/ui")

    private val bannedSymbols = listOf(
        "fun LoadingIconButtonOutlined(",
        "fun HeaderText(",
        "fun HeaderTextSecondary(",
        "fun PrimaryTextBold(",
        "fun SecondaryTextLight(",
        "fun SubtitleWithIcon(",
        "fun MyCardNoPadding(",
        "fun MyCard(",
        "fun MyClickableCard(",
        "fun DefaultCard(",
        "fun NoInternetView(",
        "fun ErrorView(",
        "fun FishAmountAndWeightView(",
        "fun LottieStars(",
        "fun MapLayersButton(",
        "fun MapSettingsButton(",
    )

    @Test
    fun legacyComposableDeclarationsAreRemoved() {
        if (!uiRoot.exists()) return
        val ktFiles = uiRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        val offenders = mutableListOf<String>()
        for (file in ktFiles) {
            val text = file.readText()
            for (banned in bannedSymbols) {
                if (text.contains(banned)) {
                    offenders.add("${file.path} declares $banned")
                }
            }
        }
        assertTrue(
            offenders.isEmpty(),
            "Legacy composables must stay deleted, found:\n" + offenders.joinToString("\n")
        )
    }
}
