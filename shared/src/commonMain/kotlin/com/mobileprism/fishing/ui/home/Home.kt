package com.mobileprism.fishing.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.mobileprism.fishing.ui.HomeTabs
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.AppBottomNavigation
import com.mobileprism.fishing.ui.home.views.AppNavItem
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.map
import fishing.shared.generated.resources.notes
import fishing.shared.generated.resources.profile
import fishing.shared.generated.resources.weather
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class HomeSections(
    val title: StringResource,
    val icon: ImageVector,
    val hasRoute: (NavDestination) -> Boolean,
) {
    MAP(Res.string.map, Icons.Outlined.Map, { it.hasRoute<MainDestinations.Map>() }),
    NOTES(Res.string.notes, Icons.Outlined.Menu, { it.hasRoute<HomeTabs.NotesTab>() }),
    WEATHER(Res.string.weather, Icons.Outlined.WbSunny, { it.hasRoute<HomeTabs.WeatherTab>() }),
    PROFILE(Res.string.profile, Icons.Outlined.Person, { it.hasRoute<HomeTabs.ProfileTab>() }),
}

fun homeNavItems(): List<Triple<String, ImageVector, StringResource>> =
    HomeSections.entries.map { section ->
        Triple(section.name, section.icon, section.title)
    }

@Composable
fun FishingNotesBottomBar(
    modifier: Modifier,
    tabs: Array<HomeSections>,
    currentSection: HomeSections,
    navigateToRoute: (HomeSections) -> Unit,
) {
    val navItems = HomeSections.entries
        .filter { section -> tabs.any { it == section } }
        .map { section ->
            AppNavItem(
                key = section.name,
                icon = section.icon,
                label = stringResource(section.title),
            )
        }
    AppBottomNavigation(
        items = navItems,
        currentKey = currentSection.name,
        onSelect = { item ->
            HomeSections.entries
                .firstOrNull { it.name == item.key }
                ?.let(navigateToRoute)
        },
        modifier = modifier,
    )
}
