package com.tiesiogdvd.composetest.ui.settings

import com.tiesiogdvd.composetest.R

enum class SettingsType{
    PLAYER_SCREEN,
    NOTIFICATION,
    AUDIO_DISCOVERY,
    LIBRARY,
    LOOKS,
    DEFAULTS,
    YT_DOWNLOAD
}

enum class SettingsMethod{
    SWITCH, RANGE_SLIDER, SELECT_MULTIPLE, SELECT_SINGLE, CHECKBOX, COLOR_SELECTION, INPUT_TEXT
}

data class SettingsItems(
    val name:String,
    val settingInfo:String?,
    val settingsType: SettingsType,
    val items:List<SettingsItem>,
    val enabled: Boolean
)

data class SettingsItem(
    val name:String,
    val settingInfo:String,
    val settingsMethod: SettingsMethod,
    val childItems: List<ChildItem>?,
    val minRange: Int?,
    val rangeText: String?,
    val enabled: Boolean
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