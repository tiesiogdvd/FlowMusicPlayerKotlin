package com.tiesiogdvd.composetest.ui.bottomNavBar

import com.tiesiogdvd.composetest.R

enum class NavRoutes{
    LIBRARY,
    EQUALIZER,
    SETTINGS,
    YT_DOWNLOAD,
    PLAYER
}

data class BottomNavItem(
    val name:String,
    val route:String,
    val icon: Int
)

object BottomNavItems {
    val BottomNavItems = listOf(
        BottomNavItem(
        name = "Library",
        route = NavRoutes.LIBRARY.name,
        icon = R.drawable.ic_action_navbar_library2
        ),
        BottomNavItem(
        name = "Equalizer",
        route = NavRoutes.EQUALIZER.name,
        icon = R.drawable.ic_action_navbar_equalizer1
        ),
        BottomNavItem(
        name = "Settings",
        route = NavRoutes.SETTINGS.name,
        icon = R.drawable.ic_action_navbar_settings
        ),
        BottomNavItem(
        name = "YT Download",
        route = NavRoutes.YT_DOWNLOAD.name,
        icon = R.drawable.ic_action_note_no_alpha
    )
    )
}

object MusicPlayerItem{
    val item = BottomNavItem(
        name = "Player",
        route = NavRoutes.PLAYER.name,
        icon = R.drawable.ic_action_play
    )
}