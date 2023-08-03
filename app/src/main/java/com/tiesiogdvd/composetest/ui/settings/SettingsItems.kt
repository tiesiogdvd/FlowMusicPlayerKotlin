package com.tiesiogdvd.composetest.ui.settings

import androidx.datastore.preferences.core.Preferences
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItem
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavRoutes

enum class SettingsType{
    PLAYER_SCREEN,
    GENERAL,
    NOTIFICATIONS,
    AUDIO_DISCOVERY,
    LIBRARY,
    MENU,
    LOOKS,
    DEFAULTS,
    YT_DOWNLOAD,
    LYRICS_API,
    EQUALIZER,
    NAVIGATION
}
data class SettingsNavItem(
    val name:String,
    val route:String,
    val icon: Int
)

object SettingsNavItems {
    val items = listOf(
        SettingsNavItem(
            name = "Player Screen",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_play
        ),
        SettingsNavItem(
            name = "General",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_equalizer1
        ),
        SettingsNavItem(
            name = "Notifications",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_settings
        ),
        SettingsNavItem(
            name = "Audio Discovery",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_note_no_alpha
        ),
        SettingsNavItem(
            name = "Library Menu",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_play
        ),
        SettingsNavItem(
            name = "Looks",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_equalizer1
        ),
        SettingsNavItem(
            name = "Defaults",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_settings
        ),
        SettingsNavItem(
            name = "YT Download",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_note_no_alpha
        ),
        SettingsNavItem(
            name = "Looks",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_equalizer1
        ),
        SettingsNavItem(
            name = "Lyrics",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_navbar_settings
        ),
        SettingsNavItem(
            name = "Navigation",
            route = SettingsType.PLAYER_SCREEN.name,
            icon = R.drawable.ic_action_note_no_alpha
        ),
    )
}


enum class SettingsMethod{
    SWITCH, RANGE_SLIDER, SELECT_MULTIPLE, SELECT_SINGLE, CHECKBOX, COLOR_SELECTION, INPUT_TEXT
}

/*enum class SettingsPlayerScreen(settingsItem: SettingsItem) {
    HideSeekbar(SettingsItem("Hide seekbar", "", settingsMethod = SettingsMethod.SWITCH, enabled = true)),
}*/

data class SettingsItems(
    val name:String,
    val settingInfo:String?,
    val settingsType: SettingsType,
    val items:List<SettingsItem>,
    val enabled: Boolean
)

data class SettingsItem(
    val name:String,
    val settingInfo:String? = null,
    val settingsMethod: SettingsMethod,
    val childItems: List<ChildItem>? = null,
    val minRange: Int? = null,
    val maxRange: Int? = null,
    val rangeText: String? = null,
    val enabled: Boolean? = true,
    val preferencesKey: Preferences.Key<*>? = null
)


data class ChildItem(
    val name: String
)


object SettingsItemsList{
    val audioDiscoverySettings = SettingsItems(
        name = "Audio discovery",
        enabled = true,
        settingInfo = null,
        settingsType = SettingsType.AUDIO_DISCOVERY,
        items = listOf(
            SettingsItem(
                name = "Lowest file size",
                settingInfo = "Sets the lowest file size on scanning of audio",
                settingsMethod = SettingsMethod.RANGE_SLIDER,
                childItems = null,
                minRange = 0,
                rangeText = "KB",
                enabled = true
            ),
            SettingsItem(
                name = "Lowest audio length",
                settingInfo = "Sets the lowest file size on scanning of audio",
                settingsMethod = SettingsMethod.RANGE_SLIDER,
                childItems = null,
                minRange = 0,
                rangeText = "seconds",
                enabled = true
            ),
            SettingsItem(
                name = "Audio scan method",
                settingInfo = "Sets the lowest file size on scanning of audio",
                settingsMethod = SettingsMethod.SELECT_SINGLE,
                childItems = listOf(ChildItem("MediaStore"), ChildItem("Storage scraping")),
                minRange = 0,
                rangeText = "seconds",
                enabled = true
            )
        )
    )
}