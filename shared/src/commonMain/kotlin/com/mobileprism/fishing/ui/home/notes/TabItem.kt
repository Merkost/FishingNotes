package com.mobileprism.fishing.ui.home.notes

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

typealias ComposableFun = @Composable (navController: NavController) -> Unit

sealed class TabItem(
    var icon: DrawableResource,
    var titleRes: StringResource,
    var screen: ComposableFun
) {
    class Places(screen: ComposableFun) :
        TabItem(Res.drawable.ic_baseline_location_on_24, Res.string.places, screen)

    class Catches(screen: ComposableFun) :
        TabItem(Res.drawable.ic_fish, Res.string.catches, screen)

    class Statistics(screen: ComposableFun) :
        TabItem(Res.drawable.ic_statistics, Res.string.statistics, screen)

    data object PlaceCatches :
        TabItem(Res.drawable.ic_fish, Res.string.catches, { _ -> })

    data object Note :
        TabItem(Res.drawable.ic_baseline_sticky_note_2_24, Res.string.note, { _ -> })
}
