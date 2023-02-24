package com.tiesiogdvd.composetest.ui.bottomNavBar

import com.tiesiogdvd.composetest.R

data class BottomNavItem(
    val name:String,
    val route:String,
    val icon: Int
)

object BottomNavItems {
    val BottomNavItems = listOf(
        BottomNavItem(
        name = "Library",
        route = "library",
        icon = R.drawable.ic_action_navbar_library2
        ),
        BottomNavItem(
        name = "Equalizer",
        route = "equalizer",
        icon = R.drawable.ic_action_navbar_equalizer1
        ),
        BottomNavItem(
        name = "Settings",
        route = "settings",
        icon = R.drawable.ic_action_navbar_settings
        ),
        BottomNavItem(
        name = "YT Download",
        route = "yt_download",
        icon = R.drawable.ic_action_note_no_alpha
    )
    )
}

object MusicPlayerItem{
    val item = BottomNavItem(
        name = "Player",
        route = "player",
        icon = R.drawable.ic_action_play
    )
}